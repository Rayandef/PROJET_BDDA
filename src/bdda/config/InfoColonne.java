package bdda.config;

public class InfoColonne<nom extends CharSequence, type extends CharSequence>{

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

