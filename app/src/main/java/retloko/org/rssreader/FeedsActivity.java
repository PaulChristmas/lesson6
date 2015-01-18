package retloko.org.rssreader;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.*;

public class FeedsActivity extends ListActivity implements LoaderManager.LoaderCallbacks<List<Feed>> {
    FeedsAdapter adapter;
    private ListView listView;
    private Button addFeedBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feeds_activity);

        listView = (ListView) findViewById(android.R.id.list);
        addFeedBtn = (Button) findViewById(R.id.add_feed);

        adapter = new FeedsAdapter(this, new ArrayList<Feed>());
        listView.setAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);

        addFeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(FeedsActivity.this);
                dialog.setTitle(R.string.add_feed_dialog_title);
                dialog.setContentView(R.layout.add_feed);
                Button dialogOK = (Button) dialog.findViewById(R.id.dialogOK);
                final EditText dialogName = (EditText) dialog.findViewById(R.id.dialogName);
                final EditText dialogUrl = (EditText) dialog.findViewById(R.id.dialogUrl);

                dialogOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues values = new ContentValues();
                        String name = dialogName.getText().toString();
                        String url = dialogUrl.getText().toString();
                        values.put("name", name);
                        values.put("url", url);
                        getContentResolver().insert(RssContentProvider.FEEDS_CONTENT_URI, values);
                        getLoaderManager().restartLoader(0, null, FeedsActivity.this);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }

    @Override
    public Loader<List<Feed>> onCreateLoader(int i, Bundle bundle) {
        return new FeedsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Feed>> listLoader, List<Feed> feeds) {
        adapter.setData(feeds);
    }

    @Override
    public void onLoaderReset(Loader<List<Feed>> listLoader) {
        adapter = new FeedsAdapter(this, new ArrayList<Feed>());
        listView.setAdapter(adapter);
    }
}
