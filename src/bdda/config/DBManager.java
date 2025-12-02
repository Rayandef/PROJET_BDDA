package bdda.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManager {

    private DBConfig config;
    private Map<String, Relation> tables;

    public DBManager(DBConfig dbConfig) {
        this.config = dbConfig;
        this.tables = new HashMap<>();
    }

    /**
     * Ajoute une table dans la base.
     */
    public void AddTable(Relation tab) {
        if (tab == null || tab.getNom() == null) {
            return;
        }
        tables.put(tab.getNom(), tab);
    }

    /**
     * Renvoie la table correspondant à nomTable.
     */
    public Relation GetTable(String nomTable) {
        if (nomTable == null) {
            return null;
        }
        return tables.get(nomTable);
    }

    /**
     * Supprime une table à partir de son nom.
     */
    public void RemoveTable(String nomTable) {
        if (nomTable == null) {
            return;
        }
        tables.remove(nomTable);
    }

    /**
     * Supprime toutes les tables de la base.
     */
    public void RemoveAllTables() {
        tables.clear();
    }

    /**
     * Affiche le schéma d'une table.
     */
    public void DescribeTable(String nomTable) {
        Relation relation = GetTable(nomTable);
        if (relation == null) {
            System.out.println("Table " + nomTable + " inexistante");
            return;
        }

        System.out.println("Table : " + relation.getNom());
        List<InfoColonne<String, String>> colonnes = relation.getInfoColonne();
        for (InfoColonne<String, String> info : colonnes) {
            System.out.println(" - " + info.getNom() + " (" + info.getType() + ")");
        }
    }

    /**
     * Affiche le schéma de toutes les tables.
     */
    public void DescribeAllTables() {
        for (String tab : tables.keySet()) {
            DescribeTable(tab);
        }
    }

    /**
     * Sauvegarde de l'état de la base.
     */
    public void saveState() {

    }

    /**
     * Chargement de l'état de la base.
     */
    public void loadState() {

    }

    public Map<String, Relation> getTables() {
        return tables;
    }
}
