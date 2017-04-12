package com.example.teddy.top10downloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.xmlListView);

        downloadUrl("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
    }
    //called when time to inflate activities menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu); //already a context so can call getInflater
        return true;
    }

    //called when item is selected from options menu.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String feedUrl;

        switch(id) { //finds which item was selected
            case R.id.mnuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml";
                break;
            case R.id.mnuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=10/xml";
                break;
            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=10/xml";
                break;
            default: //should always have default return super. for sub menus in future.
                return super.onOptionsItemSelected(item);

        }
        downloadUrl(feedUrl); //downloads the xml.
        return true;
    }
    //private helper to download the xml.
    private void downloadUrl(String feedUrl) {
        Log.d(TAG, "downloadUrl: starting Asynctask");
        DownloadData downloadData = new DownloadData(); //creates class to find xml online and and puts it to the app
        downloadData.execute(feedUrl);
        Log.d(TAG, "downloadUrl: done");
    }

    //An Async Task that runs in the background that parses the information.
    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        //gets back a string and parses it. Then uses the created Adapter to list it
        // in a more readable format than the ArrayAdapter.
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications(); //calls the Parse Applications to put into FeedEntries.
            parseApplications.parse(s);
            //use own created Adapter instead of ArrayAdapter.
            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record,
                    parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);

        }

        //created for AsyncTask to run the download in the background.
        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground: starts with " + params[0]);
            String rssFeed = downloadXML(params[0]);
            if (rssFeed == null) { //if there is no feed
                Log.e(TAG, "doInBackground: Error Downloading" );
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder(); //stringbuilder to save memory

            try {
                URL url = new URL(urlPath); //takes in url
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //tries to open connection
                int response = connection.getResponseCode(); //check response code(should be 200)
                Log.d(TAG, "downloadXML: the response code was " + response);
//                InputStream inputStream = connection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader);
                //uses buffered reader by getting the inputStream from the connection
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500]; //arbitrary length
                while (true) {
                    charsRead = reader.read(inputBuffer); //read from the reader into the Buffer
                    if (charsRead < 0) { //no more characters
                        break;
                    }
                    if (charsRead > 0) { //otherwise should append to the result
                        xmlResult.append(String.copyValueOf(inputBuffer,0, charsRead));
                    }
                }
                reader.close();
                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL" + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data: " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security Exception: Needs permission?" + e.getMessage() );
                //e.printStackTrace(); //prints out stack trace
            }
            return null;
        }
    }
}
