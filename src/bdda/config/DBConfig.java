package bdda.config;

import java.io.FileInputStream;
import java.util.Properties;
    /** Classe permettant le chargement de la base de donnée
    */
public class DBConfig {
    /**dbpath -> l'emplacement de la base de données */
    private String dbpath;
    private int pageSize;
    private int dm_maxfilecount;

    public DBConfig(String dbpath, int pageSize, int dm_maxfilecount) throws Exception{
        this.dbpath = dbpath;
        if(dbpath!="./DB/binData"){
            throw new Exception("dbpath incorrect");
        }
        if(pageSize<=0){
            throw new Exception("On ne peut pas avoir une page de taille nulle");
        }
        this.pageSize = pageSize;
        if(dm_maxfilecount<=0){
            throw new Exception("On ne peut pas avec 0 fichiers max");
        }
        this.dm_maxfilecount = dm_maxfilecount;
    }

    public DBConfig(){
        this.dbpath = "./DB/binData";
        this.pageSize = 1;
        this.dm_maxfilecount = 1;
    }

    public String getDbpath() {
        return dbpath;
    }

    public int getPageSize(){
        return this.pageSize;
    }

    public int getDm_maxfilecount(){
        return this.dm_maxfilecount;
    }

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
            config = new DBConfig(dbpath, pageSize, dm_maxfilecount);

        } catch (Exception e) {
            System.out.println("Erreur lecture config: " + e.getMessage());
        }
        return config;
    }

    @Override
    public String toString() {
        return "DBConfig{dbpath='" + dbpath + "'}";
    }
}
