package retloko.org.rssreader;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.content.*;

public class RssContentProvider extends ContentProvider {

    private static final int URI_FEEDS = 0;
    private static final int URI_FEEDS_ID = 1;
    private static final int URI_POSTS = 2;
    private static final String DB_NAME = "rssdb";
    private static final int DB_VERSION = 1;
    private static final String FEEDS_TABLE = "feeds";
    private static final String FEEDS_PATH = FEEDS_TABLE;
    private static final String POSTS_TABLE = "posts";
    private static final String POSTS_PATH = POSTS_TABLE;
    private static final String AUTHORITY = "ru.ifmo.md.lesson6";
    public static final Uri FEEDS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FEEDS_PATH);
    public static final Uri POSTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + POSTS_PATH);
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, FEEDS_PATH, URI_FEEDS);
        uriMatcher.addURI(AUTHORITY, FEEDS_PATH + "/#", URI_FEEDS_ID);
        uriMatcher.addURI(AUTHORITY, POSTS_PATH + "/#", URI_POSTS);
    }

    RssDbHelper dbHelper;

    public boolean onCreate() {
        dbHelper = new RssDbHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case URI_FEEDS:
                queryBuilder.setTables(FEEDS_TABLE);
                break;

            case URI_POSTS:
                queryBuilder.setTables(POSTS_TABLE);
                String feedId = uri.getLastPathSegment();
                queryBuilder.appendWhere("feed_id = " + feedId);
                break;

            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final String tableName;

        switch (uriMatcher.match(uri)) {
            case URI_FEEDS:
                tableName = FEEDS_TABLE;
                break;

            case URI_POSTS:
                tableName = POSTS_TABLE;
                contentValues.put("feed_id", uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        long rowId = db.insert(tableName, null, contentValues);
        Uri resultUri = ContentUris.withAppendedId(uri, rowId);

        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int rowsDeleted;

        switch (uriMatcher.match(uri)) {
            case URI_FEEDS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(FEEDS_TABLE, "_id = " + id, selectionArgs);
                } else {
                    rowsDeleted = db.delete(FEEDS_TABLE, selection + "and _id = " + id, selectionArgs);
                }
                break;

            case URI_POSTS:
                String feedId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(POSTS_TABLE, "feed_id = " + feedId, selectionArgs);
                } else {
                    rowsDeleted = db.delete(POSTS_TABLE, selection + "and feed_id = " + feedId, selectionArgs);
                }
                break;

            default:
                throw new IllegalArgumentException("Bad URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // We do not require update in our implementation
        throw new IllegalArgumentException("Bad URI: " + uri);
    }

    private static class RssDbHelper extends SQLiteOpenHelper {
        public RssDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("pragma foreign_keys = on;");
            db.execSQL("create table feeds ("
                    + "_id integer primary key autoincrement, "
                    + "name text, "
                    + "url text);");
            db.execSQL("create table posts ("
                    + "_id integer primary key autoincrement, "
                    + "feed_id integer, "
                    + "title text, "
                    + "link text, "
                    + "summary text);");

            for (Feed feed : Feed.PREINSTALLED_FEEDS) {
                db.execSQL("insert into feeds (name, url) values (?, ?)", new String[]{feed.name, feed.url});
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i2) {
            db.execSQL("drop table if exists feeds");
            db.execSQL("drop table if exists posts");
            onCreate(db);
        }
    }
}
