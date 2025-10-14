package bdda.config;

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
