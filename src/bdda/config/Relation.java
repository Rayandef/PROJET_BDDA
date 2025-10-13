package bdda.config;

import java.util.List;

/**
 * Tuple composé de <nom, type> pour chaque colonne d'une relation
 */
class InfoColonne<nom extends CharSequence, type extends CharSequence>{

    /** Nom de la colonne */
    private String nom;

    /** Type de la colonne entre INT, FLOAT, CHAR et VARCHAR */
    private String type;

    /** Crée un tuple InfoColonne */
    public InfoColonne(){
        this.nom = new String();
        this.type = new String();
    }

    /**
     * Récupère la nom
     * @return le nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * Récupère
     * @return le type
     */
    public String getType() {
        return type;
    }

    /**
     * Défini le nom
     * @param nom
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Défini le type
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }
}

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

    /** Crée une relation 
     * @param nom
     * @param infoColonne
    */
    public Relation(String nom, List<InfoColonne<String, String>> infoColonne) throws Exception{
        this.nom = nom;
        for(InfoColonne<String, String> info : infoColonne){
            if(info.getType() != "INT" || info.getType() != "FLOAT" || info.getType() != "CHAR" || info.getType() != "VARCHAR"){
                throw new Exception("L'élément " + (infoColonne.indexOf(info) + 1) + "de la liste est incorrecte");
            }
            this.infoColonne = infoColonne;
        }
        colonne = infoColonne.size();
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


}
