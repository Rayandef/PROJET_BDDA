package bdda.config;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectOperator implements IRecordIterator{
    private IRecordIterator fils;
    private ArrayList<String> nomsColonnes; //noms des colonnes à projeter
    private HashMap<String, Relation> aliasMap; //map des alias pour evaluer les colonnes

    public ProjectOperator(IRecordIterator fils, ArrayList<String> nomsColonnes, HashMap<String, Relation> aliasMap) {
        this.fils = fils;
        this.nomsColonnes = nomsColonnes;
        this.aliasMap = aliasMap;
    }

    @Override
    public Record getNextRecord() {
        Record record = fils.getNextRecord();
        if(record == null){
            return null;
        }
        ArrayList<String> valeurAProjeter = new ArrayList<>();
        for(String colonne : nomsColonnes){
            //sérparer le nom de la colonne et l'alias
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

    @Override
    public void close() {
        fils.close();
    }

    @Override
    public void reset() {
        fils.reset();
    }
}
