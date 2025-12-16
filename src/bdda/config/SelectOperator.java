package bdda.config;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectOperator implements IRecordIterator{
    private IRecordIterator fils;
    private ArrayList<Condition> conditions; //conditions de selection
    private HashMap<String, Relation> aliasMap; //map des alias pour evaluer les conditions

    public SelectOperator(IRecordIterator fils, ArrayList<Condition> conditions,  HashMap<String, Relation> aliasMap) {
        this.fils = fils;
        this.conditions = conditions;
        this.aliasMap = aliasMap;
    }

    @Override
    public Record getNextRecord() {
        Record record;
        while(((record = fils.getNextRecord()) != null)) {
            boolean garde = true;
            for(Condition condition : conditions) {
                if(!condition.evaluerConditionSurRecord(record, aliasMap)){
                    garde = false;
                    break;
                }
            }
            if (garde) {
                return record;
            }
        }
        return null;
    }

    @Override
    public void close() {
        fils.close();
    }

    @Override
    public void reset() {
        fils.reset();
    }

}
