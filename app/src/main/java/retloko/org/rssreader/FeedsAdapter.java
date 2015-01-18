package retloko.org.rssreader;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class FeedsAdapter extends BaseAdapter {
    private final FeedsActivity activity;
    private List<Feed> data;

    public FeedsAdapter(FeedsActivity activity, List<Feed> data) {
        this.activity = activity;
        this.data = data;
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

    public void setData(List<Feed> newData) {
        data = newData;
        notifyDataSetChanged();
    }

    public void addItem(Feed what) {
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
            convertView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed, viewGroup, false);
        }
        TextView nameView = (TextView) convertView.findViewById(android.R.id.text1);
        TextView urlView = (TextView) convertView.findViewById(android.R.id.text2);

        final Feed feed = (Feed) getItem(i);

        nameView.setText(feed.name.trim());
        nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent postIntent = new Intent(activity, PostsActivity.class);

                postIntent.putExtra(PostsActivity.FEED_ID, feed.id);
                postIntent.putExtra(PostsActivity.FEED_NAME, feed.name);
                postIntent.putExtra(PostsActivity.FEED_URL, feed.url);

                activity.startActivity(postIntent);
            }
        });
        nameView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Uri requestUri = ContentUris.withAppendedId(RssContentProvider.FEEDS_CONTENT_URI, feed.id);

                activity.getContentResolver().delete(requestUri, null, null);
                activity.getLoaderManager().restartLoader(0, null, activity);

                return false;
            }
        });

        urlView.setText(feed.url.trim());

        return convertView;
    }
}
