package bdda.config;

/** Représente l'identifiant d'un tuple */
public class RecordId {
    /** Identifiant de la page */
    private PageID pageId;

    /** Identifiant du slot */
    private int slotIdx;

    /**
     * Crée un record selon le pageId et le slotIdx mis en entrée
     * @param pageId Identifiant de la page où on va créer le record
     * @param slotIdx Identifiant du slot où on va créer le record
     */
    public RecordId(PageID pageId, int slotIdx) {
        this.pageId = pageId;
        this.slotIdx = slotIdx;
    }

    /** Récupère l'identifiant de la page */
    public PageID getPageId() {
        return pageId;
    }

    /** Récupère l'identifiant du slot */
    public int getSlotIdx() {
        return slotIdx;
    }

    /** Défini l'identifiant de la page */
    public void setPageId(PageID pageId) {
        this.pageId = pageId;
    }

    /** Défini l'indentifiant du slot */
    public void setSlotIdx(int slotIdx) {
        this.slotIdx = slotIdx;
    }

    @Override
    public String toString() {
        return "RecordId{" + pageId + ", slot=" + slotIdx + "}";
    }
}
