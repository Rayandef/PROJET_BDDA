package bdda.config;

import java.util.ArrayList;
import java.util.HashMap;



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
    public Relation recupererRelation(String terme, HashMap<String, Relation> aliasMap) {
        String[] parties = terme.split("\\.");
        if (parties.length != 2) {
            return null;
        }
        String alias = parties[0];

        if (aliasMap != null && aliasMap.containsKey(alias)) {
            return aliasMap.get(alias);
        }
        for (Relation rel : aliasMap.values()) {
            if (rel.getNom().equalsIgnoreCase(alias)) {
                return rel;
            }
        }
        return null;
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
    public ArrayList<String> evaluerCondition(Condition condition, HashMap<String, Relation> aliasMap) {

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
            Object valeurGauche = convertirValeur(valeursGauche.get(i), colGauche);
            Object valeurDroite =(valeursDroite != null)? convertirValeur(valeursDroite.get(i), colDroite): convertirValeur(nettoyerConstante(termeDroite), colGauche);
            boolean ok = comparer(valeurGauche,valeurDroite,op,colGauche);

            if (ok) {
                resultats.add(valeursGauche.get(i));
            }
        }
        return resultats;
    }

    //Méthode qui compare deux objets en fonction de l'opérateur
    private boolean comparer(Object gauche, Object droite, String op, InfoColonne<String, String> col) {

        String type = col.getType().toUpperCase();

        switch (type) {
            case "INT", "FLOAT": {
                double a = Double.parseDouble(gauche.toString());
                double b = Double.parseDouble(droite.toString());

                return switch (op) {
                    case "="  -> a == b;
                    case "<>" -> a != b;
                    case "<"  -> a < b;
                    case ">"  -> a > b;
                    case "<=" -> a <= b;
                    case ">=" -> a >= b;
                    default   -> false;
                };
            }

            case "CHAR", "VARCHAR": {
                String s1 = gauche.toString();
                String s2 = droite.toString();
                int cmp = s1.compareTo(s2);

                return switch (op) {
                    case "="  -> cmp == 0;
                    case "<>" -> cmp != 0;
                    case "<"  -> cmp < 0;
                    case ">"  -> cmp > 0;
                    case "<=" -> cmp <= 0;
                    case ">=" -> cmp >= 0;
                    default   -> false;
                };
            }
        }
        return false;
}



    //méthode qui récupère les informations d'une colonne donnée dans une relation associée à un terme (ex: Tab2.AA renvoie les infos de la colonne AA dans la relation Tab2)
    public InfoColonne<String, String> recupererColonne(String terme, HashMap<String, Relation> aliasMap) {
        //separee le terme en alias et nom de colonne
        String[] parties = terme.split("\\.");
        String nomColonne = parties[1];
        //recupere la relation associée
        Relation rel = recupererRelation(terme, aliasMap);
        if (rel == null) return null;
        //parcourt les colonnes de la relation pour trouver la colonne correspondante
        for (InfoColonne<String, String> col : rel.getInfoColonne()) {
            if (col.getNom().equalsIgnoreCase(nomColonne)) {
                return col;
            }
        }

        return null;
    }

    //méthode qui évalue une condition pour un index donné
    public boolean evaluerConditionIndex(int index,HashMap<String, Relation> aliasMap) {String termeGauche = gauche;String termeDroite = droite;
        // Récupère et convertit la valeur de gauche
        Object valeurGauche = convertirValeur(recupererValeursColonne(termeGauche, aliasMap).get(index), recupererColonne(termeGauche, aliasMap));

        Object valeurDroite;
        //Si le terme de droite est une colonne
        if (termeDroite.contains(".")) {
            valeurDroite = convertirValeur(recupererValeursColonne(termeDroite, aliasMap).get(index),recupererColonne(termeDroite, aliasMap)); // Récupère et convertit la valeur de droite
        } else {
            valeurDroite = convertirValeur(nettoyerConstante(termeDroite),recupererColonne(termeGauche, aliasMap));
        }
        return comparer(valeurGauche,valeurDroite,operateur,recupererColonne(termeGauche, aliasMap));
    }

    //méthode qui nettoie une constante en enlevant les guillemets s'ils sont présents
    private String nettoyerConstante(String valeur) {
        valeur = valeur.trim();
        if ((valeur.startsWith("'") && valeur.endsWith("'")) || (valeur.startsWith("\"") && valeur.endsWith("\""))
        ) {
            return valeur.substring(1, valeur.length() - 1);
        }
        return valeur;
    }


    //méthode qui évalue une condition sur un record donné
    public boolean evaluerConditionSurRecord(Record record, HashMap<String, Relation> aliasMap) {

        Object valeurGauche;
        Object valeurDroite;
        InfoColonne<String, String> colonneReference = null;

        //terme gauche
        InfoColonne<String, String> colGauche = recupererColonne(gauche, aliasMap);
        int indexGauche = recupererIndexColonne(gauche, aliasMap);

        if (colGauche != null && indexGauche != -1) {
            // gauche = colonne
            valeurGauche = convertirValeur(record.getValeurs().get(indexGauche), colGauche);
            colonneReference = colGauche;
        } else {
            // gauche = constante
            valeurGauche = nettoyerConstante(gauche);
        }

        //erme droite
        InfoColonne<String, String> colDroite = recupererColonne(droite, aliasMap);
        int indexDroite = recupererIndexColonne(droite, aliasMap);

        if (colDroite != null && indexDroite != -1) {
            // droite = colonne
            valeurDroite = convertirValeur(record.getValeurs().get(indexDroite), colDroite);
            if (colonneReference == null) {
                colonneReference = colDroite;
            }
        } else {
            // droite = constante
            valeurDroite = nettoyerConstante(droite);
        }
        //Conversionnécessaire 
        if (colonneReference != null) {
            if (!(valeurGauche instanceof Number) && !(valeurGauche instanceof String)) {
                valeurGauche = convertirValeur(valeurGauche.toString(), colonneReference);
            }
            if (!(valeurDroite instanceof Number) && !(valeurDroite instanceof String)) {
                valeurDroite = convertirValeur(valeurDroite.toString(), colonneReference);
            }
        }

    return comparer(valeurGauche, valeurDroite, operateur, colonneReference);
}


    //méthode qui récupère l'index d'une colonne donnée dans une relation associée à un terme (ex: Tab2.AA renvoie l'index de la colonne AA dans la relation Tab2)
    public int recupererIndexColonne(String terme, HashMap<String, Relation> aliasMap) {
        String[] parties = terme.split("\\.");
        if (parties.length != 2) {
            return -1;
        }
        String nomColonne = parties[1];
        Relation rel = recupererRelation(terme, aliasMap);
        if (rel == null) {
            return -1;
        }
        ArrayList<InfoColonne<String, String>> colonnes = (ArrayList<InfoColonne<String, String>>) rel.getInfoColonne();

        for (int i = 0; i < colonnes.size(); i++) {
            if (colonnes.get(i).getNom().equalsIgnoreCase(nomColonne)) {
                return i;
            }
        }
        return -1; // pas trouvé
    }

}