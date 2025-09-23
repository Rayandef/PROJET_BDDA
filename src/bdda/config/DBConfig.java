package bdda.config;

import java.io.FileInputStream;
import java.util.Properties;

public class DBConfig {
    private String dbpath;

    public DBConfig(String dbpath) {
        this.dbpath = dbpath;
    }

    public String getDbpath() {
        return dbpath;
    }

    public static DBConfig loadDBConfig(String fichierConfig) {
        Properties props = new Properties();
        DBConfig config = null;

        try {
            FileInputStream fis = new FileInputStream(fichierConfig);
            props.load(fis);
            fis.close();

            String dbpath = props.getProperty("dbpath");
            config = new DBConfig(dbpath);

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
