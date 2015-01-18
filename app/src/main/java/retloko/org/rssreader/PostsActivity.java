package retloko.org.rssreader;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import java.util.*;

public class PostsActivity extends ListActivity implements LoaderManager.LoaderCallbacks<List<Post>> {
    public static final String FEED_ID = "feedId";
    public static final String FEED_NAME = "feedName";
    public static final String FEED_URL = "feedUrl";

    private int feedId;
    private TextView emptyView;
    private String feedName;
    private String feedUrl;
    private PostsAdapter adapter;
    private ListView listView;
    private boolean updatingFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posts_activity);

        updatingFinished = false;

        Intent starter = getIntent();
        feedId = starter.getIntExtra(FEED_ID, -1);
        feedName = starter.getStringExtra(FEED_NAME);
        feedUrl = starter.getStringExtra(FEED_URL);

        emptyView = (TextView) findViewById(android.R.id.empty);
        listView = (ListView) findViewById(android.R.id.list);

        adapter = new PostsAdapter(this, new ArrayList<Post>());
        listView.setAdapter(adapter);

        setTitle(feedName);

        Intent postsIntent = new Intent(this, RssIntentService.class);
        postsIntent.putExtra(RssIntentService.FEED_ID, feedId);
        postsIntent.putExtra(RssIntentService.FEED_URL, feedUrl);
        postsIntent.putExtra(RssIntentService.RECEIVER, new PostsReceiver(new Handler()));
        startService(postsIntent);

        getLoaderManager().initLoader(1, null, PostsActivity.this);
    }

    @Override
    public Loader<List<Post>> onCreateLoader(int i, Bundle bundle) {
        return new PostsLoader(this, feedId);
    }

    @Override
    public void onLoadFinished(Loader<List<Post>> listLoader, List<Post> posts) {
        adapter.setData(posts);
        if (updatingFinished && posts.size() == 0) {
            emptyView.setText(R.string.posts_no_posts_msg);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Post>> listLoader) {
        adapter = new PostsAdapter(this, new ArrayList<Post>());
        listView.setAdapter(adapter);
    }

    private class PostsReceiver extends ResultReceiver {
        public PostsReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int code, Bundle data) {
            updatingFinished = true;
            if (code == -1) {
                String err = data.getString("error");
                Toast.makeText(PostsActivity.this, err, Toast.LENGTH_LONG).show();

                if (adapter.getCount() == 0) {
                    emptyView.setText(R.string.posts_no_posts_msg);
                }
            } else {
                String lastPost = data.getString("lastPost");
                if (adapter.getCount() != 0) {
                    Post lastLoadedPost = (Post) adapter.getItem(0);
                    Log.i("current last post:", lastLoadedPost.title);
                    Log.i("fresh last post:", lastPost);
                    if (!lastLoadedPost.title.equals(lastPost)) {
                        getLoaderManager().restartLoader(1, null, PostsActivity.this);
                    }
                } else if (!TextUtils.isEmpty(lastPost)) {
                    Log.i("fresh last post:", lastPost);
                    getLoaderManager().restartLoader(1, null, PostsActivity.this);
                } else {
                    emptyView.setText(R.string.posts_no_posts_msg);
                }
            }
        }
    }
}
