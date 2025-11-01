package bdda.test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import bdda.config.InfoColonne;
import bdda.config.Relation;
import bdda.config.Record;

/**
 * Classe de test pour Relation
 * @author Rayan
 * @version 1.0
 */
public class RelationTests {

    
    public static void main(String[] args) {
        System.out.println("TESTS Relation");

        try {
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

            Relation relation = new Relation("Etudiants", colonnes);
            System.out.println("Relation créée avec succès : " + relation.getNom() + " (" + relation.getColonne() + " colonnes)");

            bdda.config.Record record = new bdda.config.Record(Arrays.asList(
                "42", "18.5", "Rayan", "Paris"
            ));

            int bufferSize = 200;
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            relation.writeRecordToBuffer(record, buffer, 0);
            System.out.println("Écriture du record dans le buffer réussie.");

            bdda.config.Record recordLu = new bdda.config.Record();
            relation.readFromBuffer(recordLu, buffer, 0);

            System.out.println("Lecture du buffer réussie !");
            System.out.println("Valeurs lues : " + recordLu.getValeurs());

        } catch (Exception e) {
            System.out.println("Erreur pendant le test Relation : " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("FIN TEST Relation");
    }
}
