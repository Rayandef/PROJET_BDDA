package bdda.test;

import java.io.File;

import bdda.config.DBConfig;

/**
 * Classe de tests pour la configuration de la base de données (DBConfig).
 * Permet de vérifier le comportement du constructeur et du chargement depuis un fichier.
 */
public class TestDBConfig {

    /**
     * Teste le constructeur de DBConfig avec gestion des exceptions.
     *
     * @param dbpath Le chemin vers la base de données à tester
     * @param pageSize La taille des pages à tester
     * @param dm_maxfilecount Le nombre maximum de fichiers à tester
     * @return Un objet DBConfig valide ou une configuration par défaut en cas d'erreur
     */
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

    public static DBConfig testConstructeurTD3(String dbpath, int pageSize, int dm_maxfilecount, int bm_buffercount, String bm_policy){
        DBConfig config = null;
        try {
            config = new DBConfig(dbpath, pageSize, dm_maxfilecount, bm_buffercount, bm_policy);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new DBConfig();
        }
        return config;
    }

    /**
     * Exécute tous les tests sur la classe DBConfig.
     * Affiche les résultats des différents scénarios de test sur la console.
     */
    public static void runTests() {
        System.out.println("===== Tests DBConfig =====");
        System.out.println("Test1");
        // 1. Test constructeur direct avec page size et dm_maxfilecount correct
        System.out.println("Test construteur sans erreur dans pagesize et dm_maxfilecount");
        DBConfig testConfig1 = testConstructeur("."+File.separator+"DB"+File.separator+"binData", 32, 512);
        System.out.println(" Fin test 1");

        System.out.println("Test2");
        // 2. Test constructeur direct avec pagesize incorrect et dm_maxfilecount correct
        System.out.println("Test construteur avec erreur dans pagesize et sans erreur dans dm_maxfilecount");
        DBConfig testConfig2 = testConstructeur("."+File.separator+"DB"+File.separator+"binData", 0, 512);
        System.out.println("Fin test 2");

        System.out.println("Test3");
        // 3. Test constructeur direct avec pagesize correct et dm_maxfilecount incorrect
        System.out.println("Test construteur avec erreur dans pagesize et sans erreur dans dm_maxfilecount");
        DBConfig testConfig3 = testConstructeur("."+File.separator+"DB"+File.separator+"binData", 32, 0);
        System.out.println("Fin test 3");

        System.out.println("Test4");
        // 4. Test constructeur dbpath incorrect avec pagesize et dm_maxfilecount correct
        System.out.println("Test construteur avec erreur dans pagesize et sans erreur dans dm_maxfilecount");
        DBConfig testConfig4 = testConstructeur("."+File.separator+"DB"+File.separator+"binData", 32, 0);
        System.out.println(" Fin test 4");

        System.out.println("Test5");
        // 5. Test lecture depuis fichier de configuration correct
        System.out.println("Test lecture fichier de configuration correct");
        DBConfig conf = DBConfig.loadDBConfig("config"+File.separator+"dbconfig.properties");
        System.out.println();
        if (conf != null) {
            System.out.println("Config fichier : " + conf);
            if (!"./DB/binData".equals(conf.getDbpath())) {
                System.out.println("Erreur : dbpath lu incorrect !");
            } else {
                System.out.println("Test lecture fichier OK");
            }
        } else {
            System.out.println("Erreur : config fichier n’a pas pu être chargée !");
        }
        System.out.println("Fin test 5");

        System.out.println("Test6");
        // 6. Test lecture d'un fichier de configuration inexistant
        System.out.println("Test lecture fichier de configuration inexistant");
        DBConfig conf3 = DBConfig.loadDBConfig("config"+ File.separator+"fichier_inexistant.properties");
        if (conf3 == null) {
            System.out.println("Test fichier inexistant OK (retour null)");
        } else {
            System.out.println("Erreur : un objet a été créé alors que le fichier est absent !");
        }
        System.out.println("Fin test 6");

        System.out.println("Test7");
        //7. Test constructeur avec bm_buffercount incorrect
        System.out.println("Test constructeur avec bm_buffercount incorrect");
        DBConfig testconf4 = testConstructeurTD3("."+File.separator+"DB"+File.separator+"binData", 32, 512, 0, "LRU");
        System.out.println("Fin test 7");
        System.out.println("Test8");
        //8. Test constructeur avec bm_policy incorrect
        System.out.println("Test constructeur avec bm_policy incorrect");
        DBConfig testconf5 = testConstructeurTD3("."+File.separator+"DB"+File.separator+"binData", 32, 512, 20, "FIFO");
        System.out.println("===== Fin des tests DBConfig =====");
    }

    public static void main(String[] args) {
        runTests();
    }
}