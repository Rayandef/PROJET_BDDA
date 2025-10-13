package bdda.test;

import java.nio.ByteBuffer;
import java.io.File;

import bdda.config.DBConfig;
import bdda.config.DiskManager;
import bdda.config.BufferManager;
import bdda.config.PageID;

/**
 * Classe de tests pour BufferManager
 * @author Rayan
 * @version 1.0
 */
public class BufferManagerTests {

    public static void testInit(BufferManager bm) {
        System.out.println("Test Init BufferManager");
        try {
            bm.init();
            System.out.println("Init OK : BufferManager initialisé avec succès.");
        } catch (Exception e) {
            System.out.println("Init echec : " + e.getMessage());
        }
        System.out.println("Fin Test Init BufferManager");
    }

    public static void testGetAndFreePage(BufferManager bm, DiskManager dm) {
        System.out.println("Test GetPage et FreePage");
        try {
            PageID p = dm.allocPage();

            ByteBuffer buff = bm.getPage(p);
            if (buff != null) {
                System.out.println("GetPage OK : page récupérée dans un buffer.");
            } else {
                System.out.println("GetPage ECHEC : aucun buffer retourné.");
            }

            bm.FreePage(p, true);
            System.out.println("FreePage OK : page libérée et marquée dirty.");

        } catch (Exception e) {
            System.out.println("Test GetPage/FreePage ECHEC : " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println(" Fin Test GetPage et FreePage");
    }

    public static void testSetPolicy(BufferManager bm) {
        System.out.println("Test SetCurrentReplacementPolicy");
        try {
            bm.SetCurrentReplacementPolicy(BufferManager.ReplacementPolicy.MRU);
            System.out.println("Politique de remplacement changée avec succès -> MRU.");
            bm.SetCurrentReplacementPolicy(BufferManager.ReplacementPolicy.LRU);
            System.out.println("Politique de remplacement changée avec succès -> LRU.");
        } catch (Exception e) {
            System.out.println("SetCurrentReplacementPolicy ECHEC : " + e.getMessage());
        }
        System.out.println("Fin Test SetCurrentReplacementPolicy");
    }

    public static void testFlushBuffers(BufferManager bm) {
        System.out.println("Test FlushBuffers");
        try {
            bm.FlushBuffers();
            System.out.println("FlushBuffers OK : buffers vidés et pages dirty écrites sur disque.");
        } catch (Exception e) {
            System.out.println("FlushBuffers ECHEC : " + e.getMessage());
        }
        System.out.println("Fin Test FlushBuffers");
    }

    public static void testFinish(BufferManager bm) {
        System.out.println("Test Finish BufferManager");
        try {
            bm.finish();
            System.out.println("Finish OK : DiskManager.finish() appelé et buffers vidés.");
        } catch (Exception e) {
            System.out.println("Finish ECHEC : " + e.getMessage());
        }
        System.out.println("Fin Test Finish BufferManager");
    }

    public static void main(String[] args) {
        System.out.println("Démarrage des tests BufferManager");

        DBConfig conf = new DBConfig();
        DiskManager dm = new DiskManager(conf);
        BufferManager bm = new BufferManager(conf, dm);

        File dataDir = new File(conf.getDbpath());
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        testInit(bm);
        testSetPolicy(bm);
        testGetAndFreePage(bm, dm);
        testFlushBuffers(bm);
        testFinish(bm);

        System.out.println("Fin des tests BufferManager");
    }
}
