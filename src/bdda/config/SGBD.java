package bdda.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import bdda.config.Relation.Size;


public class SGBD {

    private DBConfig config;
    private DiskManager diskManager;
    private BufferManager bufferManager;
    private DBManager dbManager;

    public SGBD(DBConfig config) {
        this.config = config;
        this.diskManager = new DiskManager(config);
        this.bufferManager = new BufferManager(config, diskManager);
        this.dbManager = new DBManager(config, diskManager, bufferManager);

        diskManager.init();
        bufferManager.init();

        dbManager.loadState();
    }

    public void Run() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            try {
                String command = sc.nextLine().trim();

                if (command.equalsIgnoreCase("EXIT")) {
                    processExitCommand();
                    break;
                }
                else if (command.startsWith("CREATE TABLE")) {
                    processCreateTableCommand(command);
                }
                else if (command.startsWith("DROP TABLES")) {
                    processDropAllTablesCommand();
                }
                else if (command.startsWith("DROP TABLE")) {
                    processDropTableCommand(command);
                }
                else if (command.startsWith("DESCRIBE TABLES")) {
                    processDescribeAllTablesCommand();
                }
                else if (command.startsWith("DESCRIBE TABLE")) {
                    processDescribeTableCommand(command);
                }
                else if(command.startsWith("SELECT")) {
                    processSelectCommand(command);
                }
                else if(command.startsWith("INSERT INTO")){
                    processInsertIntoCommand(command);
                }
                else if(command.startsWith("APPEND INTO")){
                    processAppendIntoCommand(command);
                }
                else if(command.startsWith("DELETE")){
                    processDeleteCommand(command);
                }
                else if(command.startsWith("UPDATE")){
                    processUpdateCommand(command);
                }
                else {
                    System.out.println("Commande inconnue : " + command);
                }
            } catch (Exception e){
                System.out.println(e);
            }
        }

        sc.close();
    }


    private void processExitCommand() {
        dbManager.saveState();
        bufferManager.FlushBuffers();
        diskManager.finish();
        System.out.println("EXIT");
    }


    private void processCreateTableCommand(String cmd) {
        try {
            String after = cmd.replace("CREATE TABLE", "").trim();
            String tableName = after.substring(0, after.indexOf("(")).trim();
            String inside = after.substring(after.indexOf("(") + 1, after.lastIndexOf(")"));
            String[] fields = inside.split(",");

            ArrayList<InfoColonne<String, String>> colonnes = new ArrayList<>();

            for (String f : fields) {
                String[] parts = f.split(":");
                InfoColonne<String, String> col = new InfoColonne<>();
                col.setNom(parts[0]);                
                switch(parts[1]){
                    case "FLOAT", "INT":
                    col.setType(parts[1]);
                    break;

                    default:
                    col.setType(parts[1].split("\\(")[0]);
                    Size.valueOf(col.getType()).setTaille(Integer.parseInt(parts[1].split("\\(")[1].split("\\)")[0]));
                }
                colonnes.add(col);
            }

            PageID header = bufferManager.allocPage();

            Relation rel = new Relation(tableName, colonnes, header, diskManager, bufferManager);
            rel.initHeaderPage(header);

            dbManager.addTable(rel);

            System.out.println("TABLE " + tableName + " CREATED");

        } catch (Exception e) {
            System.out.println("Erreur CREATE TABLE : " + e.getMessage());
        }
    }


    private void processDropTableCommand(String cmd) {
        String nom = cmd.replace("DROP TABLE", "").trim();

        Relation rel = dbManager.getTable(nom);
        if (rel == null) return;

        for (PageID p : rel.getAllPages()) {
            diskManager.deAllocPage(p);
        }

        dbManager.removeTable(nom);

        System.out.println("TABLE " + nom + " DROPPED");
    }


    private void processDropAllTablesCommand() {

        for (String nom : new ArrayList<>(dbManager.getTables().keySet())) {
            Relation rel = dbManager.getTable(nom);
            if (rel != null) {
                for (PageID p : rel.getAllPages()) {
                    diskManager.deAllocPage(p);
                }
            }
        }

        dbManager.removeAllTables();

        System.out.println("ALL TABLES DROPPED");
    }


    private void processDescribeTableCommand(String cmd) {
        String nom = cmd.replace("DESCRIBE TABLE", "").trim();
        dbManager.describeTable(nom);
    }


    private void processDescribeAllTablesCommand() {
        dbManager.describeAllTables();
    }

    private void processSelectCommand(String cmd) {
        try {
            // 1. Récupérer les relations et les alias
            HashMap<String, Relation> aliasMap = extraireAlias(cmd);
            if (aliasMap.isEmpty()) {
                System.out.println("Aucune table trouvée pour la commande SELECT");
                return;
            }

            // On prend la première relation (pour l'instant, pas de jointure)
            Relation rel = aliasMap.values().iterator().next();

            // 2. Créer le scanner pour lire les tuples de la relation
            IRecordIterator scanner = new RelationScanner(rel);

            // 3. Récupérer les conditions de filtrage
            ArrayList<Condition> conditions = extraireConditions(cmd);

            // 4. Appliquer le SelectOperator si des conditions existent
            IRecordIterator selectOp = scanner;
            if (!conditions.isEmpty()) {
                selectOp = new SelectOperator(scanner, conditions, aliasMap);
            }

            // 5. Récupérer les colonnes à projeter
            ArrayList<String> colonnesSelect = extraireColonnesSelect(cmd);

            // 6. Appliquer le ProjectOperator si une projection est demandée
            IRecordIterator projectOp = selectOp;
            if (!colonnesSelect.isEmpty()) {
                projectOp = new ProjectOperator(selectOp, colonnesSelect, aliasMap);
            }

            // 7. Afficher les tuples récupérés
            RecordPrinter printer = new RecordPrinter(projectOp);
            printer.printAllRecords();

            System.out.println("---");

        } catch (Exception e) {
            System.out.println("Erreur lors de l'exécution du SELECT : " + e.getMessage());
            e.printStackTrace();
        }
    }


    //méthode qui gère les alias dans une commande et retourne une map des alias contenant le nom de l'alias et la relation correspondante
    private HashMap<String, Relation> extraireAlias(String commande) {

        HashMap<String, Relation> aliasMap = new HashMap<>();

        // On isole la partie FROM ... (jusqu'à WHERE s'il existe)
        String afterFrom = commande.split("FROM")[1].trim();

        if (afterFrom.contains("WHERE")) {
            afterFrom = afterFrom.split("WHERE")[0].trim();
        }

        // Découpe par espaces
        String[] tokens = afterFrom.split(" ");

        // Cas 1 : FROM Table
        if (tokens.length == 1) {
            Relation rel = dbManager.getTable(tokens[0]);
            aliasMap.put(tokens[0], rel);
            return aliasMap;
        }
        // Cas 2 : FROM Table Alias
        String tableName = tokens[0];
        String alias = tokens[1];

        Relation rel = dbManager.getTable(tableName);
        aliasMap.put(alias, rel);

        return aliasMap;
    }


    //Méthode qui vérifie si une commande contient une condition
    private boolean contientCondition(String command){
        String[] elements = command.split(" ");
        for(String element : elements){
            if(element.equals("WHERE")){
                return true; // une condition est présente
            }
        }
        return false;
    }

    //Méthode qui extrait les conditions d'une commande et les retourne sous forme d'une liste d'objets Condition
    private ArrayList<Condition> extraireConditions(String command){
        ArrayList<Condition> conditions = new ArrayList<>();
        final String[] OPERATEURS = {"<=", ">=", "<>", "=", "<", ">"};
        if(!contientCondition(command)){
            return conditions; //pas de conditions
        }
        String wherePart = command.split("WHERE")[1].trim(); //on récupère la partie après WHERE
        String[] conds = wherePart.split("AND"); //on sépare les conditions par AND
        for(String cond : conds){
            cond = cond.trim();
            for(String op : OPERATEURS){
                if(cond.contains(op)){
                    String[] parts = cond.split(op);
                    String gauche = parts[0].trim();
                    String droite = parts[1].trim();
                    Condition condition = new Condition(gauche, op, droite);
                    // Remplir l'objet condition avec les informations extraites
                    conditions.add(condition);
                    break;
                }
            }
        }
        return conditions;
    }

    private ArrayList<String> extraireColonnesSelect(String cmd) {

        ArrayList<String> colonnes = new ArrayList<>();

        String selectPart = cmd.split("FROM")[0].replace("SELECT", "").trim(); // partie entre SELECT et FROM
        // Si SELECT *
        if (selectPart.equals("*")) {
            return colonnes; // vide = toutes les colonnes
        }
        String[] cols = selectPart.split(","); //On sépare par virgule
        for (String c : cols) {
            colonnes.add(c.trim()); //on ajoute la colonne à la liste
        }

        return colonnes;
    }

    private void processInsertIntoCommand(String cmd){
        String[] command = cmd.split("\\s");
        Relation table = dbManager.getTable(command[2]);
        if (table == null){
            System.out.println("Aucune table trouvée pour la commande INSERT INTO");
            return ;
        }
        String[] valeurs = command[4].split("[,()]");
        List<String> valeurList = new ArrayList<>();
        for (String s : valeurs){
            if (!s.isEmpty()){
                valeurList.add(s);
            }
        }
        Record record = new Record(valeurList);
        if (table.estInserable(record)){
            table.insertRecord(record);
        }
    }

    private void processAppendIntoCommand(String cmd){
        String[] command = cmd.split("\\s");
        Relation table = dbManager.getTable(command[2]);
        if (table == null){
            System.out.println("Aucune table trouvée pour la commande INSERT INTO");
            return ;
        }

        String pathFileCSV = command[4].substring(command[4].indexOf("(") + 1, command[4].indexOf(")"));

        try (BufferedReader br = new BufferedReader(new FileReader(pathFileCSV))) {
            String line ;
            while ((line = br.readLine()) != null){
                String[] valeurs = line.split(",");
                List<String> valeurList = new ArrayList<>();
                for (String s : valeurs){
                    valeurList.add(s);
                }
                Record record = new Record(valeurList);
                if (table.estInserable(record)){
                    table.insertRecord(record);
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier .csv : " + e);
        }
    }

    private HashMap<String, Relation> extraireAliasDelete(String cmd) {

        HashMap<String, Relation> aliasMap = new HashMap<>();

        // DELETE Pomme c WHERE ...
        String apresDelete = cmd.replaceFirst("DELETE", "").trim();

        if (apresDelete.contains("WHERE")) {
            apresDelete = apresDelete.split("WHERE")[0].trim();
        }

        String[] tokens = apresDelete.split("\\s+");

        // DELETE Pomme
        if (tokens.length == 1) {
            Relation rel = dbManager.getTable(tokens[0]);
            aliasMap.put(tokens[0], rel);
            return aliasMap;
        }

        // DELETE Pomme c
        String tableName = tokens[0];
        String alias = tokens[1];

        Relation rel = dbManager.getTable(tableName);
        aliasMap.put(alias, rel);

        return aliasMap;
    }

    private HashMap<String, Relation> extraireAliasUpdate(String commande){
        HashMap<String, Relation> aliasMap = new HashMap<>();

        // On isole la partie SET ... (jusqu'à WHERE s'il existe)
        String afterFrom = commande.split("SET")[1].trim();

        if (afterFrom.contains("WHERE")) {
            afterFrom = afterFrom.split("WHERE")[0].trim();
        }

        // Découpe par espaces
        String[] tokens = afterFrom.split(" |\\.");

        //SET TABLE.valeur
        String tableName = tokens[0];
        String alias = tokens[1];

        Relation rel = dbManager.getTable(tableName);
        aliasMap.put(alias, rel);

        return aliasMap;
    }

    private void processUpdateCommand(String cmd){
        // 1. Alias
        HashMap<String, Relation> aliasMap = extraireAliasUpdate(cmd);
        Relation rel = aliasMap.values().iterator().next();
        // 2. Conditions
        ArrayList<Condition> conditions = extraireConditions(cmd);
        // 3. Scanner existant
        RelationScanner scanner = new RelationScanner(rel);

        int nbModifiees = 0;
        Record record;

        while ((record = scanner.getNextRecord()) != null) {

            boolean ok = true;
            for (Condition c : conditions) {
                if (!c.evaluerConditionSurRecord(record, aliasMap)) {
                    ok = false;
                    break;
                }
            }

            if (ok) {
                RecordId rid = scanner.getCurrentRecordId();
                rel.deleteRecord(rid);
                nbModifiees++;
            }
        }

        scanner.close();
        System.out.println(nbModifiees + " ligne(s) mis à jour.");
    }

    private void processDeleteCommand(String cmd) {
        // 1. Alias
        HashMap<String, Relation> aliasMap = extraireAliasDelete(cmd);
        Relation rel = aliasMap.values().iterator().next();
        // 2. Conditions
        ArrayList<Condition> conditions = extraireConditions(cmd);
        // 3. Scanner existant
        RelationScanner scanner = new RelationScanner(rel);

        int nbSupprimes = 0;
        Record record;

        while ((record = scanner.getNextRecord()) != null) {

            boolean ok = true;
            for (Condition c : conditions) {
                if (!c.evaluerConditionSurRecord(record, aliasMap)) {
                    ok = false;
                    break;
                }
            }

            if (ok) {
                RecordId rid = scanner.getCurrentRecordId();
                rel.deleteRecord(rid);
                nbSupprimes++;
            }
        }

        scanner.close();
        System.out.println(nbSupprimes + " ligne(s) supprimée(s)");
    }


}
