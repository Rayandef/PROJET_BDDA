package bdda.config;

import java.util.ArrayList;
import java.util.HashMap;

/** Classe permettant de gérer la selection des tuples */
public class SelectOperator implements IRecordIterator{
    /** Iterator */
    private IRecordIterator fils;

    /** conditions de selection */
    private ArrayList<Condition> conditions;

    /** map des alias pour evaluer les conditions */
    private HashMap<String, Relation> aliasMap;

    /**
     * Construit un SelectOperator
     * @param fils Iterator
     * @param conditions Liste de conditions de la commande SELECT
     * @param aliasMap Map des alias
     */
    public SelectOperator(IRecordIterator fils, ArrayList<Condition> conditions,  HashMap<String, Relation> aliasMap) {
        this.fils = fils;
        this.conditions = conditions;
        this.aliasMap = aliasMap;
    }

    @Override
    /** 
     * Implémente getNextRecord() de IRecordIterator
     */
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
    /**
     * Implémente reset() de IRecordIterator
     */
    public void close() {
        fils.close();
    }

    @Override
    /** 
     * Implémente getNextRecord de IRecordIterator 
     */
    public void reset() {
        fils.reset();
    }

}
