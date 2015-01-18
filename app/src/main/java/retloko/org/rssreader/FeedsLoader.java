package retloko.org.rssreader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import java.util.*;

public class FeedsLoader extends AsyncTaskLoader<List<Feed>> {
    public FeedsLoader(Context context) {
        super(context);
    }

    @Override
    public List<Feed> loadInBackground() {
        List<Feed> feeds = new ArrayList<Feed>();

        Cursor cursor = getContext().getContentResolver().query(RssContentProvider.FEEDS_CONTENT_URI, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String uri = cursor.getString(2);
            feeds.add(new Feed(id, name, uri));
            cursor.moveToNext();
        }
        cursor.close();

        return feeds;
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
