package bdda.config;

import java.util.ArrayList;
import java.util.List;
/**
 * Classe représentant l'enregitrement des valeurs de chaque tuple dans une liste de chaîne de caractère
 */
public class Record {

    /** Liste de tuples (en String, séparé par des virgules) */
    private List<String> valeurs;

    /** Crée un Record */
    public Record(List<String> valeurs){
        this.valeurs = valeurs;
    }

    /** Constructeur par défaut*/
    public Record(){
        valeurs = new ArrayList<>();
    }

    /**
     * Récupère les valeurs
     * @return les valeurs
     */
    public List<String> getValeurs(){
        return valeurs;
    }

    /**
     * Défini les valeurs 
     * @param valeurs
     */
    public void setValeurs(List<String> valeurs) {
        this.valeurs = valeurs;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(String valeur : valeurs){
            sb.append("[ ");
            String[] tabTempValeurs = valeur.split(",");
            for(int i = 0; i < tabTempValeurs.length; i++){
                sb.append(tabTempValeurs[i] + " ");
            }
            sb.append(" ]");
        }
        return sb.toString();
    }
}
