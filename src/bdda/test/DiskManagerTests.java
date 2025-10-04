package bdda.test;

import bdda.config.DBConfig;
import bdda.config.DiskManager;

import java.io.File;

/**
 * Classe de tests pour DiskManager (Init et Finish)
 * @author Rayan
 * @version 1.0
 */
public class DiskManagerTests {

    public static void testInitAndFinish() {
        System.out.println("===== Test Init & Finish DiskManager =====");

        // Config de test
        DBConfig conf = new DBConfig(); // utilise le chemin par défaut ./DB/binData
        DiskManager dm = new DiskManager(conf);

        // Test init
        dm.init();
        File dataDir = new File(conf.getDbpath());
        if (dataDir.exists() && dataDir.isDirectory()) {
            System.out.println("Test Init OK : dossier créé/existant -> " + dataDir.getAbsolutePath());
        } else {
            System.out.println("Test Init ECHEC : dossier non créé !");
        }

        // Test finish
        dm.finish();
        File saveFile = new File(conf.getDbpath(), "freepages.txt");
        if (saveFile.exists()) {
            System.out.println("Test Finish OK : fichier de sauvegarde créé -> " + saveFile.getAbsolutePath());
        } else {
            System.out.println("Test Finish ECHEC : fichier freepages.txt manquant !");
        }

        System.out.println("===== Fin Test Init & Finish DiskManager =====");
    }

    public static void main(String[] args) {
        testInitAndFinish();
    }
}
