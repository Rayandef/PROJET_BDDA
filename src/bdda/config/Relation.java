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

    /** Liste des tailles des 4 types de colonnes */
    public enum Size{
        INT(4), FLOAT(4), CHAR(64), VARCHAR(64) ;

        private int taille ;
        private static final int LIMITE = 64 ;

        private Size(int taille){
            this.taille = taille ;
        }

        public int getTaille(){
            return this.taille ;
        }

        public void setTaille(int taille){
            if (verifTaille(taille)){
                this.taille = taille ;
            }
        }

        private boolean verifTaille(int taille){
            return (taille <= LIMITE)? true : false ;
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
        int pageSize = bufferManager.getDbConfig().getPageSize();
        int recordSize = getRecordSlotSize();
        int headerSize = getDataPageHeaderSize();
        this.nbCasesParPage = (pageSize - headerSize) / recordSize;
    }

    public Relation(String nom, List<InfoColonne<String, String>> infoColonne, PageID headerPageId, DiskManager diskManager, BufferManager bufferManager) throws Exception {
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
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        int pageSize = bufferManager.getDbConfig().getPageSize();
        int recordSize = getRecordSlotSize();
        int slotTotalSize = 1 + recordSize; // 1 octet pour état + taille du record
        this.nbCasesParPage = (pageSize - Integer.BYTES) / slotTotalSize; // Integer.BYTES = header pour usedSlots

        if (this.nbCasesParPage <= 0) {
            System.out.println("pageSize=" + pageSize + ", recordSize=" + recordSize + ", nbCasesParPage calculé=" + ((pageSize - Integer.BYTES)/(recordSize+1)));
            throw new Exception("La page est trop petite pour stocker un record !");
        }
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
            if(!(info.getType().equals("INT")) || !(info.getType().equals("FLOAT")) || !(info.getType().equals("CHAR")) || !(info.getType().equals("VARCHAR"))){
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

    public int getNbCasesParPage(){
        return this.nbCasesParPage ;
    }

    public PageID getHeaderPageId() {
        return this.headerPageId;
    }

    public DiskManager getDiskManager(){
        return this.diskManager ;
    }

    public BufferManager getBufferManager(){
        return this.bufferManager ;
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

    private int getRecordSlotSize() {
        int size = 0;
        for (InfoColonne<String, String> info : infoColonne) {
            String type = info.getType().toUpperCase();
            switch (type) {
                case "INT":
                case "FLOAT":
                    size += Integer.BYTES;
                    break;
                case "CHAR":
                    size += Size.CHAR.getTaille();
                    break;
                case "VARCHAR":
                    size += Integer.BYTES + Size.VARCHAR.getTaille();
                    break;
                default:
                    break;
            }
        }
        return size;
    }

    private int getDataPageHeaderSize() {
        return Integer.BYTES + nbCasesParPage;
    }

    private int getDataPageRecordOffset(int slotIdx) {
        return getDataPageHeaderSize() + slotIdx * getRecordSlotSize();
    }

    private int getHeaderEntryOffset(int entryIndex) {
        int fixed = Integer.BYTES * 3;
        int entrySize = Integer.BYTES * 3;
        return fixed + entryIndex * entrySize;
    }

    private int getHeaderEntryCapacity() {
        if (bufferManager == null) {
            return 0;
        }
        int pageSize = bufferManager.getDbConfig().getPageSize();
        int usable = pageSize - Integer.BYTES * 3;
        int entrySize = Integer.BYTES * 3;
        return Math.max(1, usable / entrySize);
    }

    private static class HeaderEntryInfo {
        PageID headerPage;
        int entryIndex;
        PageID dataPage;
        int freeSlots;

        HeaderEntryInfo(PageID headerPage, int entryIndex, PageID dataPage, int freeSlots) {
            this.headerPage = headerPage;
            this.entryIndex = entryIndex;
            this.dataPage = dataPage;
            this.freeSlots = freeSlots;
        }
    }

    private List<HeaderEntryInfo> collectHeaderEntries() {
        ArrayList<HeaderEntryInfo> entries = new ArrayList<>();
        if (bufferManager == null || headerPageId == null) {
            return entries;
        }

        PageID currentHeader = new PageID(headerPageId.getFileIdx(), headerPageId.getPageIdx());
        while (currentHeader != null) {
            PageID headerToRead = new PageID(currentHeader.getFileIdx(), currentHeader.getPageIdx());
            ByteBuffer headerBuffer = bufferManager.getPage(headerToRead);
            int nbEntries = headerBuffer.getInt(0);
            int nextFileIdx = headerBuffer.getInt(Integer.BYTES);
            int nextPageIdx = headerBuffer.getInt(Integer.BYTES * 2);
            try {
                for (int i = 0; i < nbEntries; i++) {
                    int offset = getHeaderEntryOffset(i);
                    int fileIdx = headerBuffer.getInt(offset);
                    int pageIdx = headerBuffer.getInt(offset + Integer.BYTES);
                    int freeSlots = headerBuffer.getInt(offset + Integer.BYTES * 2);
                    entries.add(new HeaderEntryInfo(
                        new PageID(headerToRead.getFileIdx(), headerToRead.getPageIdx()),
                        i,
                        new PageID(fileIdx, pageIdx),
                        freeSlots));
                }
            } finally {
                bufferManager.FreePage(headerToRead, false);
            }

            if (nextFileIdx >= 0 && nextPageIdx >= 0) {
                currentHeader = new PageID(nextFileIdx, nextPageIdx);
            } else {
                currentHeader = null;
            }
        }
        return entries;
    }

    private HeaderEntryInfo findHeaderEntry(PageID dataPageId) {
        for (HeaderEntryInfo entry : collectHeaderEntries()) {
            if (entry.dataPage.getFileIdx() == dataPageId.getFileIdx() &&
                entry.dataPage.getPageIdx() == dataPageId.getPageIdx()) {
                return entry;
            }
        }
        return null;
    }

    private void updateHeaderFreeSlots(PageID dataPageId, int freeSlots) {
        HeaderEntryInfo entry = findHeaderEntry(dataPageId);
        if (entry == null || bufferManager == null) {
            return;
        }
        ByteBuffer headerBuffer = bufferManager.getPage(entry.headerPage);
        try {
            int offset = getHeaderEntryOffset(entry.entryIndex) + Integer.BYTES * 2;
            headerBuffer.putInt(offset, freeSlots);
        } finally {
            bufferManager.FreePage(entry.headerPage, true);
        }
    }

    private void removeHeaderEntry(PageID dataPageId) {
        HeaderEntryInfo entry = findHeaderEntry(dataPageId);
        if (entry == null || bufferManager == null) {
            return;
        }
        ByteBuffer headerBuffer = bufferManager.getPage(entry.headerPage);
        try {
            int nbEntries = headerBuffer.getInt(0);
            int lastIndex = nbEntries - 1;
            if (lastIndex >= 0 && entry.entryIndex != lastIndex) {
                int source = getHeaderEntryOffset(lastIndex);
                int target = getHeaderEntryOffset(entry.entryIndex);
                int fileIdx = headerBuffer.getInt(source);
                int pageIdx = headerBuffer.getInt(source + Integer.BYTES);
                int freeSlots = headerBuffer.getInt(source + Integer.BYTES * 2);
                headerBuffer.putInt(target, fileIdx);
                headerBuffer.putInt(target + Integer.BYTES, pageIdx);
                headerBuffer.putInt(target + Integer.BYTES * 2, freeSlots);
            }
            headerBuffer.putInt(0, Math.max(0, nbEntries - 1));
        } finally {
            bufferManager.FreePage(entry.headerPage, true);
        }
    }

    public void initHeaderPage(PageID headerId) {
        if (bufferManager == null) {
            return;
        }
        ByteBuffer buffer = bufferManager.getPage(headerId);
        try {
            buffer.putInt(0, 0);
            buffer.putInt(Integer.BYTES, -1);
            buffer.putInt(Integer.BYTES * 2, -1);
        } finally {
            bufferManager.FreePage(headerId, true);
        }
    }

    private void registerDataPageInHeader(PageID dataPageId, int freeSlots) throws Exception {
        if (bufferManager == null || headerPageId == null || dataPageId == null) return;

        PageID currentHeader = new PageID(headerPageId.getFileIdx(), headerPageId.getPageIdx());

        while (true) {
            ByteBuffer headerBuffer = bufferManager.getPage(currentHeader);
            int nbEntries = headerBuffer.getInt(0);
            int nextFileIdx = headerBuffer.getInt(Integer.BYTES);
            int nextPageIdx = headerBuffer.getInt(Integer.BYTES * 2);
            int capacity = getHeaderEntryCapacity();

            if (nbEntries < capacity) {
                int offset = getHeaderEntryOffset(nbEntries);
                headerBuffer.putInt(offset, dataPageId.getFileIdx());
                headerBuffer.putInt(offset + Integer.BYTES, dataPageId.getPageIdx());
                headerBuffer.putInt(offset + 2 * Integer.BYTES, freeSlots);
                headerBuffer.putInt(0, nbEntries + 1);
                bufferManager.FreePage(currentHeader, true);
                return;
            }

            if (nextFileIdx < 0 || nextPageIdx < 0) {
                PageID newHeader = bufferManager.allocPage();
                    if (newHeader == null) {
                        throw new Exception("Impossible d'allouer une nouvelle header page !");
                    }
                initHeaderPage(newHeader);


                headerBuffer.putInt(Integer.BYTES, newHeader.getFileIdx());
                headerBuffer.putInt(2 * Integer.BYTES, newHeader.getPageIdx());
                bufferManager.FreePage(currentHeader, true);
                currentHeader = newHeader;
            } else {
                bufferManager.FreePage(currentHeader, false);
                currentHeader = new PageID(nextFileIdx, nextPageIdx);
            }
        }
    }


    private void initializeDataPage(PageID pageId) {
        if (bufferManager == null || pageId == null) return;

        ByteBuffer buffer = bufferManager.getPage(pageId);
        try {
            buffer.putInt(0, 0);

            for (int i = 0; i < nbCasesParPage; i++) {
                buffer.put(Integer.BYTES + i, (byte) 0);
            }

            int recordStart = Integer.BYTES + nbCasesParPage;
            int totalRecordBytes = nbCasesParPage * getRecordSlotSize();
            for (int i = 0; i < totalRecordBytes; i++) {
                buffer.put(recordStart + i, (byte) 0);
            }
        } finally {
            bufferManager.FreePage(pageId, true);
        }
    }



    public PageID allocateAndRegisterDataPage() {
        if (bufferManager == null) {
            return null;
        }
        PageID newPage = bufferManager.allocPage();
        if (newPage == null) {
            return null;
        }
        initializeDataPage(newPage);
        try {
            registerDataPageInHeader(newPage, nbCasesParPage);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return newPage;
    }

    public void addDataPage(){
        allocateAndRegisterDataPage();
    }

    public PageID getFreeDataPageId(int sizeRecord){
        for (HeaderEntryInfo entry : collectHeaderEntries()) {
            if (entry.freeSlots > 0) {
                return entry.dataPage;
            }
        }
        return allocateAndRegisterDataPage();
    }


    public RecordId writeRecordToDataPage(Record record, PageID pageId){
        if (record == null || pageId == null || bufferManager == null) {
            return null;
        }
        ByteBuffer buffer = bufferManager.getPage(pageId);
        int slotWritten = -1;
        int updatedUsed = -1;
        try {
            int usedSlots = buffer.getInt(0);
            for (int i = 0; i < nbCasesParPage; i++) {
                int stateOffset = Integer.BYTES + i;
                if (buffer.get(stateOffset) == 0) {
                    writeRecordToBuffer(record, buffer, getDataPageRecordOffset(i));
                    buffer.put(stateOffset, (byte) 1);
                    updatedUsed = usedSlots + 1;
                    buffer.putInt(0, updatedUsed);
                    slotWritten = i;
                    break;
                }
            }
        } finally {
            bufferManager.FreePage(pageId, slotWritten >= 0);
        }

        if (slotWritten < 0) {
            return null;
        }
        updateHeaderFreeSlots(pageId, nbCasesParPage - updatedUsed);
        return new RecordId(pageId, slotWritten);
    }

    public ArrayList<Record> getRecordsInDataPage(PageID pageId){
        ArrayList<Record> records = new ArrayList<>();
        if (pageId == null || bufferManager == null) {
            return records;
        }
        ByteBuffer buffer = bufferManager.getPage(pageId);
        try {
            for (int i = 0; i < nbCasesParPage; i++) {
                int stateOffset = Integer.BYTES + i;
                if (buffer.get(stateOffset) != 0) {
                    Record r = new Record();
                    readFromBuffer(r, buffer, getDataPageRecordOffset(i));
                    records.add(r);
                }
            }
        } finally {
            bufferManager.FreePage(pageId, false);
        }
        return records;
    }

    /**
     * Parcourt toutes les header pages afin de rassembler la liste des pages de données.
     * Chaque header est structurée ainsi : [nbEntries][nextFileIdx][nextPageIdx]
     * suivi de nbEntries triplets (fileIdx, pageIdx, nbSlotsLibres).
     */
    public List<PageID> getDataPages(){
        ArrayList<PageID> dataPages = new ArrayList<>();
        for (HeaderEntryInfo entry : collectHeaderEntries()) {
            dataPages.add(entry.dataPage);
        }
        return dataPages;
    }

    public RecordId insertRecord(Record record){
        if (record == null) {
            return null;
        }
        PageID target = getFreeDataPageId(getRecordSlotSize());
        if (target == null) {
            return null;
        }
        return writeRecordToDataPage(record, target);
    }

    public ArrayList<Record> getAllRecords(){
        ArrayList<Record> all = new ArrayList<>();
        for (PageID pageId : getDataPages()) {
            all.addAll(getRecordsInDataPage(pageId));
        }
        return all;
    }

    public void deleteRecord(RecordId rid){
        if (rid == null || rid.getPageId() == null || bufferManager == null) {
            return;
        }
        PageID pageId = rid.getPageId();
        ByteBuffer buffer = bufferManager.getPage(pageId);
        int updatedUsed = -1;
        boolean modified = false;
        try {
            int slotIdx = rid.getSlotIdx();
            if (slotIdx < 0 || slotIdx >= nbCasesParPage) {
                return;
            }
            int stateOffset = Integer.BYTES + slotIdx;
            if (buffer.get(stateOffset) == 0) {
                return;
            }
            int currentUsed = buffer.getInt(0);
            updatedUsed = Math.max(0, currentUsed - 1);
            buffer.put(stateOffset, (byte) 0);
            buffer.putInt(0, updatedUsed);
            modified = true;
        } finally {
            bufferManager.FreePage(pageId, modified);
        }

        if (updatedUsed < 0) {
            return;
        }

        if (updatedUsed == 0) {
            bufferManager.deAllocPage(pageId);
            removeHeaderEntry(pageId);
        } else {
            updateHeaderFreeSlots(pageId, nbCasesParPage - updatedUsed);
        }
    }

    public ArrayList<PageID> getAllPages() {
        ArrayList<PageID> pages = new ArrayList<>();
        pages.add(headerPageId);
        pages.addAll(getDataPages());
        return pages;
    }




}
