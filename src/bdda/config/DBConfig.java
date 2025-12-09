package bdda.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
    /** Classe permettant le chargement de la base de donnée
    */
public class DBConfig {
    /**dbpath -> variable indiquant l'emplacement de la base de données */
    private String dbpath;
    /**pageSize -> variable indiquant la taille des pages de donnés */
    private int pageSize;
    /**dm_maxfilecount -> variable indiquant le nombres maximum de fichiers Datax.bin */
    private int dm_maxfilecount;
    /**dm_buffercount -> variable indiquant le nombre de buffers gérés par le BufferManager */
    private int bm_buffercount;
    /**bm_policy -> variable indiquant la politique de remplacement */
    private String bm_policy;

    /**
     * @param dbpath
     * @throws Exception
     */
    public DBConfig(String dbpath) throws Exception{
        if(!dbpath.equals("." + File.separator + "DB" + File.separator + "binData")){
            throw new Exception("dbpath incorrect");
        }
        this.dbpath = dbpath;
        this.pageSize = 256;
        this.dm_maxfilecount = 512;
        this.bm_buffercount = 20;
        this.bm_policy = "LRU";
    }

    /**Créer une configuration de la base de données selon les variables mises en entrées
     * @param dbpath Le chemin vers la base de données
     * @param pageSize La taille des pages de données
     * @param dm_maxfilecount Le nombre maximum de fichiers Datax.bin
     * @throws Exception
     */
    public DBConfig(String dbpath, int pageSize, int dm_maxfilecount) throws Exception{
        this(dbpath);
        if(pageSize<=0){
            throw new Exception("On ne peut pas avoir une page de taille nulle ou négative");
        }
        this.pageSize = pageSize;
        if(dm_maxfilecount<=0){
            throw new Exception("On ne peut pas avoir un nombre de fichiers négatif ou nul");
        }
        this.dm_maxfilecount = dm_maxfilecount;
    }

    /**
     * @param dbpath
     * @param pageSize
     * @param dm_maxfilecount
     * @param bm_buffercount
     * @param bm_policy
     * @throws Exception
     */
    public DBConfig(String dbpath, int pageSize, int dm_maxfilecount, int bm_buffercount, String bm_policy) throws Exception{
        this(dbpath, pageSize, dm_maxfilecount);
        if(bm_buffercount <= 0){
            throw new Exception("On ne peut pas avoir un nombre de buffer de 0 ou négatif");
        }
        this.bm_buffercount = bm_buffercount;
        this.bm_policy = bm_policy;
        if(!(this.bm_policy.equals("LRU")||(this.bm_policy.equals("MRU")))){
            throw new Exception("La politique de remplacement doit être LRU ou MRU");
        }
    }

    /**Créer une configuaration par défaut */
    public DBConfig(){
        DBConfig defaultConfig = DBConfig.loadDBConfig("config"+File.separator+"dbconfig.properties"); 
        this.dbpath = defaultConfig.dbpath;
        this.pageSize = defaultConfig.pageSize;
        this.dm_maxfilecount = defaultConfig.dm_maxfilecount;
        this.bm_buffercount = defaultConfig.bm_buffercount;
        this.bm_policy = defaultConfig.bm_policy;

    }

    /**
     * @return Le chemin vers la base de données
     */
    public String getDbpath() {
        return dbpath;
    }

    /**
     * @return La taille des pages de données
    */
    public int getPageSize(){
        return this.pageSize;
    }

    /**
     * @return Le nombre maximum de fichiers Datax.bin
     */
    public int getDm_maxfilecount(){
        return this.dm_maxfilecount;
    }

    /**
     * @return Le nombre maximum de buffers
     */
    public int getBm_buffercount(){
        return this.bm_buffercount;
    }

    /**
     * @return La politique de remplacement utilisée
     */
    public String getBm_policy(){
        return this.bm_policy;
    }

    /**
     * Charge la configuration de la base de données depuis un fichier de propriétés.
     * @param fichierConfig Chemin du fichier de configuration
     * @return Un objet DBConfig initialisé avec les valeurs du fichier, ou null en cas d'erreur
     */
    public static DBConfig loadDBConfig(String fichierConfig) {
        Properties props = new Properties();
        DBConfig config = null;
        try {
            FileInputStream fis = new FileInputStream(fichierConfig);
            props.load(fis);
            fis.close();
            String dbpath = props.getProperty("dbpath");
            int pageSize = Integer.parseInt(props.getProperty("pageSize"));
            int dm_maxfilecount = Integer.parseInt(props.getProperty("dm_maxfilecount"));
            int bm_buffercount = Integer.parseInt(props.getProperty("bm_buffercount"));
            String bm_policy = props.getProperty("bm_policy");
            config = new DBConfig(dbpath, pageSize, dm_maxfilecount, bm_buffercount, bm_policy);

        } catch (Exception e) {
            System.out.println("Erreur lecture config: " + e.getMessage());
        }
        return config;
    }

    /**
     * Retourne une représentation textuelle de la configuration.
     * @return Chaîne représentant la configuration
     */
    @Override
    public String toString() {
        return "DBConfig{" +"dbpath='" + this.dbpath + File.separator + ", pageSize=" + this.pageSize +", dm_maxfilecount=" + this.dm_maxfilecount + ", bm_buffercount=" + this.bm_buffercount + ", bm_policy =" + this.bm_policy + '}';
    }
}
