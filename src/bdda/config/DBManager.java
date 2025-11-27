package bdda.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManager {

    private DBConfig config;
    private Map<String, Relation> tables;

    public DBManager(DBConfig dbConfig){
        this.config = dbConfig;
        this.tables = new HashMap<>();   
    
    }

    /**
     * Ajoute une table dans la base
     */
    public void AddTable(Relation tab) {
        tables.put(tab.getNom(), tab);
    }


    /**
     * Renvoie la table correspondant à nomTable
     */
    public Relation getTable(String nomTab){
        return tables.get(nomTab);
    }

    /**
     * Supprime une table à partir de son nom
     */
    public void removeTable(String nomTable){
        
    }

    /**
     * Supprime toutes les tables de la base
     */
    public void removeAllTable(){
        for(String tab : this.tables.keySet()){
            removeTable(tab);
        }
    }

    /**
     * Affiche le schéma d’une table
     */
    public void describeTable(String nomTable){

    }

    /**
     * Affiche le schéma de toutes les tables
     */
    public void describeAllTable(){
        for(String tab: this.tables.keySet()){
            removeTable(tab);
        }
    }

    /**
     * Sauvegarde de l’état de la base
     */
    public void saveState(){

    }

    /**
     * Chargement de l’état de la base
     */
    public void loadState(){

    }

    public Map<String, Relation> getTables() {
        return tables;
    }

}
