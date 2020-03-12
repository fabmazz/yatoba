package org.frazzmark.yatoba.app;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.*;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import org.frazzmark.yatoba.app.util.*;
import org.frazzmark.yatoba.app.util.OsmDatabaseHelper.OsmDatabaseContract;

import java.util.ArrayList;


public class BusStopsActivity extends AppCompatActivity{
    private ArrayList<BusStop> fermate = new ArrayList<BusStop>();
    RecyclerView recView;
    ProgressDialog progress;
    Context con;
    StopAdapter stopAdapter;
    FragmentManager fragmentManager;

    private SharedPreferences sharedpref;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if(progress.isShowing()) progress.dismiss();
                int resultCode = bundle.getInt("result");
                //todo add check system for error
            }
        }
    };
    //double FIVE_HUNDRED_METRES = 0.002697; //this should be 300 metres
    private final double TWO_HUNDRED_METRES = 0.001798; //this should be 200 metres

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startButton = (Button) findViewById(R.id.StartTaskbutton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ordinaFermate();
            }
        });
        Button startJSONButton = (Button) findViewById(R.id.startJSONButton);
        startJSONButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryDatabase(45.062792, 7.678908);
            }
        });

        registerReceiver(receiver,new IntentFilter(OsmCallerService.PACKAGE));

        stopAdapter = new StopAdapter(fermate);
        recView = (RecyclerView) findViewById(R.id.cardList);
        recView.setAdapter(stopAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recView.setLayoutManager(llm);

        fragmentManager = getSupportFragmentManager();
        }

    @Override
    protected void onStart() {
        super.onStart();
        con = getApplicationContext();
        sharedpref = con.getSharedPreferences("Last started",Context.MODE_PRIVATE);

    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(OsmCallerService.PACKAGE));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.action_update_db){
            updateDatabase();
        }
        if(id == R.id.action_get_busstop){
            queryDatabase(45.062792, 7.678908);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //This shows a ProgressDialog, simpler version
    public void updateDatabase(){
        progress = new ProgressDialog(BusStopsActivity.this, R.style.Theme_AppCompat_Dialog);
        progress.setTitle(R.string.wait_title);
        progress.setMessage(getString(R.string.catolog_update_message));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.show();
        Intent intent = new Intent(this, OsmCallerService.class);
        startService(intent);
    }
    @TargetApi(11)
    protected void queryDatabase(double lat, double lon) {
        int ref;
        double later, loner;
        String query;
        String result = "";

        SQLiteOpenHelper dbHelper = new OsmDatabaseHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                OsmDatabaseContract.StopTable._ID,
                OsmDatabaseContract.StopTable.COLUMN_NAME_STOP_REF,
                OsmDatabaseContract.StopTable.COLUMN_NAME_LATITUDE,
                OsmDatabaseContract.StopTable.COLUMN_NAME_LONGITUDE
        };
        double upperlat = lat + TWO_HUNDRED_METRES;
        double lowerlat = lat - TWO_HUNDRED_METRES;
        double upperlon = lon + TWO_HUNDRED_METRES;
        double lowerlon = lon - TWO_HUNDRED_METRES;

        String sortOrder = OsmDatabaseContract.StopTable.COLUMN_NAME_STOP_REF;
        String whereclause = "( "+OsmDatabaseContract.StopTable.COLUMN_NAME_LATITUDE+" BETWEEN "+ lowerlat+" AND "+upperlat+ ") AND ("
                + OsmDatabaseContract.StopTable.COLUMN_NAME_LONGITUDE+ " BETWEEN "+ lowerlon+" AND "+upperlon+")";
        DownloadBusInfo runnable;
        Cursor cursor = db.query(OsmDatabaseContract.StopTable.TABLE_NAME, projection,whereclause,null,null,null,sortOrder);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            ref = cursor.getInt(cursor.getColumnIndexOrThrow(OsmDatabaseContract.StopTable.COLUMN_NAME_STOP_REF));
            later = cursor.getDouble(cursor.getColumnIndexOrThrow(OsmDatabaseContract.StopTable.COLUMN_NAME_LATITUDE));
            loner = cursor.getDouble(cursor.getColumnIndexOrThrow(OsmDatabaseContract.StopTable.COLUMN_NAME_LONGITUDE));
            runnable = new DownloadBusInfo(new DownloadBusInfo.OnDownloadCompleted() {
                @Override
                public void downloadCompleted(BusStop stop) {
                    if(stop!=null) aggiungiFermata(stop); //scartare la fermata se riceve null
                }
            });
            BusStop newstop = new BusStop(ref,later, loner);
            if(Build.VERSION.SDK_INT >= 11) {
                runnable.executeOnExecutor(DownloadBusInfo.THREAD_POOL_EXECUTOR, newstop);
            }
            else {
                runnable.execute(newstop);
            }
            result += ref + System.getProperty("bus_line.separator");
            cursor.moveToNext();
        }
        //infotext.setText(result);
        cursor.close();
        db.close();
    }
    protected void aggiungiFermata(BusStop stop) {
        boolean lineFound = false;
        for(BusStop.Line line : stop.getLines()){
           // Log.d("aggiungiFermata", "Bus stop "+stop.getRef()+": found line #" + line.getNumber());
            lineFound = true;
        }
        if(!lineFound) Log.w("aggiungiFermata", "Bus stop "+stop.getRef()+":No lines found");
        fermate.add(stop);
        stopAdapter.notifyDataSetChanged();
    }

    //todo put this in a better place
    //this code is useful for ordering by distance
    protected void ordinaFermate(){
        ArrayList<BusStop> nuovefermate;
        nuovefermate = orderStopsByDistance(fermate, 45.062792,7.678908);
        stopAdapter.replaceStops(nuovefermate);
        stopAdapter.notifyDataSetChanged();
    }
    public ArrayList<BusStop> orderStopsByDistance(ArrayList<BusStop> stops, double latitude, double longitude){
        ArrayList<BusStop> newlist = new ArrayList<BusStop>();
        ArrayList<Double> distances = new ArrayList<Double>();
        double distance;
        int position;
        boolean ok;
        for(BusStop stop: stops){
            ok=false;
            distance = Distance(stop.getLatitude(), stop.getLongitude(), latitude, longitude);
            if(newlist.size()!=0)
            for(BusStop newstop : newlist){
                position = newlist.indexOf(newstop);
                if(distance < distances.get(position)){
                    newlist.add(position,stop);
                    distances.add(position, distance);
                    ok=true;
                    Log.d("orderBusStop", "stop found: put stop in position " + position + " with distance " + distance);
                    break;
                }
            }
            if(!ok){
                newlist.add(stop);
                distances.add(distance);
                Log.d("orderBusStop", "stop missing. put stop in position " + newlist.indexOf(stop) + " with distance " + distance);
            }
        }

        return newlist;
    }
    public double Distance (double lat1, double lon1, double lat2, double lon2){
        double distlat,distlon, dist;
        if(lat1 >= lat2) distlat = lat1-lat2;
        else distlat = lat2-lat1;
        if(lon1 >= lon2) distlon = lon1-lon2;
        else distlon=lon2-lon1;
        dist = Math.hypot(distlat,distlon);
        return dist;
    }

}

