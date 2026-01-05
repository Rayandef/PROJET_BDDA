package bdda.config;

/**
 * Iterator personnalisé permettant de parcourir un record.
 */
public interface IRecordIterator {
    /** Récupère le prochain record */
    public Record getNextRecord();
    /** Ferme l'iterator */
    public void close();
    /** Réinitialise l'iterator */
    public void reset();
}
