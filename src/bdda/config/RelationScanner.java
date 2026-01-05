package bdda.config;

import java.nio.ByteBuffer;
import java.util.List;

/** Classe représentant un scanner permettant de parcourir une relation */
public class RelationScanner implements IRecordIterator{
    /** Relation qu'on va parcourir */
    private Relation relation; 

    /** index de la page courante */
    private int pageIndex;

    /** index du tuple courant dans la page courante */
    private int tupleIndex;

    /** buffer de la page courante */
    private ByteBuffer pageBufferActuel;

    /** PageID de la page courante */
    private PageID pageIDActuel;

    /** derniere case exploree */
    private int lastSlotIndex = -1;

    /** derniere pageID exploree */
    private PageID lastPageId = null;

    /** Construit un scanner à partir d'une relation
     * @param relation la relation qu'on va parcourir
     */
    public RelationScanner(Relation relation){
        this.relation = relation;
        this.pageIndex = 0;
        this.tupleIndex = 0;
        this.pageIDActuel = null;
        loadNextPage();
    }

    /** Charge la prochaine page */
    private void loadNextPage(){
        List<PageID> dataPages = relation.getDataPages();
        if(pageIndex >= dataPages.size()){
            pageBufferActuel = null;
            pageIDActuel = null;
            return; // éviter l'accès hors limites
        }
        if(pageIDActuel != null){
            relation.getBufferManager().FreePage(pageIDActuel, false);
        }

        pageIDActuel = dataPages.get(pageIndex);
        pageBufferActuel = relation.getBufferManager().getPage(pageIDActuel);
        tupleIndex = 0;
    }

    /** 
     * Implémente getNextRecord() de IRecordIterator
     */
    @Override
    public Record getNextRecord() {
        if (pageBufferActuel == null) {
            return null; // plus de pages
        }
        while (tupleIndex < relation.getNbCasesParPage()) {
            int stateOffset = Integer.BYTES + tupleIndex;
            byte state = pageBufferActuel.get(stateOffset);
            if (state != 0) {
                Record record = new Record();
                // Lecture du record
                relation.readFromBuffer(record, pageBufferActuel,relation.getDataPageRecordOffset(tupleIndex));
                //Mémorisation du RecordId courant
                lastSlotIndex = tupleIndex;
                lastPageId = pageIDActuel;
                tupleIndex++;
                return record;
            }

            tupleIndex++;
        }
        // Fin de page, on libère et on passe à la suivante
        relation.getBufferManager().FreePage(pageIDActuel, false);
        pageIndex++;
        loadNextPage();

        return getNextRecord();
    }


    @Override
    /**
     * Implémente reset() de IRecordIterator
     */
    public void reset(){
        if(pageIDActuel != null){
            relation.getBufferManager().FreePage(pageIDActuel, false);
        }
        pageIndex = 0;
        tupleIndex = 0;
        loadNextPage();
    }

    @Override
    /** 
     * Implémente getNextRecord de IRecordIterator 
     */
    public void close(){
        if(pageIDActuel != null){
            relation.getBufferManager().FreePage(pageIDActuel, false);
        }
        pageBufferActuel = null;
        pageIDActuel = null;
    }

    /**
     * Récupere l'identifiant du tuple courant
     * @return l'identifiant du tuple en train d'être parcouru
     */
    public RecordId getCurrentRecordId() {
        if (lastPageId == null || lastSlotIndex < 0) {
            return null;
        }
        return new RecordId(lastPageId, lastSlotIndex);
    }


}
