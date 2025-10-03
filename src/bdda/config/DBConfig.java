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

    /**Créer une configuration de la base de données selon les variables mises en entrées
     * @param dbpath Le chemin vers la base de données
     * @param pageSize La taille des pages de données
     * @param dm_maxfilecount Le nombre maximum de fichiers Datax.bin
     */
    public DBConfig(String dbpath, int pageSize, int dm_maxfilecount) throws Exception{
        this.dbpath = dbpath;
        if(!dbpath.equals("." + File.separator + "DB" + File.separator + "binData")){
            throw new Exception("dbpath incorrect");
        }
        if(pageSize<=0){
            throw new Exception("On ne peut pas avoir une page de taille nulle ou négative");
        }
        this.pageSize = pageSize;
        if(dm_maxfilecount<=0){
            throw new Exception("On ne peut pas avoir un nombre de fichiers négatif ou nul");
        }
        this.dm_maxfilecount = dm_maxfilecount;
    }

    /**Créer une configuaration par défaut */
    public DBConfig(){
        this.dbpath = "."+ File.separator+"DB"+File.separator+"binData";
        this.pageSize = 1;
        this.dm_maxfilecount = 1;
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
            config = new DBConfig(dbpath, pageSize, dm_maxfilecount);

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
        return "DBConfig{" +"dbpath='" + dbpath + '\'' + ", pageSize=" + pageSize +", dm_maxfilecount=" + dm_maxfilecount +'}';
    }
}
