package bdda.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe correspondant à la relation entre une entité et une association"
 */
public class Relation {


    /** Nom de la relation */
    private String nom;

    /** nombre de colonnes */
    private int colonne;

    /** Info de chaque colonne */
    private List<InfoColonne<String, String>> infoColonne;

    /** Identifiant de la Header Page de la relation */
    private PageID headerPageId;

    /** Nombre de cases par page de données */
    private int nbCasesParPage;

    /** Référence vers le DiskManager */
    private DiskManager diskManager;

    /** Référence vers le BufferManager */
    private BufferManager bufferManager;

    /** Liste des pages ayant encore de la place */
    private List<PageID> pagesLibres = new ArrayList<>();

    /** Liste des pages pleines */
    private List<PageID> pagesPleines = new ArrayList<>();


    /** Liste des tailles des 4 types de colonnes */
    public enum Size{
        INT(4), FLOAT(4), CHAR(64), VARCHAR(64) ;

        private final int taille ;

        private Size(int taille){
            this.taille = taille ;
        }

        public int getTaille(){
            return this.taille ;
        }
    }

    /** Crée une relation 
     * @param nom
     * @param infoColonne
    */
    public Relation(String nom, List<InfoColonne<String, String>> infoColonne) throws Exception{
        this.nom = nom;
        for(InfoColonne<String, String> info : infoColonne){
            if (!(info.getType().equals("INT") ||info.getType().equals("FLOAT") || info.getType().equals("CHAR") || info.getType().equals("VARCHAR"))) {
                throw new Exception("L'élément " + (infoColonne.indexOf(info) + 1) + " de la liste est incorrect");
            } 
            this.infoColonne = infoColonne;
        }
        colonne = infoColonne.size();
    }

    public Relation(String nom, List<InfoColonne<String, String>> infoColonne, PageID headerPageId, int nbCasesParPage, DiskManager diskManager, BufferManager bufferManager, List<PageID> pagesLibres, List<PageID> pagesPleines) throws Exception {

    this.nom = nom;

    for (InfoColonne<String, String> info : infoColonne) {
        if (!(info.getType().equals("INT") ||
              info.getType().equals("FLOAT") ||
              info.getType().equals("CHAR") ||
              info.getType().equals("VARCHAR"))) {
            throw new Exception("L'élément " + (infoColonne.indexOf(info) + 1) + " est incorrect");
        }
    }

    this.infoColonne = infoColonne;
    this.colonne = infoColonne.size();

    this.headerPageId = headerPageId;
    this.nbCasesParPage = nbCasesParPage;
    this.diskManager = diskManager;
    this.bufferManager = bufferManager;
    this.pagesLibres = pagesLibres;
    this.pagesPleines = pagesPleines;
    }


    /**
     * Récupère le nombre de colonnes
     * @return le nombre de colonnes
     */
    public int getColonne() {
        return colonne;
    }

    /**
     * Récupère les infos de chaque colonne
     * @return les infos de chaque colonne
     */
    public List<InfoColonne<String, String>> getInfoColonne() {
        return infoColonne;
    }

    /**
     * Récupère le nom de la relation
     * @return le nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * Défini le nombre de colonne
     * @param colonne
     */
    void setColonne(int colonne) {
        this.colonne = colonne;
    }

    /**
     * Défini les infos des colonnes
     * @param infoColonne
     */
    public void setInfoColonne(List<InfoColonne<String, String>> infoColonne) throws Exception{
        for(InfoColonne<String, String> info : infoColonne){
            if(info.getType() != "INT" || info.getType() != "FLOAT" || info.getType() != "CHAR" || info.getType() != "VARCHAR"){
                throw new Exception("L'élément " + (infoColonne.indexOf(info) + 1) + "de la liste est incorrecte");
            }
            this.infoColonne = infoColonne;
        }
        setColonne(infoColonne.size());
    }

    /**
     * Défini le nom de la relation
     * @param nom
     */
    public void setNom(String nom) {
        this.nom = nom;
    }


    /**
     * Ecrit les données d'un Record et les stocke dans un ByteBuffer
     * @param record Le Record contenant les données
     * @param buffer Le ByteBuffer où les données seront stockées
     * @param pos La position de départ dans le ByteBuffer
     */
    public void writeRecordToBuffer(Record record, ByteBuffer buffer, int pos){
        buffer.position(pos);
        List<String> valeurs = record.getValeurs();

        for (int i = 0; i < infoColonne.size(); i++) {
            InfoColonne<String, String> col = infoColonne.get(i);
            String type = col.getType().toUpperCase();
            String valeur = valeurs.get(i);

            switch (type) {
                case "INT":
                    int intValue = Integer.parseInt(valeur);
                    buffer.putInt(intValue);
                    break;

                case "FLOAT":
                    float floatValue = Float.parseFloat(valeur);
                    buffer.putFloat(floatValue);
                    break;

                case "CHAR":
                    int length = Size.CHAR.getTaille() ;
                    if (valeur.length() > length) {
                        valeur = valeur.substring(0, length);
                    } else if (valeur.length() < length) {
                        StringBuilder sb = new StringBuilder(valeur);
                        while (sb.length() < length) sb.append(' ');
                        valeur = sb.toString();
                    }

                    // Écriture caractère par caractère
                    byte[] bytes = valeur.getBytes(StandardCharsets.UTF_8);
                    buffer.put(bytes);
                    break;

                case "VARCHAR":
                    int maxLength = Size.VARCHAR.getTaille() ;
                    if (valeur.length() > maxLength) {
                        valeur = valeur.substring(0, maxLength);
                    }

                    byte[] data = valeur.getBytes(StandardCharsets.UTF_8);
                    buffer.putInt(data.length);  // longueur réelle
                    buffer.put(data);            // contenu
                    break;

                default:
                    System.err.println("Type inconnu : " + type);
                    break;
            }
        }
    }

    /**
     * Lit les données d'un ByteBuffer et les stocke dans un Record
     * @param record Le Record où les données seront stockées
     * @param buffer Le ByteBuffer contenant les données
     * @param pos La position de départ dans le ByteBuffer
     */
    public void readFromBuffer(Record record, ByteBuffer buffer, int pos) {
        buffer.position(pos);
        List<String> valeurs = new ArrayList<>();

        for (InfoColonne<String, String> info : infoColonne) {
            String type = info.getType();

            if (type.equals("INT")) {
                int val = buffer.getInt();
                valeurs.add(String.valueOf(val));
            } else if (type.equals("FLOAT")) {
                float val = buffer.getFloat();
                valeurs.add(String.valueOf(val));
            } else if (type.equals("CHAR")) {
                int size = Size.CHAR.getTaille(); 
                byte[] data = new byte[size];
                buffer.get(data);
                String str = new String(data, StandardCharsets.UTF_8).trim();
                valeurs.add(str);
            } else if (type.equals("VARCHAR")) {
                int maxSize = Size.VARCHAR.getTaille(); 
                int len = buffer.getInt(); 
                if (len > maxSize) len = maxSize;
                byte[] data = new byte[len];
                buffer.get(data);
                String str = new String(data, StandardCharsets.UTF_8);
                valeurs.add(str);
            }
        }

    record.setValeurs(valeurs);
    }

    public void addDataPage() {
        PageID pid = bufferManager.allocPage();

        if (pid == null) {
            System.err.println("Impossible d'allouer une nouvelle page pour la relation " + nom);
            return;
        }
        pagesLibres.add(pid);
    }

    public PageID getFreeDataPageId(int sizeRecord) {
        try {
            ByteBuffer headerBuffer = bufferManager.getPage(headerPageId);

            int nbPages = headerBuffer.getInt(0);
            for (int i = 0; i < nbPages; i++) {
                int offset = 4 + i * 8;
                int fileIdx = headerBuffer.getInt(offset);
                int pageIdx = headerBuffer.getInt(offset + 4);
                PageID pageID = new PageID(fileIdx, pageIdx);

                ByteBuffer dataBuffer = bufferManager.getPage(pageID);

                int espaceLibre = dataBuffer.getInt(0);

                if (espaceLibre >= sizeRecord) {
                    bufferManager.FreePage(pageID, false);
                    bufferManager.FreePage(headerPageId, false);
                    return pageID;
                }
                bufferManager.FreePage(pageID, false);
            }

            bufferManager.FreePage(headerPageId, false);
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getRecordSize() {
    int size = 0;

    for (InfoColonne<String, String> info : infoColonne) {
        switch (info.getType()) {
            case "INT":
                size += 4;
                break;
            case "FLOAT":
                size += 4;
                break;
            case "CHAR":
                size += Size.CHAR.getTaille();
                break;
            case "VARCHAR":
                // stratégie TP : VARCHAR(T) = 4 bytes length + T bytes data
                size += 4 + Size.VARCHAR.getTaille();
                break;
                }
            }
        return size;
    }


    public RecordId writeRecordToDataPage(Record record, PageID pageId){
        try {
        // Récupération du buffer de la page
        ByteBuffer buffer = bufferManager.getPage(pageId);

        // Lecture des informations de gestion de la page
        int freeSpace = buffer.getInt(0);        // espace libre restant
        int nbRecords = buffer.getInt(4);        // nombre d'enregistrements actuellement dans la page

        // Taille fixe d’un record
        int recordSize = getRecordSize();

        // Calcul de l’offset d’écriture : après les records existants
        int pos = 8 + nbRecords * recordSize;

        // Écriture du record dans le buffer
        writeRecordToBuffer(record, buffer, pos);

        // Mise à jour du nombre de records
        nbRecords++;
        buffer.putInt(4, nbRecords);

        // Mise à jour de l’espace libre restant
        freeSpace -= recordSize;
        buffer.putInt(0, freeSpace);

        // Création du RecordId
        RecordId rid = new RecordId(pageId, nbRecords - 1);

        // Libération de la page (dirty = true car modifiée)
        bufferManager.FreePage(pageId, true);

        return rid;

    } catch (Exception e) {
        e.printStackTrace();
        return null;
        }
    }

    public ArrayList<Record> getRecordsInDataPage(PageID pageId){
        ArrayList<Record> records = new ArrayList<>();

    try {
        // Récupération de la page en mémoire
        ByteBuffer buffer = bufferManager.getPage(pageId);

        // Lecture des valeurs d'entête
        int freeSpace = buffer.getInt(0); //lu pour le faire passer
        int nbRecords = buffer.getInt(4);

        int recordSize = getRecordSize();

        // Offset où commencent les records
        int pos = 8;

        // Lire chaque record
        for (int i = 0; i < nbRecords; i++) {
            Record r = new Record();
            readFromBuffer(r, buffer, pos);
            records.add(r);

            pos += recordSize;
        }

        // Libération de la page après lecture (dirty = false)
        bufferManager.FreePage(pageId, false);

    } catch (Exception e) {
        e.printStackTrace();
    }

    return records;
    }

    public List<PageID> getDataPages(){

    }

    public RecordId insertRecord(Record record){

    }

    public ArrayList<Record> getAllRecords(){

    }

    public void deleteRecord(RecordId rid){

    }


}
