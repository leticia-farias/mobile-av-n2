package com.example.avn2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class TrilhasDB extends SQLiteOpenHelper {
    private static final String DATABASE = "trilha_database";
    private static final int VERSION = 1;

    public TrilhasDB(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_way_points_table =
                "CREATE TABLE waypoints(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "latitude NUMERIC NOT NULL, longitude NUMERIC NOT NULL, altitude NUMERIC NOT NULL);";
        db.execSQL(create_way_points_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String drop_waypoints_table = "DROP TABLE IF EXISTS waypoints";
        db.execSQL(drop_waypoints_table);
        onCreate(db);
    }

    public void registrarWaypoint (Waypoint waypoint) {
        ContentValues values = new ContentValues();
        values.put("latitude", waypoint.getLatitude());
        values.put("longitude", waypoint.getLongitude());
        values.put("altitude", waypoint.getAltitude());
        getWritableDatabase().insert("waypoints", null, values);
    }

    public ArrayList<Waypoint> recuperarWaypoints () {
        ArrayList<Waypoint> waypoints = new ArrayList<>();

        String[] columns = {"id", "latitude", "longitude", "altitude"};
        try (Cursor cursor = getWritableDatabase().query("waypoints", columns, null, null, null, null, null)) {

            while (cursor.moveToNext()) {
                Waypoint waypoint = new Waypoint();
                waypoint.setId(cursor.getLong(0));
                waypoint.setLatitude(cursor.getDouble(1));
                waypoint.setLongitude(cursor.getDouble(2));
                waypoint.setAltitude(cursor.getDouble(3));
                waypoints.add(waypoint);
            }
        }
        return waypoints;
    }

    public void apagaTrilha () {
        getWritableDatabase().execSQL("DELETE FROM waypoints");
    }
}
