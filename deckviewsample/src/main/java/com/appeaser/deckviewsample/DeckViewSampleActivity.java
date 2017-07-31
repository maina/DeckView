package com.appeaser.deckviewsample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.appeaser.deckview.views.DeckChildView;
import com.appeaser.deckview.views.DeckView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

/**
 * Basic sample for DeckView.
 * Images are downloaded and cached using
 * Picasso "http://square.github.io/picasso/".
 * DeckView is *very* young & can only
 * afford basic functionality.
 */
public class DeckViewSampleActivity extends Activity {

    // View that stacks its children like a deck of cards
    DeckView<Datum> mDeckView;

    Drawable mDefaultHeaderIcon;
    ArrayList<Datum> mEntries;

    // Placeholder for when the image is being downloaded
    Bitmap mDefaultThumbnail;

    // Retain position on configuration change
    // imageSize to pass to http://lorempixel.com
    int scrollToChildIndex = -1, imageSize = 500;

    // SavedInstance bundle keys
    final String CURRENT_SCROLL = "current.scroll", CURRENT_LIST = "current.list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_view_sample);

        mDeckView = (DeckView) findViewById(R.id.deckview);
        mDefaultThumbnail = BitmapFactory.decodeResource(getResources(),
                R.drawable.default_thumbnail);
        mDefaultHeaderIcon = getResources().getDrawable(R.drawable.default_header_icon);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_LIST)) {
                mEntries = savedInstanceState.getParcelableArrayList(CURRENT_LIST);
            }

            if (savedInstanceState.containsKey(CURRENT_SCROLL)) {
                scrollToChildIndex = savedInstanceState.getInt(CURRENT_SCROLL);
            }
        }

        if (mEntries == null) {
            mEntries = new ArrayList<>();

            for (int i = 1; i < 100; i++) {
                Datum datum = new Datum();
                datum.id = generateUniqueKey();
                if(i%2==0){
                    datum.link="http://www.facebook.com";
                }else if(i%3==0){
                    datum.link="http://www.twitter.com";

                }else if(i%5==0){
                    datum.link="http://www.yahoo.com";

                }
                    else {
                    datum.link = "http://www.google.com";
                }
                datum.headerTitle = "Image ID " + datum.id;
                mEntries.add(datum);
            }
        }

        // Callback implementation
        DeckView.Callback<Datum> deckViewCallback = new DeckView.Callback<Datum>() {
            @Override
            public ArrayList<Datum> getData() {
                return mEntries;
            }

            @Override
            public void loadViewData(WeakReference<DeckChildView<Datum>> dcv, Datum item) {
                loadViewDataInternal(item, dcv);
            }

            @Override
            public void unloadViewData(Datum item) {
               // Picasso.with(DeckViewSampleActivity.this).cancelRequest(item.target);
            }

            @Override
            public void onViewDismissed(Datum item) {
                mEntries.remove(item);
                mDeckView.notifyDataSetChanged();
            }

            @Override
            public void onItemClick(Datum item) {
                Toast.makeText(DeckViewSampleActivity.this,
                        "Item with title: '" + item.headerTitle + "' clicked",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNoViewsToDeck() {
                Toast.makeText(DeckViewSampleActivity.this,
                        "No views to show",
                        Toast.LENGTH_SHORT).show();
            }
        };

        mDeckView.initialize(deckViewCallback);

        if (scrollToChildIndex != -1) {
            mDeckView.post(new Runnable() {
                @Override
                public void run() {
                    // Restore scroll position
                    mDeckView.scrollToChild(scrollToChildIndex);
                }
            });
        }
    }
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.proceed();
        }
    }
    void loadViewDataInternal(final Datum datum,
                              final WeakReference<DeckChildView<Datum>> weakView) {
        // datum.target can be null
       // Picasso.with(DeckViewSampleActivity.this).cancelRequest(datum.target);
        weakView.get().onDataLoaded(datum,
                mDefaultHeaderIcon, datum.headerTitle, Color.DKGRAY,datum.link);
//        datum.target = new android.webkit.WebView(this);
//        datum.target.getSettings().setJavaScriptEnabled(true);
//        datum.target.setWebViewClient(new MyWebViewClient());
//        WebSettings settings = datum.target.getSettings();
//        settings.setDomStorageEnabled(true);
//
//        openURL(datum.target);
        // Begin loading
       // Picasso.with(DeckViewSampleActivity.this).load(datum.link).into(datum.target);
    }
    private void openURL(WebView webView) {
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.loadUrl("file:///android_asset/index.html");
        webView.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deck_view_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Add a new item to the end of the list
        if (id == R.id.action_add) {
            Datum datum = new Datum();
            datum.id = generateUniqueKey();
            datum.headerTitle = "(New) Image ID " + datum.id;
            datum.link = "http://lorempixel.com/" + imageSize + "/" + imageSize
                    + "/sports/" + "ID " + datum.id + "/";
            mEntries.add(datum);
            mDeckView.notifyDataSetChanged();
            return true;
        } else if (id == R.id.action_add_multiple) {
            // Add multiple items (between 5 & 10 items)
            // at random indices
            Random rand = new Random();

            // adding between 5 and 10 items
            int numberOfItemsToAdd = rand.nextInt(6) + 5;

            for (int i = 0; i < numberOfItemsToAdd; i++) {
                int atIndex = mEntries.size() > 0 ?
                        rand.nextInt(mEntries.size()) : 0;

                Datum datum = new Datum();
                datum.id = generateUniqueKey();
                datum.link = "http://lorempixel.com/" + imageSize + "/" + imageSize
                        + "/sports/" + "ID " + datum.id + "/";
                datum.headerTitle = "(New) Image ID " + datum.id;
                mEntries.add(atIndex, datum);
            }

            mDeckView.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save current scroll and the list
        int currentChildIndex = mDeckView.getCurrentChildIndex();
        outState.putInt(CURRENT_SCROLL, currentChildIndex);
        outState.putParcelableArrayList(CURRENT_LIST, mEntries);

        super.onSaveInstanceState(outState);
    }

    // Generates a key that will remain unique
    // during the application's lifecycle
    private static int generateUniqueKey() {
        return ++KEY;
    }

    private static int KEY = 0;
}
