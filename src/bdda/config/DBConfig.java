package bdda.config;

import java.io.FileInputStream;
import java.util.Properties;

public class DBConfig {
    private String dbpath;
    private int pageSize;
    private int dm_maxfilecount;

    public DBConfig(String dbpath, int pageSize, int dm_maxfilecount) {
        this.dbpath = dbpath;
        this.pageSize = pageSize;
        this.dm_maxfilecount = dm_maxfilecount;
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
        /* Charge les élément d'un fichier config*/
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
