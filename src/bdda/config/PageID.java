package bdda.config;

/**Classe représentant la classification des pages dans chaque fichier 
 * @author !Jordan
 * @author !1.0
*/
public class PageID {
    /** Identifiant du fichier */
    private int fileIdx;
    /** Identifiant de la page */
    private int pageIdx;

    /**Créer une page selon le fileIdx et le pageIdx mis en entrée
     * @param fileIdx Identifiant du fichier qui comprend la page
     * @param pageIdx Identifiant de la page pour la situé dans le fichier
     */
    public PageID(int fileIdx, int pageIdx){
        this.fileIdx = fileIdx ;
        this.pageIdx = pageIdx ;
    }

    /** Récupère FileIdx
     * @return l'id du fichier
     */
    public int getFileIdx() {
        return fileIdx;
    }

    /** Récupère PageIdx
     * @return l'id de la page
     */
    public int getPageIdx() {
        return pageIdx;
    }

    /** Change le fileIdx
     * @param fileIdx le nouveau id de la fichier
     */
    public void setFileIdx(int fileIdx) {
        this.fileIdx = fileIdx;
    }

    /** Change le PageIdx
     * @param pageeIdx le nouveau id de la page
     */
    public void setPageIdx(int pageIdx) {
        this.pageIdx = pageIdx;
    }
}
