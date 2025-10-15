package bdda.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    /** Liste des tailles des 4 types de colonnes */
    public enum Size{
        INT(4), FLOAT(4), CHAR(64), VARCHAR(64) ;

        private final int taille ;

        private Size(int taille){
            this.taille = taille ;
        }

        public int getTaille(){
            return this.taille ;
        }
    }

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
     * Ecrit les données d'un Record et les stocke dans un ByteBuffer
     * @param record Le Record contenant les données
     * @param buffer Le ByteBuffer où les données seront stockées
     * @param pos La position de départ dans le ByteBuffer
     */
    public void writeRecordToBuffer(Record record, ByteBuffer buffer, int pos){
        buffer.position(pos);
        List<String> valeurs = record.getValeurs();

        for (int i = 0; i < infoColonne.size(); i++) {
            InfoColonne<String, String> col = infoColonne.get(i);
            String type = col.getType().toUpperCase();
            String valeur = valeurs.get(i);

            switch (type) {
                case "INT":
                    int intValue = Integer.parseInt(valeur);
                    buffer.putInt(intValue);
                    break;

                case "FLOAT":
                    float floatValue = Float.parseFloat(valeur);
                    buffer.putFloat(floatValue);
                    break;

                case "CHAR":
                    int length = Size.CHAR.getTaille() ;
                    if (valeur.length() > length) {
                        valeur = valeur.substring(0, length);
                    } else if (valeur.length() < length) {
                        StringBuilder sb = new StringBuilder(valeur);
                        while (sb.length() < length) sb.append(' ');
                        valeur = sb.toString();
                    }

                    // Écriture caractère par caractère
                    byte[] bytes = valeur.getBytes(StandardCharsets.UTF_8);
                    buffer.put(bytes);
                    break;

                case "VARCHAR":
                    int maxLength = Size.VARCHAR.getTaille() ;
                    if (valeur.length() > maxLength) {
                        valeur = valeur.substring(0, maxLength);
                    }

                    byte[] data = valeur.getBytes(StandardCharsets.UTF_8);
                    buffer.putInt(data.length);  // longueur réelle
                    buffer.put(data);            // contenu
                    break;

                default:
                    System.err.println("Type inconnu : " + type);
                    break;
            }
        }
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
                int size = Size.CHAR.getTaille(); 
                byte[] data = new byte[size];
                buffer.get(data);
                String str = new String(data, StandardCharsets.UTF_8).trim();
                valeurs.add(str);
            } else if (type.equals("VARCHAR")) {
                int maxSize = Size.VARCHAR.getTaille(); 
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
