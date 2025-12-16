package bdda.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bdda.config.BufferManager;
import bdda.config.DBConfig;
import bdda.config.DiskManager;
import bdda.config.InfoColonne;
import bdda.config.PageID;
import bdda.config.Record;
import bdda.config.RecordId;
import bdda.config.Relation;

/**
 * Classe de test pour Relation
 */
public class RelationTests {

    public static void main(String[] args) {
        System.out.println("=== TESTS Relation ===");

        try {
            // === Initialisation DiskManager et BufferManager ===
            DBConfig conf = new DBConfig();
            DiskManager diskManager = new DiskManager(conf);
            diskManager.init();
            BufferManager bufferManager = new BufferManager(conf, diskManager);
            bufferManager.init();

            // === Création des colonnes ===
            InfoColonne<String, String> col1 = new InfoColonne<>();
            col1.setNom("id");
            col1.setType("INT");

            InfoColonne<String, String> col2 = new InfoColonne<>();
            col2.setNom("note");
            col2.setType("FLOAT");

            InfoColonne<String, String> col3 = new InfoColonne<>();
            col3.setNom("nom");
            col3.setType("CHAR");

            InfoColonne<String, String> col4 = new InfoColonne<>();
            col4.setNom("ville");
            col4.setType("VARCHAR");

            List<InfoColonne<String, String>> colonnes = Arrays.asList(col1, col2, col3, col4);

            // === Allocation automatique de la header page ===
            PageID headerPage = bufferManager.allocPage();
            if (headerPage == null) throw new Exception("Impossible d'allouer la header page !");
            Relation relation = new Relation("Etudiants", colonnes, headerPage, diskManager, bufferManager);
            relation.initHeaderPage(headerPage);
            System.out.println("Relation créée : " + relation.getNom() + " (" + relation.getColonne() + " colonnes)");

            // === Test écriture et lecture d'un record ===
            Record record = new Record(Arrays.asList("42", "18.5", "Rayan", "Paris"));
            int bufferSize = 200;
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(bufferSize);

            relation.writeRecordToBuffer(record, buffer, 0);
            System.out.println("Écriture du record dans le buffer réussie.");

            Record recordLu = new Record();
            relation.readFromBuffer(recordLu, buffer, 0);
            System.out.println("Lecture du buffer réussie : " + recordLu.getValeurs());

            // === Insertion de plusieurs records ===
            for (int i = 1; i <= 7; i++) {
                Record r = new Record(Arrays.asList("" + i, "" + (10 + i), "Nom" + i, "Ville" + i));
                RecordId rid = relation.insertRecord(r);
                if (rid != null) {
                    System.out.println("Record inséré : " + r.getValeurs() + " => page " + rid.getPageId() + " slot " + rid.getSlotIdx());
                } else {
                    System.out.println("Erreur : le record n'a pas pu être inséré.");
                }
                if (i==6){
                    relation.deleteRecord(rid);
                }
            }

            // === Récupération de tous les records ===
            ArrayList<Record> allRecords = relation.getAllRecords();
            System.out.println("\nTous les records dans la relation :");
            for (Record r : allRecords) {
                System.out.println(r.getValeurs());
            }

            // === Récupération par page ===
            System.out.println("\nRecords par page :");
            for (PageID pageId : relation.getDataPages()) {
                ArrayList<Record> pageRecords = relation.getRecordsInDataPage(pageId);
                System.out.println("Page " + pageId + " :");
                for (Record r : pageRecords) {
                    System.out.println("  " + r.getValeurs());
                }
            }

            // === Suppression d'un record ===
            if (!allRecords.isEmpty()) {
                RecordId firstRid = relation.insertRecord(allRecords.get(0)); // On peut tester delete
                relation.deleteRecord(firstRid);
                System.out.println("\nSuppression du premier record : " + firstRid);
            }

            // === Pages de données allouées après insertions et suppression ===
            System.out.println("\nPages de données allouées :");
            for (PageID pageId : relation.getDataPages()) {
                System.out.println(pageId);
            }

        } catch (Exception e) {
            System.out.println("Erreur pendant les tests Relation : " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== FIN TESTS Relation ===");
    }
}

