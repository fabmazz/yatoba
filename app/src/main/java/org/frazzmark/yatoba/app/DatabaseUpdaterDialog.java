package org.frazzmark.yatoba.app;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.widget.ProgressBar;
import org.frazzmark.yatoba.app.util.OsmCallerService;
/**
 * Created by fabio on 8/2/15.
 */
public class DatabaseUpdaterDialog extends AppCompatDialog {

    OsmCallerService callerTask;
    ProgressBar progressbar;

    public DatabaseUpdaterDialog(Context context) {
        super(context);
    }
    //non funziona ancora
    /*
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_db);
        progressbar = (ProgressBar) findViewById(R.id.progressBar);
        callerTask = new OsmCallerService( );
        callerTask.execute();
    }
    */
    public void onTaskFinished(){

    }
}
