package bdda.test;

import java.nio.ByteBuffer;
import java.util.*;
import bdda.config.*;

/**
 * Classe de test pour la classe Relation.
 * Teste la lecture et l'écriture de Record dans un ByteBuffer.
 * @author Rayan
 * @version 1.0
 */
public class RelationTests {

    public static void testIntFloat() throws Exception {
        System.out.println("Test Relation : INT et FLOAT");

        // Définition des colonnes
        InfoColonne<String, String> c1 = new InfoColonne<>();
        c1.setNom("id");
        c1.setType("INT");

        InfoColonne<String, String> c2 = new InfoColonne<>();
        c2.setNom("note");
        c2.setType("FLOAT");

        List<InfoColonne<String, String>> colonnes = Arrays.asList(c1, c2);
        Relation rel = new Relation("Notes", colonnes);

        // Création d’un enregistrement
        Record record = new Record(Arrays.asList("42", "18.5"));

        // Création d’un buffer et écriture du record
        ByteBuffer buffer = ByteBuffer.allocate(100);
        rel.writeRecordToBuffer(record, buffer, 0);

        // Lecture dans un nouveau record
        Record recordLu = new Record();
        rel.readFromBuffer(recordLu, buffer, 0);

        System.out.println("Record initial : " + record);
        System.out.println("Record lu      : " + recordLu);
        System.out.println("Fin Test INT et FLOAT\n");
    }

    public static void testCharVarchar() throws Exception {
        System.out.println("Test Relation : CHAR et VARCHAR");

        // Définition des colonnes
        InfoColonne<String, String> c1 = new InfoColonne<>();
        c1.setNom("prenom");
        c1.setType("CHAR");

        InfoColonne<String, String> c2 = new InfoColonne<>();
        c2.setNom("ville");
        c2.setType("VARCHAR");

        List<InfoColonne<String, String>> colonnes = Arrays.asList(c1, c2);
        Relation rel = new Relation("Personnes", colonnes);

        // Création d’un enregistrement
        Record record = new Record(Arrays.asList("Rayan", "Paris"));

        // Création d’un buffer et écriture du record
        ByteBuffer buffer = ByteBuffer.allocate(200);
        rel.writeRecordToBuffer(record, buffer, 0);

        // Lecture dans un nouveau record
        Record recordLu = new Record();
        rel.readFromBuffer(recordLu, buffer, 0);

        System.out.println("Record initial : " + record);
        System.out.println("Record lu      : " + recordLu);
        System.out.println("Fin Test CHAR et VARCHAR\n");
    }

    public static void main(String[] args) {
        try {
            System.out.println("Début des tests Relation\n");
            testIntFloat();
            testCharVarchar();
            System.out.println("Fin des tests Relation");
        } catch (Exception e) {
            System.out.println("Erreur pendant les tests Relation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
