package retloko.org.rssreader;

public class Feed {
    public static final Feed[] PREINSTALLED_FEEDS = new Feed[]{
            new Feed(-1, "stackoverflow", "http://stackoverflow.com/feeds/tag/android"),
            new Feed(-1, "news", "http://feeds.bbci.co.uk/news/rss.xml"),
            new Feed(-1, "msk", "http://echo.msk.ru/interview/rss-fulltext.xml"),
            new Feed(-1, "bash", "http://bash.im/rss/")
    };
    public final String name;
    public final String url;
    public int id;

    public Feed(int id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }
}