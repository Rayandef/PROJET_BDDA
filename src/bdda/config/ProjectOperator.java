package bdda.config;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectOperator implements IRecordIterator{
    /** Iterator */
    private IRecordIterator fils;

    /** Nom des colonnes à projeter */
    private ArrayList<String> nomsColonnes;

    /** Map des alias pour evaluer les colonnes */
    private HashMap<String, Relation> aliasMap;

    /**
     * Constructeur de ProjectOperator
     * @param fils iterator pour parcourir les tuples
     * @param nomsColonnes Nom des colonnes à projeter
     * @param aliasMap Map des alias pour evaluer les colonnes
     */
    public ProjectOperator(IRecordIterator fils, ArrayList<String> nomsColonnes, HashMap<String, Relation> aliasMap) {
        this.fils = fils;
        this.nomsColonnes = nomsColonnes;
        this.aliasMap = aliasMap;
    }

    /**
     * Implémente getNextRecord() de IRecordIterator
     */
    @Override
    public Record getNextRecord() {
        Record record = fils.getNextRecord();
        if(record == null){
            return null;
        }
        ArrayList<String> valeurAProjeter = new ArrayList<>();
        for(String colonne : nomsColonnes){
            //séparer le nom de la colonne et l'alias
            String[] parties = colonne.split("\\.");
            String alias = parties[0]; //alias de la relation
            String nomColonne = parties[1]; //nom de la colonne
            Relation rel = aliasMap.get(alias);
            //On chercher l'index de la colonne dans la relation
            int index = -1 ;
            ArrayList<InfoColonne<String, String>> infoColonnes = (ArrayList<InfoColonne<String, String>>) rel.getInfoColonne();
            for(int i = 0; i < infoColonnes.size(); i++){
                if(infoColonnes.get(i).getNom().equals(nomColonne)){
                    index = i;
                    break;
                }
            }

            String valeur = record.getValeurs().get(index);
            valeurAProjeter.add(valeur);
        }
        return new Record(valeurAProjeter);
    }

    /**
     * Implémente close() de IRecordIterator
     */
    @Override
    public void close() {
        fils.close();
    }

    /**
     * Implémente reset() de IRecordIterator
     */
    @Override
    public void reset() {
        fils.reset();
    }
}
