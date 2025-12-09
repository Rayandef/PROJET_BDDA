package bdda.config;

import java.util.ArrayList;
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
}
