package org.frazzmark.yatoba.app.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
/**
 * Created by fabio on 8/2/15.
 */
public class OsmDatabaseHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String FLOAT_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "busStop.db";
    public OsmDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(OsmDatabaseContract.StopTable.SQL_CREATE_TABLE);
        db.execSQL(OsmDatabaseContract.LinesTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(OsmDatabaseContract.StopTable.SQL_DELETE_ENTRIES);
        db.execSQL(OsmDatabaseContract.LinesTable.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    public static class OsmDatabaseContract {
        // To prevent someone from accidentally instantiating the contract class,
        // give it an empty constructor.
        public OsmDatabaseContract(){}
        private static final String TEXT_TYPE = " TEXT";
        private static final String INT_TYPE = " INTEGER";
        private static final String FLOAT_TYPE = " REAL";
        private static final String COMMA_SEP = ", ";
        /* Inner class that defines the table contents */
        public static abstract class StopTable implements BaseColumns {
            public static final String TABLE_NAME = "fermate";
            public static final String COLUMN_NAME_STOP_REF = "stopid";
            public static final String COLUMN_NAME_STOP_NAME = "name";
            public static final String COLUMN_NAME_LATITUDE = "latitude";
            public static final String COLUMN_NAME_LONGITUDE = "longitude";
            public static final String COLUMN_NAME_LINES = "linee";

            static final String SQL_CREATE_TABLE =
                    "CREATE TABLE " + StopTable.TABLE_NAME + " (" +
                            StopTable._ID + INT_TYPE+ " PRIMARY KEY," +
                            StopTable.COLUMN_NAME_STOP_REF + INT_TYPE + COMMA_SEP +
                            StopTable.COLUMN_NAME_STOP_NAME+ TEXT_TYPE + COMMA_SEP +
                            StopTable.COLUMN_NAME_LATITUDE+ FLOAT_TYPE + COMMA_SEP +
                            StopTable.COLUMN_NAME_LONGITUDE+ FLOAT_TYPE+ COMMA_SEP+
                            COLUMN_NAME_LINES + TEXT_TYPE+
                            ")";

            static final String SQL_DELETE_ENTRIES =
                    "DROP TABLE IF EXISTS " + StopTable.TABLE_NAME;
            public static final String COLUMN_NAME_NULLABLE = "null";
            static final String REF_IDX_NAME = "refidx";
            static final String CREATE_INDEX_BY_REF = "CREATE INDEX "+ REF_IDX_NAME +" ON "
                    + TABLE_NAME +"("+ COLUMN_NAME_STOP_REF +")";

        }
        public static abstract class LinesTable implements BaseColumns {
            public static final String TABLE_NAME = "linesbytime";
            public static final String COLUMN_NAME_STOP_REF = "stopid";
            public static final String COLUMN_NAME_TIME = "time";
            public static final String COLUMN_NAME_LINE = "line";
            static final String SQL_CREATE_TABLE =
                    "CREATE TABLE " + LinesTable.TABLE_NAME + " ("+
                            LinesTable._ID + INT_TYPE + " PRIMARY KEY," +
                            LinesTable.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP+
                            LinesTable.COLUMN_NAME_LINE + INT_TYPE + COMMA_SEP+
                            LinesTable.COLUMN_NAME_STOP_REF + INT_TYPE + " )";
            public static final String TIME_IDX_NAME="timeidx";
            static final String SQL_DELETE_ENTRIES =
                    "DROP TABLE IF EXISTS "+ LinesTable.TABLE_NAME;
            static final String CREATE_INDEX_BY_TIME = "CREATE INDEX "+TIME_IDX_NAME+ " ON " + LinesTable.TABLE_NAME+
                    " ( datetime("+COLUMN_NAME_TIME+") )";
        }
    }
}



