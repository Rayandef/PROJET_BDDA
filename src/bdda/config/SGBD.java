package bdda.config;

import java.util.ArrayList;
import java.util.HashMap;
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
            String command = sc.nextLine().trim();

            if (command.equalsIgnoreCase("EXIT")) {
                ProcessExitCommand();
                break;
            }
            else if (command.startsWith("CREATE TABLE")) {
                ProcessCreateTableCommand(command);
            }
            else if (command.startsWith("DROP TABLES")) {
                ProcessDropAllTablesCommand();
            }
            else if (command.startsWith("DROP TABLE")) {
                ProcessDropTableCommand(command);
            }
            else if (command.startsWith("DESCRIBE TABLES")) {
                ProcessDescribeAllTablesCommand();
            }
            else if (command.startsWith("DESCRIBE TABLE")) {
                ProcessDescribeTableCommand(command);
            }
            else if(command.startsWith("SELECT")) {
                ProcessSelectCommand(command);
            }
            else {
                System.out.println("Commande inconnue : " + command);
            }
        }

        sc.close();
    }


    private void ProcessExitCommand() {
        dbManager.saveState();
        bufferManager.FlushBuffers();
        diskManager.finish();
        System.out.println("EXIT");
    }


    private void ProcessCreateTableCommand(String cmd) {
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
                col.setType(parts[1].split("\\(")[0]);
                Size.valueOf(col.getType()).setTaille(Integer.parseInt(parts[1].split("\\(")[1].split("\\)")[0]));
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


    private void ProcessDropTableCommand(String cmd) {
        String nom = cmd.replace("DROP TABLE", "").trim();

        Relation rel = dbManager.getTable(nom);
        if (rel == null) return;

        for (PageID p : rel.getAllPages()) {
            diskManager.deAllocPage(p);
        }

        dbManager.removeTable(nom);

        System.out.println("TABLE " + nom + " DROPPED");
    }


    private void ProcessDropAllTablesCommand() {

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


    private void ProcessDescribeTableCommand(String cmd) {
        String nom = cmd.replace("DESCRIBE TABLE", "").trim();
        dbManager.describeTable(nom);
    }


    private void ProcessDescribeAllTablesCommand() {
        dbManager.describeAllTables();
    }

    private void ProcessSelectCommand(String cmd) {
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
}
