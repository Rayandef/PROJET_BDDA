package bdda.test;

import bdda.config.DBConfig;
import bdda.config.DiskManager;
import bdda.config.PageID;
import java.io.File;
import java.nio.ByteBuffer;

/**
 * Classe de tests pour DiskManager 
 * @author Rayan, Anne-Louis
 * @version 1.0
 */
public class DiskManagerTests {

    // Test init
    public static void testInit(DBConfig conf, DiskManager dm) {
        System.out.println("===== Test Init DiskManager =====");

        dm.init();
        File dataDir = new File(conf.getDbpath());
        if (dataDir.exists() && dataDir.isDirectory()) {
            System.out.println("Test Init OK : dossier créé/existant -> " + dataDir.getAbsolutePath());
        } else {
            System.out.println("Test Init ECHEC : dossier non créé !");
        }

        System.out.println("===== Fin Test Init DiskManager =====");
    }

    // Test finish
    public static void testFinish(DBConfig conf, DiskManager dm){
        System.out.println("===== Test Finish DiskManager =====");

        dm.finish();
        File saveFile = new File(conf.getDbpath(), "freepages.txt");
        if (saveFile.exists()) {
            System.out.println("Test Finish OK : fichier de sauvegarde créé -> " + saveFile.getAbsolutePath());
        } else {
            System.out.println("Test Finish ECHEC : fichier freepages.txt manquant !");
        }

        System.out.println("===== Fin Test Finish DiskManager =====");
    }

    public static void testAllocPage(DBConfig conf, DiskManager dm) {
        System.out.println("===== Test allocPage DiskManager =====");
        PageID pageAlloue = dm.allocPage() ;
        if (pageAlloue == null) {
            System.out.println("Test AllocPage ECHEC : page non alloué !");
        } else {
            System.out.println("Test AllocPage OK : page alloué ! ") ;
        }
        System.out.println("===== Fin Test allocPage DiskManager =====");
    }

    public static void testDeAllocPage(DBConfig conf, DiskManager dm, PageID pageId){
        System.out.println("===== Test DeAllocPage DiskManager =====");
        dm.deAllocPage(pageId);
        System.out.println("===== Fin Test DeAllocPage DiskManager =====");
    }

    public static void testWriteAndReadPage(DBConfig conf, DiskManager dm, PageID pageId){
        System.out.println("===== Test WriteAndReadPage DiskManager =====");
        ByteBuffer buff = ByteBuffer.wrap(new byte[]{1,2,3});
        ByteBuffer buff2 = ByteBuffer.allocate(5) ;
        dm.writePage(pageId, buff);
        dm.readPage(pageId, buff2);
        if (!buff2.hasRemaining()){
            System.out.println("Test Ecriture ECHEC : la page est vide") ;
        } else {
            System.out.println("Test Ecriture OK : la page est remplie") ;
        } 
        while (buff2.hasRemaining()){
            System.out.println(buff2.get());
        }
        System.out.println("===== Fin Test WriteAndReadPage DiskManager =====");
    }

    public static void main(String[] args) {
        DBConfig conf = new DBConfig(); // utilise le chemin par défaut ./DB/binData
        DiskManager dm = new DiskManager(conf);
        testInit(conf, dm);

        PageID pageTest = dm.allocPage() ;

        testWriteAndReadPage(conf, dm, pageTest);
        testDeAllocPage(conf, dm, pageTest);
        testFinish(conf, dm);
    }
}
