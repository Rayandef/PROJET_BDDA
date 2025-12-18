package bdda.config;

import java.nio.ByteBuffer;
import java.util.List;

public class RelationScanner implements IRecordIterator{
    
    private Relation relation;
    private int pageIndex; //index de la page courante
    private int tupleIndex; //index du tuple courant dans la page courante
    private ByteBuffer pageBufferActuel; //buffer de la page courante
    private PageID pageIDActuel; //PageID de la page courante
    private int lastSlotIndex = -1; //derniere case exploree
    private PageID lastPageId = null; //dernier pageID explore

    public RelationScanner(Relation relation){
        this.relation = relation;
        this.pageIndex = 0;
        this.tupleIndex = 0;
        this.pageIDActuel = null;
        loadNextPage();
    }

    private void loadNextPage(){
    List<PageID> dataPages = relation.getDataPages();
    if(pageIndex >= dataPages.size()){ //plus de pages
        pageBufferActuel = null;
        pageIDActuel = null;
        return; // éviter l'accès hors limites
    }

    // libérer le buffer précédent si nécessaire
    if(pageIDActuel != null){
        relation.getBufferManager().FreePage(pageIDActuel, false);
    }

    pageIDActuel = dataPages.get(pageIndex);
    pageBufferActuel = relation.getBufferManager().getPage(pageIDActuel);
    tupleIndex = 0;
}


@Override
public Record getNextRecord() {
    if (pageBufferActuel == null) {
        return null; // plus de pages
    }
    while (tupleIndex < relation.getNbCasesParPage()) {
        int stateOffset = Integer.BYTES + tupleIndex; // état du tuple
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
    //methode pour réinitialiser le scanner au début de la relation
    public void reset(){
        if(pageIDActuel != null){
            relation.getBufferManager().FreePage(pageIDActuel, false);
        }
        pageIndex = 0;
        tupleIndex = 0;
        loadNextPage();
    }

    @Override
    //methode pour fermer le scanner et libérer les ressources
    public void close(){
        if(pageIDActuel != null){
            relation.getBufferManager().FreePage(pageIDActuel, false);
        }
        pageBufferActuel = null;
        pageIDActuel = null;
    }

    public RecordId getCurrentRecordId() {
        if (lastPageId == null || lastSlotIndex < 0) {
            return null;
        }
        return new RecordId(lastPageId, lastSlotIndex);
    }


}
