package bdda.config;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

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
                return pagesLibres.remove(pagesLibres.size() - 1);
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
            if (destinationFile.length() >= 32000 ) { // revoir cette condition car inconnu sur la taille max d'un fichier
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
            File dataDir = new File(dbConfig.getDbpath());
            File destinationFile = new File(dataDir, "Data" + pageID.getFileIdx() + ".bin") ; // récupère le fichier correspondant

            try (FileChannel channel = FileChannel.open(destinationFile.toPath(), StandardOpenOption.READ)) {
                long offset = pageID.getPageIdx() * dbConfig.getPageSize();
                channel.position(offset); // récupère la page à lire
                buff.clear() ;
                channel.read(buff); // le buffer est rempli avec la page
                buff.flip();
            }
        }catch(IOException e){
            System.out.println("Erreur lors de la lecture de la page : " + e) ;
            e.printStackTrace();
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
            File destinationFile = new File(dataDir, "Data" + pageID.getFileIdx() + ".bin") ; // récupère le fichier correspondant

            try (FileChannel channel = FileChannel.open(destinationFile.toPath(), StandardOpenOption.WRITE)) {
                long offset = pageID.getPageIdx() * dbConfig.getPageSize(); 
                channel.position(offset); // récupère la page pour écrire
                buff.rewind() ;
                channel.write(buff); // rempli la page avec le buffer
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture de la page : " + e.getLocalizedMessage());
            e.printStackTrace() ;
        }
    }

    /** De-Alloue une page 
     * @author !Jordan, !Anne-Louis
     * @version !1.0
     * @param pageID une page à désallouer
    */
    public void deAllocPage(PageID pageID){
        try{
            pagesLibres.add(pageID) ; // rajoute la page dans la liste des pages libres
        } catch (Exception e){
            System.out.println("Erreur lors de la désallocation d'une page : " + e.getMessage()) ;
            e.printStackTrace();
        }
    }

    /** Récupère la liste de pages libres
     * @author !Anne-Louis
     * @version !1.0
    */
    public List<PageID> lirePageLibres(){
        File freepages = new File(dbConfig.getDbpath(), "freepages.txt") ;
        pagesLibres = new ArrayList<>() ;
        if (freepages.exists()){
            try (BufferedReader br = new BufferedReader(new FileReader(dbConfig.getDbpath() + File.separator + "freepages.txt"))) {
                String ligne;
                while ((ligne = br.readLine()) != null) {
                    // On enlève les espaces parasites
                    ligne = ligne.trim();
                    if (ligne.isEmpty()) continue; // ignore les lignes vides

                    // On sépare les 2 valeurs
                    String[] parts = ligne.split(",");
                    if (parts.length == 2) {
                        int fileIdx = Integer.parseInt(parts[0].trim());
                        int pageIdx = Integer.parseInt(parts[1].trim());
                        pagesLibres.add(new PageID(fileIdx, pageIdx));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return pagesLibres ;
    }

    /**
     * Gère les opérations d'initialisations
     * @author !Jordan, Rayan
     * @version !1.0
     */
    public void init(){
        pagesLibres = lirePageLibres();
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
