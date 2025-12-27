package bdda.config;

public class RecordPrinter {

    private IRecordIterator recordIterator;

    public RecordPrinter(IRecordIterator recordIterator) {
        this.recordIterator = recordIterator;
    }

    public void printAllRecords(){
        Record record;
        int nbSelect = 0;

        while( (record = recordIterator.getNextRecord()) != null ){
            nbSelect += 1;
            printRecord(record);
        }
        System.out.println(nbSelect + " Record(s) sélectionné(s)");
    }
    
    private void printRecord(Record record){
    if(record == null || record.getValeurs() == null){
        return;
    }
    for(int i = 0; i < record.getValeurs().size(); i++){
        System.out.print(record.getValeurs().get(i));
        if(i < record.getValeurs().size() - 1){
            System.out.print(" | ");
        }
    }
    System.out.println(); // saut de ligne après chaque tuple
    }

}
