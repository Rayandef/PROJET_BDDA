package bdda.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
//INT 4 OCTETS
//FLOAT 4 OCTETS
//CHAR 64
//VARCHAR 64

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
            if (!(info.getType().equals("INT") ||info.getType().equals("FLOAT") || info.getType().equals("CHAR") || info.getType().equals("VARCHAR"))) {
                throw new Exception("L'élément " + (infoColonne.indexOf(info) + 1) + " de la liste est incorrect");
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

    /**
     * Lit les données d'un ByteBuffer et les stocke dans un Record
     * @param record Le Record où les données seront stockées
     * @param buffer Le ByteBuffer contenant les données
     * @param pos La position de départ dans le ByteBuffer
     */
    public void readFromBuffer(Record record, ByteBuffer buffer, int pos) {
        buffer.position(pos);
        List<String> valeurs = new ArrayList<>();

        for (InfoColonne<String, String> info : infoColonne) {
            String type = info.getType();

            if (type.equals("INT")) {
                int val = buffer.getInt();
                valeurs.add(String.valueOf(val));
            } else if (type.equals("FLOAT")) {
                float val = buffer.getFloat();
                valeurs.add(String.valueOf(val));
            } else if (type.equals("CHAR")) {
                int size = 64; 
                byte[] data = new byte[size];
                buffer.get(data);
                String str = new String(data, StandardCharsets.UTF_8).trim();
                valeurs.add(str);
            } else if (type.equals("VARCHAR")) {
                int maxSize = 64; 
                int len = buffer.getInt(); 
                if (len > maxSize) len = maxSize;
                byte[] data = new byte[len];
                buffer.get(data);
                String str = new String(data, StandardCharsets.UTF_8);
                valeurs.add(str);
            }
        }

    record.setValeurs(valeurs);
}
}
