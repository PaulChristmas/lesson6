package retloko.org.rssreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class WebActivity extends Activity {
    public static final String ARTICLE_TITLE = "title";
    public static final String ARTICLE_URL = "url";

    private Intent starter;
    private WebView articleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        articleView = new WebView(this);
        setContentView(articleView);

        starter = getIntent();
        String articleTitle = starter.getStringExtra(ARTICLE_TITLE);
        String articleUrl = starter.getStringExtra(ARTICLE_URL);

        setTitle(articleTitle);

        articleView.loadUrl(articleUrl);
    }
}
