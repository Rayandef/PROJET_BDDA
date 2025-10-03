package bdda.test;

import bdda.config.DBConfig;

public class TestDBConfig {

    // Méthode qui exécute tous les tests de DBConfig
    public static void runTests() {
        System.out.println("===== Tests DBConfig =====");

        // 1. Test constructeur direct
        DBConfig conf1 = new DBConfig("./DB", 32, 512);
        System.out.println("Config directe : " + conf1);
        if (!"./DB".equals(conf1.getDbpath())) {
            System.out.println(" Erreur : dbpath direct incorrect !");
        } else {
            System.out.println(" Test constructeur direct OK");
        }

        // 2. Test lecture depuis fichier correct
        DBConfig conf2 = DBConfig.loadDBConfig("config/dbconfig.properties");
        if (conf2 != null) {
            System.out.println("Config fichier : " + conf2);
            if (!"./DB/binData".equals(conf2.getDbpath())) {
                System.out.println(" Erreur : dbpath lu incorrect !");
            } else {
                System.out.println(" Test lecture fichier OK");
                System.out.println("Le pageSize: " + conf2.getPageSize());
                System.out.println("Le dm_maxfilecount est: " + conf2.getDm_maxfilecount());
            }
        } else {
            System.out.println(" Erreur : config fichier n’a pas pu être chargée !");
        }

        // 3. Test fichier inexistant
        DBConfig conf3 = DBConfig.loadDBConfig("config/fichier_inexistant.properties");
        if (conf3 == null) {
            System.out.println(" Test fichier inexistant OK (retour null)");
        } else {
            System.out.println(" Erreur : un objet a été créé alors que le fichier est absent !");
        }

        System.out.println("===== Fin des tests DBConfig =====");
    }
}
