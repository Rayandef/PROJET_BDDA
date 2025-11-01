package bdda.config;

public class RecordId {
    private PageID pageId;
    private int slotIdx;

    public RecordId(PageID pageId, int slotIdx) {
        this.pageId = pageId;
        this.slotIdx = slotIdx;
    }

    public PageID getPageId() {
        return pageId;
    }

    public int getSlotIdx() {
        return slotIdx;
    }

    public void setPageId(PageID pageId) {
        this.pageId = pageId;
    }

    public void setSlotIdx(int slotIdx) {
        this.slotIdx = slotIdx;
    }

    @Override
    public String toString() {
        return "RecordId{" + pageId + ", slot=" + slotIdx + "}";
    }
}
