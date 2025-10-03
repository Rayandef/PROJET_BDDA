package bdda.config;

import java.nio.ByteBuffer;

/** Classe représentant l'organisation du disque 
 * @author !Jordan
 * @version 1.0
*/
public class DiskManager {
    /**Crée un DiskManager
     * @author !Jordan
     * @version !1.0
     * @param dbConfig stocke en variable membre un pointeur
     */
    public DiskManager(DBConfig dbConfig){

    }

    /** Alloue une page 
     * @author !Jordan
     * @version !1.0
     * @param pageID une page à allouer
     * @return la page allouée
    */
    public PageID AllocPage(PageID pageID){

        return new PageID();
    }

    /** Lis une page 
     * @author !Jordan
     * @version !1.0
     * @param pageID une page à allouer
     * @param buff une buffer
    */
    public void ReadPage(PageID pageID, ByteBuffer buff){

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
    public void WritePage(PageID pageID){

    }
    /**
     * Gère les opérations d'initialisations
     * @author !Jordan
     * @version !1.0
     */
    public void init(){

    }

    /**
     * Gère les opérations de sauvegarde
     * @author !Jordan
     * @version !1.0
     */
    public void finish(){

    }
}
