package bdda.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManager {

    private DBConfig config;
    private Map<String, Relation> tables;
    private DiskManager diskManager;
    private BufferManager bufferManager;

    public DBManager(DBConfig dbConfig, DiskManager diskManager, BufferManager bufferManager) {
        this.config = dbConfig;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.tables = new HashMap<>();
    }

    public DBManager(DBConfig dbConfig) {
        this(dbConfig, null, null);
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
     * Renvoie la table correspondant a nomTable.
     */
    public Relation GetTable(String nomTable) {
        if (nomTable == null) {
            return null;
        }
        return tables.get(nomTable);
    }

    /**
     * Supprime une table a partir de son nom.
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
     * Affiche le schema d'une table.
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
     * Affiche le schema de toutes les tables.
     */
    public void DescribeAllTables() {
        for (String tab : tables.keySet()) {
            DescribeTable(tab);
        }
    }

    /**
     * Sauvegarde de l'etat de la base.
     * Format simple: tableName|fileIdx:pageIdx|colName:colType,colName:colType
     * Fichier: database.save dans dbpath.
     */
    public void SaveState() {
        File dir = new File(config.getDbpath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File saveFile = new File(dir, "database.save");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            for (Relation rel : tables.values()) {
                PageID header = rel.getHeaderPageId();
                String headerStr = (header == null) ? "-1:-1" : header.getFileIdx() + ":" + header.getPageIdx();

                StringBuilder cols = new StringBuilder();
                List<InfoColonne<String, String>> infoCols = rel.getInfoColonne();
                for (int i = 0; i < infoCols.size(); i++) {
                    InfoColonne<String, String> c = infoCols.get(i);
                    cols.append(c.getNom()).append(":").append(c.getType());
                    if (i < infoCols.size() - 1) {
                        cols.append(",");
                    }
                }

                writer.write(rel.getNom() + "|" + headerStr + "|" + cols.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Chargement de l'etat de la base depuis database.save.
     */
    public void LoadState() {
        if (diskManager == null || bufferManager == null) {
            return;
        }

        File saveFile = new File(config.getDbpath(), "database.save");
        if (!saveFile.exists()) {
            return;
        }

        tables.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 3) {
                    continue;
                }

                String tableName = parts[0];
                String headerPart = parts[1];
                String colsPart = parts[2];

                String[] headerSplit = headerPart.split(":");
                if (headerSplit.length != 2) {
                    continue;
                }
                int fileIdx = Integer.parseInt(headerSplit[0]);
                int pageIdx = Integer.parseInt(headerSplit[1]);
                PageID headerId = new PageID(fileIdx, pageIdx);

                String[] colDefs = colsPart.split(",");
                List<InfoColonne<String, String>> cols = new ArrayList<>();
                for (String colDef : colDefs) {
                    if (colDef.isEmpty()) {
                        continue;
                    }
                    String[] colParts = colDef.split(":");
                    if (colParts.length != 2) {
                        continue;
                    }
                    InfoColonne<String, String> info = new InfoColonne<>();
                    info.setNom(colParts[0]);
                    info.setType(colParts[1]);
                    cols.add(info);
                }

                try {
                    Relation rel = new Relation(tableName, cols, headerId, diskManager, bufferManager);
                    tables.put(tableName, rel);
                } catch (Exception e) {
                    System.out.println("Erreur lors du chargement de la table " + tableName + " : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement : " + e.getMessage());
        }
    }

    public Map<String, Relation> getTables() {
        return tables;
    }
}
