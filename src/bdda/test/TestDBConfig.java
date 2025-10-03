package bdda.test;

import java.io.File;

import bdda.config.DBConfig;

public class TestDBConfig {

    public static DBConfig testConstructeur(String dbpath, int pageSize, int dm_maxfilecount){
        DBConfig config1 = null;
        try {
            config1 = new DBConfig(dbpath, pageSize, dm_maxfilecount);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new DBConfig();
        }
        return config1;
    }

    // Méthode qui exécute tous les tests de DBConfig
    public static void runTests() {
        System.out.println("===== Tests DBConfig =====");

        // 1. Test constructeur direct avec page size et dm_maxfilecount correct
        System.out.println("Test construteur sans erreur dans pagesize et dm_maxfilecount");
        DBConfig testConfig1 = testConstructeur("."+File.separator+"DB"+File.separator+"binData", 32, 512);
        System.out.println(" Fin test 1");

        //2 Test constructeur direct avec pagesize incorrect et dm_maxfilecount correct
        System.out.println("Test construteur avec erreur dans pagesize et sans erreur dans dm_maxfilecount");
        DBConfig testConfig2 = testConstructeur("."+File.separator+"DB"+File.separator+"binData", 0, 512);
        System.out.println(" Fin test 2");

        //3 Test constructeur direct avec pagesize correct et dm_maxfilecount incorrect
        System.out.println("Test construteur avec erreur dans pagesize et sans erreur dans dm_maxfilecount");
        DBConfig testConfig3 = testConstructeur("."+File.separator+"DB"+File.separator+"binData", 32, 0);
        System.out.println(" Fin test 3");

        //4 Test constructeur dbpah incorrect avec pagesize et dm_maxfilecount correct
        System.out.println("Test construteur avec erreur dans pagesize et sans erreur dans dm_maxfilecount");
        DBConfig testConfig4 = testConstructeur("."+File.separator+"DB"+File.separator+"binData", 32, 0);
        System.out.println(" Fin test 4");


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

        //4. Test 
    }
}
