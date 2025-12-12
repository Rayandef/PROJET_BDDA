package bdda.config;

import java.lang.reflect.Array;
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


    //Méthode qui vérifie si une commande contient un alias
    private boolean contientAlias(String command){
        //Séparer la commande en éléments
        String[] elements = command.split(" ");
        //parcourir les éléments et vérifier si l'un d'eux est un nom de table existant
        for(String element : elements){
            if(dbManager.getTables().containsKey(element)){
                return true; // un alias est présent
            }
        }
        return false;
    }

    //méthode qui gère les alias dans une commande et retourne une map des alias contenant le nom de l'alias et la relation correspondante
    private HashMap<String, Relation> extraireAlias(String commande){
        if(!contientAlias(commande)){
            return null; //pas d'alias
        }
        HashMap<String, Relation> aliasMap = new HashMap<>();
        String[] elements = commande.split(" ");//On sépare la commande en éléments
        for(int i=0; i<elements.length-1; i++){ //on parcourt les éléments
            String t1 = elements[i];
            String t2 = elements[i+1];
            if(dbManager.getTable(t1)!=null){ //si l'élément est un nom de table existant
                aliasMap.put(t2, dbManager.getTable(t1)); //on ajoute l'alias et la relation correspondante à la map
            }
        }
        return aliasMap;
    }


    //Méthode qui vérifie si une commande contient une condition
    private boolean contientCondition(String command){
        String[] elements = command.split(" ");
        for(String element : elements){
            if(element.equalsIgnoreCase("WHERE")){
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
            return null; //pas de conditions
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
                    Condition condition = new Condition(gauche, droite, op);
                    // Remplir l'objet condition avec les informations extraites
                    conditions.add(condition);
                    break;
                }
            }
        }
        return conditions;
    }

}
