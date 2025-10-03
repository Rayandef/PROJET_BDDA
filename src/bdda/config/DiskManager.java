package bdda.config;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import bdda.config.PageID;

/** Classe représentant l'organisation du disque 
 * @author !Jordan
 * @version 1.0
*/
public class DiskManager {
    /** Variable membre du disque */
    private DBConfig dbConfig;

    /** Liste des pages libres */
    private List<PageID> pagesLibres;

    /**Crée un DiskManager
     * @author !Jordan
     * @version !1.0
     * @param dbConfig stocke en variable membre un pointeur
     */
    public DiskManager(DBConfig dbConfig){
        this.dbConfig = dbConfig;
    }

    /** Alloue une page 
     * @author !Jordan
     * @version !1.0
     * @param pageID une page à allouer
     * @return la page allouée
    */
    public PageID AllocPage(PageID pageID){
        if(pagesLibres.contains(pageID)){
            pagesLibres.remove(pageID);
            return pageID;
        }
        // à compléter, je ne comprends pas l'algorithme
        return new PageID();
    }

    /** Lis une page et la copie dans le buffer
     * @author !Jordan
     * @version !1.0
     * @param pageID une page à allouer
     * @param buff une buffer
    */
    public void ReadPage(PageID pageID, ByteBuffer buff){
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
     * @author !Jordan
     * @version !1.0
     * @param pageID une page à allouer
     * @param buff une buffer
     * @return la page allouée
    */
    public void WritePage(PageID pageID, ByteBuffer buff){
    }

    /** De-Alloue une page 
     * @author !Jordan
     * @version !1.0
     * @param pageID une page à allouer
    */
    public void DeAllocPage(PageID pageID){

    }
    /**
     * Gère les opérations d'initialisations
     * @author !Jordan
     * @version !1.0
     */
    public void init(){
        pagesLibres = new ArrayList<>();
    }

    /**
     * Gère les opérations de sauvegarde
     * @author !Jordan
     * @version !1.0
     */
    public void finish(){

    }
}
