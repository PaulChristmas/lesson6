package retloko.org.rssreader;

import android.content.AsyncTaskLoader;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import java.util.*;

public class PostsLoader extends AsyncTaskLoader<List<Post>> {
    int feedId;

    public PostsLoader(Context context, int feedId) {
        super(context);

        this.feedId = feedId;
    }

    @Override
    public List<Post> loadInBackground() {
        List<Post> posts = new ArrayList<Post>();

        Uri requestUri = ContentUris.withAppendedId(RssContentProvider.POSTS_CONTENT_URI, feedId);

        Cursor cursor = getContext().getContentResolver().query(requestUri, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(0);
            int feedId = cursor.getInt(1);
            String title = cursor.getString(2);
            String summary = cursor.getString(3);
            String link = cursor.getString(4);
            posts.add(new Post(id, feedId, title, link, summary));
            cursor.moveToNext();
        }
        cursor.close();

        return posts;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
