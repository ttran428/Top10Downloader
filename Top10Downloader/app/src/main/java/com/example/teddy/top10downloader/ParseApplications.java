package com.example.teddy.top10downloader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by teddy on 4/11/2017.
 */

public class ParseApplications {
    private static final String TAG = "ParseApplications";
    private ArrayList<FeedEntry> applications;

    public ParseApplications() {
        this.applications = new ArrayList<>(); //creates a new List to store data
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications; //returns the list
    }

    public boolean parse(String xmlData) {
        boolean status = true;
        FeedEntry currentRecord = null;
        boolean inEntry = false;
        String textValue = ""; //no text yet

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); //create a parser Factory.
            factory.setNamespaceAware(true);  //configuring parsers to be NameSpace aware
            XmlPullParser xpp = factory.newPullParser(); //create the parser
            xpp.setInput(new StringReader(xmlData));  //put in the data
            int eventType = xpp.getEventType(); //set event Type
            while (eventType != XmlPullParser.END_DOCUMENT) { //until finished
                String tagName = xpp.getName();
                //based on Event Type, choose which action to do. Most actions are just
                //editing the FeedEntry.
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        //Log.d(TAG, "parse: Starting tag for " + tagName);
                        if ("entry".equalsIgnoreCase(tagName)) { //creates new feedEntry to start parsing.
                            inEntry = true;
                            currentRecord = new FeedEntry();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        //Log.d(TAG, "parse: Ending tag for " + tagName);
                        if (inEntry) {  //sets all the variables available for each instance.
                            if ("entry".equalsIgnoreCase(tagName)) {
                                applications.add(currentRecord);
                            } else if ("name".equals(tagName)) {
                                currentRecord.setName(textValue);
                            } else if ("artist".equalsIgnoreCase(tagName)) {
                                currentRecord.setArtist(textValue);
                            } else if ("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textValue);
                            } else if ("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textValue);
                            } else if ("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textValue);
                            }
                        }
                        break;
                    default:
                        //nothing

                }
                eventType = xpp.next();
            }
//            for (FeedEntry app: applications) {  //tocheck the list has everything
//                Log.d(TAG, "parse: ***********");
//                Log.d(TAG, app.toString());
//            }
        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }
        return status;


    }


}
