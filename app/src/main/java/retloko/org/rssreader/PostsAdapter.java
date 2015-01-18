package retloko.org.rssreader;

import android.widget.BaseAdapter;
import android.widget.TextView;
import android.content.Intent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import java.util.*;
import android.view.*;

public class PostsAdapter extends BaseAdapter {
    private final String javascriptNamespace;
    private final PostsActivity activity;
    private final JavascriptInterface javascriptInterface = new JavascriptInterface();
    private List<Post> data;

    public PostsAdapter(PostsActivity activity, List<Post> data) {
        this.activity = activity;
        this.data = data;

        Random random = new Random();
        this.javascriptNamespace = "A" + Math.abs(random.nextInt());
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setData(List<Post> newData) {
        data = newData;
        notifyDataSetChanged();
    }

    public void addItem(Post what) {
        data.add(what);
        notifyDataSetChanged();
    }

    public void removeItem(int id) {
        data.remove(id);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post, viewGroup, false);
        }
        TextView titleView = (TextView) convertView.findViewById(android.R.id.text1);
        final WebView summaryView = (WebView) convertView.findViewById(android.R.id.text2);

        final Post post = (Post) getItem(i);

        titleView.setText(post.title.trim());
        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post.toggleSummary();
                summaryView.setVisibility(post.isShowSummary() ? View.VISIBLE : View.GONE);
            }
        });

        String showArticleHtml = String.format(activity.getString(R.string.show_article_html), javascriptNamespace, i);

        summaryView.setVisibility(post.isShowSummary() ? View.VISIBLE : View.GONE);
        summaryView.loadData(post.summary + showArticleHtml, "text/html; charset=utf-8", null);

        WebSettings webSettings = summaryView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        summaryView.addJavascriptInterface(javascriptInterface, javascriptNamespace);

        return convertView;
    }

    private class JavascriptInterface {
        @android.webkit.JavascriptInterface
        public void showArticle(int i) {
            Post post = (Post) getItem(i);
            Intent articleIntent = new Intent(activity, WebActivity.class);

            articleIntent.putExtra(WebActivity.ARTICLE_TITLE, post.title);
            articleIntent.putExtra(WebActivity.ARTICLE_URL, post.link);

            activity.startActivity(articleIntent);
        }
    }
}
