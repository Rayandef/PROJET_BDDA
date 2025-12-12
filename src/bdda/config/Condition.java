package bdda.config;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import bdda.config.Relation.Size;


public class Condition {
    String gauche;
    String operateur;
    String droite;
    public Condition(String gauche, String operateur, String droite) {
        this.gauche = gauche;
        this.operateur = operateur;
        this.droite = droite;
    }

    public String getDroite() {
        return droite;
    }

    public String getGauche() {
        return gauche;
    }

    // Récupère la relation associée à un terme (ex: Tab2.C7 retourne la relation Tab2)
    public Relation recupererRelation(String terme, HashMap<String, Relation> aliasMap){ {
        if(terme.contains(".")){
            String[] parties = terme.split("\\.");
            String alias = parties[0];
            if(aliasMap != null && aliasMap.containsKey(alias)){
                return aliasMap.get(alias);
            }
            for(String key : aliasMap.keySet()){
                Relation rel = aliasMap.get(key);
                if(rel.getNom().equalsIgnoreCase(alias)){
                    return rel;
                }
            }
        }
        System.out.println("Aucune relation trouvée pour le terme : " + terme);
        return null;
        }
    }

    //méthode qui récupère les valeurs d'une colonne donnée dans une relation associée à un terme (ex: Tab2.AA renvoie les valeurs de la colonne AA dans la relation Tab2)
    public ArrayList<String> recupererValeursColonne(String terme, HashMap<String, Relation> aliasMap) {
        ArrayList<String> valeurs = new ArrayList<>();
        String[] parties = terme.split("\\.");
        String nomColonne = parties[1]; //recupère le nom de la colonne
        Relation rel = recupererRelation(terme, aliasMap); //recupère la relation associée
        if (rel == null) {
            System.out.println("Relation introuvable.");
            return valeurs;
        }
        int indexColonne = -1;
        ArrayList<InfoColonne<String, String>> colonnes = (ArrayList<InfoColonne<String, String>>) rel.getInfoColonne();
        for (int i = 0; i < colonnes.size(); i++) {
            if (colonnes.get(i).getNom().equalsIgnoreCase(nomColonne)) { //si le nom de la colonne correspond
                indexColonne = i;
                break;
            }
        }
        if (indexColonne == -1) {
            System.out.println("Colonne introuvable : " + nomColonne);
            return valeurs;
        }
        // Parcours les records de la relation
        for (Record r : rel.getAllRecords()) {
            String valeur = r.getValeurs().get(indexColonne);//récupère la valeur de la colonne
            valeurs.add(valeur);
        }
        return valeurs;
    }

    //méthode qui convertit une valeur brute en fonction du type de la colonne
    private Object convertirValeur(String valeur, InfoColonne<String, String> col) {
        String type = col.getType().toUpperCase();

        switch (type) {
            case "INT":
                return Integer.parseInt(valeur);

            case "FLOAT":
                return Float.parseFloat(valeur);

            case "CHAR":
            case "VARCHAR":
                return valeur;

            default:
                throw new RuntimeException("Type inconnu : " + type);
        }
    }

    //méthode qui évalue une condition et retourne les valeurs correspondantes à la colonne de gauche si la condition est satisfaite
    public ArrayList<String> evaluerCondition(
        Condition condition,
        HashMap<String, Relation> aliasMap) {

        ArrayList<String> resultats = new ArrayList<>();

        String termeGauche = condition.getGauche();
        String termeDroite = condition.getDroite();
        String op = condition.operateur;

        //Récupère la relation et la colonne de gauche
        Relation rel = recupererRelation(termeGauche, aliasMap);
        String nomColGauche = termeGauche.split("\\.")[1];
        // Récupère les colonnes de la relation
        ArrayList<InfoColonne<String, String>> colonnes =(ArrayList<InfoColonne<String, String>>) rel.getInfoColonne();
        InfoColonne<String, String> colGauche = null;
        for (int i = 0; i < colonnes.size(); i++) {
            if (colonnes.get(i).getNom().equalsIgnoreCase(nomColGauche)) {
                colGauche = colonnes.get(i);
                break;
            }
        }

        // Récupère les valeurs de la colonne de gauche
        ArrayList<String> valeursGauche = recupererValeursColonne(termeGauche, aliasMap);

        // Récupère les valeurs de la colonne de droite si c'est une colonne
        ArrayList<String> valeursDroite = null;
        InfoColonne<String, String> colDroite = colGauche;

        if (termeDroite.contains(".")) {
            valeursDroite =
                    recupererValeursColonne(termeDroite, aliasMap);

            // colonne droite (pour le type)
            String nomColDroite = termeDroite.split("\\.")[1];
            for (InfoColonne<String, String> c : colonnes) {
                if (c.getNom().equalsIgnoreCase(nomColDroite)) {
                    colDroite = c;
                    break;
                }
            }
        }

        //Parcours les valeurs et évalue la condition
        for (int i = 0; i < valeursGauche.size(); i++) {
            //Valeur gauche
            Object valeurGauche = convertirValeur(valeursGauche.get(i), colGauche);
            // valeur droite (colonne ou constante)
            Object valeurDroite =(valeursDroite != null)? convertirValeur(valeursDroite.get(i), colDroite): convertirValeur(termeDroite.replace("'", ""),colGauche);
            boolean ok = comparer(valeurGauche, valeurDroite, op);
            if (ok) {
                resultats.add(valeursGauche.get(i)); //ajoute la valeur de la colonne de gauche si la condition est satisfaite
            }
        }

        return resultats;
    }

    //Méthode qui compare deux objets en fonction de l'opérateur
    private boolean comparer(Object g, Object d, String op) {

        // INT ou FLOAT
        if (g instanceof Number && d instanceof Number) {
            double a = ((Number) g).doubleValue();
            double b = ((Number) d).doubleValue();

            switch (op) {
                case "=":  return a == b;
                case "<>": return a != b;
                case "<":  return a < b;
                case ">":  return a > b;
                case "<=": return a <= b;
                case ">=": return a >= b;
            }
        }

        // CHAR ou VARCHAR
        String s1 = g.toString();
        String s2 = d.toString();

        switch (op) {
            case "=":  return s1.equals(s2);
            case "<>": return !s1.equals(s2);
            case "<":  return s1.compareTo(s2) < 0;
            case ">":  return s1.compareTo(s2) > 0;
            case "<=": return s1.compareTo(s2) <= 0;
            case ">=": return s1.compareTo(s2) >= 0;
        }

        return false;
    }


}