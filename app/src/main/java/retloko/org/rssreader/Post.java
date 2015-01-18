package retloko.org.rssreader;

public class Post {
    public final int id;
    public final int feedId;
    public final String title;
    public final String summary;
    public final String link;

    private boolean showSummary = false;

    protected Post(int id, int feedId, String title, String summary, String link) {
        this.id = id;
        this.feedId = feedId;
        this.title = title;
        this.summary = summary;
        this.link = link;
    }

    protected void toggleSummary() {
        showSummary = !showSummary;
    }

    protected boolean isShowSummary() {
        return showSummary;
    }
}