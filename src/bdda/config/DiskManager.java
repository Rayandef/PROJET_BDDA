package bdda.config;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.BufferedReader;
import bdda.config.PageID;

/** Classe représentant l'organisation du disque 
 * @author !Jordan, Rayan, !Anne-Louis
 * @version 1.0
*/
public class DiskManager {
    /** Variable membre du disque */
    private DBConfig dbConfig;

    /** Liste des pages libres */
    private List<PageID> pagesLibres;

    /**Crée un DiskManager
     * @author !Jordan, !Rayan
     * @version !1.0
     * @param dbConfig stocke en variable membre un pointeur
     */
    public DiskManager(DBConfig dbConfig){
        this.dbConfig = dbConfig;
    }

    /** Alloue une page 
     * @author !Jordan, !Anne-Louis
     * @version !1.0
     * @param pageID une page à allouer
     * @return la page allouée
    */
    public PageID allocPage(){
        try {
            // vérifie s'il existe une page libre
            if(!pagesLibres.isEmpty()){
                return pagesLibres.removeLast();
            }

            // récupère la liste des fichiers existants
            File dataDir = new File(dbConfig.getDbpath());
            File[] files = dataDir.listFiles((dir, name) -> name.startsWith("Data") && name.endsWith(".bin"));
            

            int fileIdx;
            File destinationFile;
            // si aucun fichier -> création Data0.bin
            if (files == null || files.length == 0) {
                fileIdx = 0;
                destinationFile = new File(dataDir, "Data0.bin");
                destinationFile.createNewFile();
            } else {
                // sinon, prendre le dernier fichier existant
                fileIdx = files.length - 1;
                destinationFile = new File(dataDir, "Data" + fileIdx + ".bin");
            }


            long pageSize = dbConfig.getPageSize();
            long nbPages = destinationFile.length() / pageSize;
            // si fichier rempli -> création nouveau fichier
            if (nbPages >= dbConfig.getDm_maxfilecount()) { 
                fileIdx++;
                // vérifie si le nombre max de fichier est atteint
                if (fileIdx >= dbConfig.getDm_maxfilecount()) {
                    System.err.println("Nombre maximal de fichiers atteint !");
                    return null;
                }
                destinationFile = new File(dataDir, "Data" + fileIdx + ".bin");
                destinationFile.createNewFile();
                nbPages = 0;
            }

            // création page
            try (RandomAccessFile raf = new RandomAccessFile(destinationFile, "rw")) {
                raf.seek(destinationFile.length());
                byte[] emptyPage = new byte[(int) pageSize];
                raf.write(emptyPage);
            }

            return new PageID(fileIdx, (int) nbPages) ;

         } catch (IOException e) {
            System.err.println("Erreur lors de l'allocation d'une page : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /** Lis une page et la copie dans le buffer
     * @author !Jordan
     * @version !1.0
     * @param pageID une page à allouer
     * @param buff une buffer
    */
    public void readPage(PageID pageID, ByteBuffer buff){
        try{
            BufferedReader br = new BufferedReader(new FileReader("Data" + pageID.getPageIdx()));
            String verifLu;
            while((verifLu = br.readLine()) != null){
                buff.asCharBuffer().put(br.readLine());
            }
            br.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e2){
            e2.printStackTrace();
        }
    }

    /** Ecris dans une page 
     * @author !Jordan, !Anne-Louis
     * @version !1.0
     * @param pageID une page à allouer
     * @param buff une buffer
     * @return la page allouée
    */
    public void writePage(PageID pageID, ByteBuffer buff){
        try {
            File dataDir = new File(dbConfig.getDbpath());
            File destinationFile = new File(dataDir, "Data" + pageID.getFileIdx() + ".bin") ;

            try (FileChannel channel = FileChannel.open(destinationFile.toPath(), StandardOpenOption.WRITE)) {
                long offset = pageID.getPageIdx() * dbConfig.getPageSize();
                channel.position(offset);
                channel.write(buff);
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture de la page : " + e.getLocalizedMessage());
            e.getStackTrace() ;
        }
    }

    /** De-Alloue une page 
     * @author !Jordan, !Anne-Louis
     * @version !1.0
     * @param pageID une page à désallouer
    */
    public void deAllocPage(PageID pageID){
        try{
            pagesLibres.add(pageID) ;
        } catch (Exception e){
            System.out.println("Erreur lors de la désallocation d'une page : " + e.getMessage()) ;
            e.printStackTrace();
        }
    }
    /**
     * Gère les opérations d'initialisations
     * @author !Jordan, Rayan
     * @version !1.0
     */
    public void init(){
        pagesLibres = new ArrayList<>();
        try {
            File dataDir = new File(dbConfig.getDbpath());
            if (!dataDir.exists()) {
                if (dataDir.mkdirs()) {
                    System.out.println("Dossier " + dataDir.getAbsolutePath() + " créé.");
                } else {
                    System.out.println("Impossible de créer le dossier " + dataDir.getAbsolutePath());
                }
            } else {
                System.out.println("Dossier déjà existant : " + dataDir.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'initialisation du DiskManager : " + e.getMessage());
        }
    }

    /**
     * Gère les opérations de sauvegarde
     * @author !Jordan, Rayan
     * @version !1.0
     */
    public void finish(){
        try {
            File saveFile = new File(dbConfig.getDbpath(), "freepages.txt");
            FileWriter fw = new FileWriter(saveFile);
            for (PageID p : pagesLibres) {
                fw.write(p.getFileIdx() + "," + p.getPageIdx() + "\n");
            }
            fw.close();
            System.out.println("Pages libres sauvegardées dans " + saveFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde du DiskManager : " + e.getMessage());
        }
    }
}
