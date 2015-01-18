package retloko.org.rssreader;

import android.app.IntentService;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import android.net.Uri;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import java.io.*;
import java.net.*;
import android.content.*;

public class RssIntentService extends IntentService {
    public static final String FEED_ID = "feedId";
    public static final String FEED_URL = "feedUrl";
    public static final String RECEIVER = "receiver";

    private String lastPost;

    public RssIntentService() {
        super("RssIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int feedId = intent.getIntExtra(FEED_ID, -1);
        String feedUrl = intent.getStringExtra(FEED_URL);
        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);

        try {
            String xml = getXmlFromUrl(feedUrl);
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            RssSaxParser saxParser = new RssSaxParser(feedId);

            xmlReader.setContentHandler(saxParser);
            xmlReader.parse(new InputSource(is));

            if (receiver != null) {
                Bundle bundle = new Bundle();
                bundle.putString("lastPost", lastPost);
                receiver.send(0, bundle);
            }
        } catch (IOException e) {
            onError(receiver, e.toString());
        } catch (ParserConfigurationException e) {
            onError(receiver, e.toString());
        } catch (SAXException e) {
            onError(receiver, e.toString());
        }
    }

    private void onError(ResultReceiver receiver, String err) {
        Bundle bundle = new Bundle();
        bundle.putString("error", err);
        if (receiver != null) {
            receiver.send(-1, bundle);
        }
    }

    private String getXmlFromUrl(String urlString) throws IOException {
        StringBuilder output = new StringBuilder("");

        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setRequestMethod("GET");
        httpConnection.connect();

        if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream stream = httpConnection.getInputStream();

            String contentType = connection.getContentType();
            String[] values = contentType.split(";");
            String encoding = "";

            for (String value : values) {
                value = value.trim();

                if (value.toLowerCase().startsWith("charset=")) {
                    encoding = value.substring("charset=".length());
                }
            }

            if ("".equals(encoding)) {
                encoding = httpConnection.getContentEncoding() != null ? httpConnection.getContentEncoding() : "utf-8";
            }

            BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(stream, encoding));
            String s;
            while ((s = buffer.readLine()) != null)
                output.append(s);
        }

        return output.toString();
    }

    private class RssSaxParser extends DefaultHandler {
        private String characters;
        private ContentValues values;
        private boolean saveFlag;
        private int feedId;

        public RssSaxParser(int feedId) {
            this.feedId = feedId;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String newCharacters = new String(ch, start, length);
            if (saveFlag) {
                characters += newCharacters;
            }
        }

        @Override
        public void startDocument() {
            Uri requestUri = ContentUris.withAppendedId(RssContentProvider.POSTS_CONTENT_URI, feedId);
            getContentResolver().delete(requestUri, null, null);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            // bigger blocks
            if (qName.equals("item")) {
                values = new ContentValues();
                values.put("feed_id", feedId);
            }

            // lesser blocks
            else if (qName.equals("title") || qName.equals("link") || qName.equals("description")) {
                saveFlag = true;
                characters = "";
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            saveFlag = false;

            // bigger blocks
            if (qName.equals("item")) {
                Uri requestUri = ContentUris.withAppendedId(RssContentProvider.POSTS_CONTENT_URI, feedId);
                getContentResolver().insert(requestUri, values);
            }

            // lesser blocks
            else if (qName.equals("title")) {
                if (values != null) {
                    if (TextUtils.isEmpty(lastPost)) {
                        lastPost = characters;
                    }
                    values.put("title", characters);
                }
            } else if (qName.equals("description")) {
                if (values != null) {
                    values.put("summary", characters);
                }
            } else if (qName.equals("link")) {
                if (values != null) {
                    values.put("link", characters);
                }
            }
        }
    }
}
