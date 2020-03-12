package org.frazzmark.yatoba.app.util;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.frazzmark.yatoba.app.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Fabio on 21/07/2015.
 */
public class OsmCallerService extends IntentService {
    public static final String RESULT = "result";
    OsmDatabaseHelper mHelper;
    //private Context context;
    private int result = Activity.RESULT_CANCELED;
    public static final String PACKAGE = "org.frazzmark.yatoba.app.util.OsmCall";
    final static String osmURL ="http://overpass-api.de/api/interpreter";
    final static String OSMQUERY = "[out:json];area[\"name\"=\"Torino\"][\"boundary\"=\"administrative\"][\"admin_level\"=\"8\"];node[\"highway\"=\"bus_stop\"](area);out body;";
    public OsmCallerService(){
        super("OsmCallerService");
    }
    /*
    public OsmCallerService(Context context){
        this();
        this.context = context;
    }
    */
    public String makePOSTRequest(String URL, String POSTParam){
        String response = null;
        URL url;
        HttpURLConnection connection;
        try{
            url = new URL(URL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
            stream.writeBytes(POSTParam);
            stream.flush();
            stream.close();

            int responseCode = connection.getResponseCode();
            if(responseCode==200){
                BufferedReader inreader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder responseBuffer = new StringBuilder();
                while((inputLine=inreader.readLine())!= null){
                    responseBuffer.append(inputLine);

                }
                inreader.close();
                response=responseBuffer.toString();
                //lista = new JSONObject(response);
            }
            else if(responseCode == 429){
                /*todo
                there is another connection running, wait for it
                 */

            }
            else if(responseCode==400){
                System.out.println(R.string.syntax_error);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    float toFloat(String string){
        return Float.valueOf(string.trim());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("OsmDatabase", "Service started");
        String dataString = intent.getDataString();
        JSONObject root;
        JSONArray fermate = null;
        JSONObject item,tags;
        float lon,lat;
        int ref;
        long lastrow= -6;
        String refStr;
        ContentValues values;

        String rawdata = makePOSTRequest(osmURL, OSMQUERY); //perform the query
        Log.d("yatoba", "Database update: data downloaded");
        mHelper = new OsmDatabaseHelper(getApplicationContext());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        try {
            root = new JSONObject(rawdata);
            fermate = root.getJSONArray("elements");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db.execSQL("DROP TABLE IF EXISTS "+OsmDatabaseContract.StopTable.TABLE_NAME);
        db.execSQL(OsmDatabaseContract.StopTable.SQL_CREATE_TABLE);
        assert fermate != null;
        for(int i=0; i<fermate.length(); i++){
            values = new ContentValues();
            try {
                item = fermate.getJSONObject(i);
                lon = toFloat(item.getString("lon"));
                lat = toFloat(item.getString("lat"));
                tags = item.getJSONObject("tags");
                refStr = tags.getString("ref").trim();
                if(Character.isDigit(refStr.charAt(0))) { //alcune fermate, come quelle della metro, hanno una sigla
                    ref = Integer.parseInt(refStr);
                    values.put(OsmDatabaseContract.StopTable.COLUMN_NAME_STOP_REF, ref);
                    values.put(OsmDatabaseContract.StopTable.COLUMN_NAME_LATITUDE, lat);
                    values.put(OsmDatabaseContract.StopTable.COLUMN_NAME_LONGITUDE, lon);
                    //controlla se esistono gia' le vecchie info
                    /*updated = db.update(OsmDatabaseContract.StopTable.TABLE_NAME, values, OsmDatabaseContract.StopTable.COLUMN_NAME_STOP_REF + " = " + ref, null);
                    Log.w("yatoba", "DatabaseUpdate: Rows updated: " + updated);
                    if (updated == 0) */
                    lastrow = db.insert(OsmDatabaseContract.StopTable.TABLE_NAME, null, values);
                }
            } catch (JSONException e) {
                if(e.getMessage().equals("No value for ref")) Log.w("error", "Bus stop missing ref");
                else e.printStackTrace();
                publishResults(result);
            } catch(NumberFormatException ex){
                ex.printStackTrace();
                publishResults(result);
            }
        }
        if(lastrow>0) Log.d("yatoba", "Database: Added "+lastrow+" rows");
        else Log.w("yatoba","Database: No rows added");
        db.execSQL(OsmDatabaseContract.StopTable.CREATE_INDEX_BY_REF);
        db.close();
        result = Activity.RESULT_OK;
        publishResults(Activity.RESULT_OK);
    }
    private void publishResults(int result) {
        Intent intent = new Intent();
        intent.putExtra("result", result);
        intent.setAction(PACKAGE);
        intent.setPackage("org.frazzmark.yatoba.app");
        sendBroadcast(intent);
    }
}

