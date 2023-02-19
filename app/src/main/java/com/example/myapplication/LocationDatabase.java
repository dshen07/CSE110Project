package com.example.myapplication;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;

@Database(entities = {LocationItem.class}, version = 1)
public abstract class LocationDatabase extends RoomDatabase {
    public abstract LocationItemDao locationItemDao();

    private static LocationDatabase singleton = null;

    public synchronized static LocationDatabase getSingleton(Context context){
        if(singleton == null){
            singleton = LocationDatabase.makeDatabase(context);
        }

        return singleton;
    }

    static LocationDatabase makeDatabase(Context context){
        return Room.databaseBuilder(context, LocationDatabase.class, "location_app.db")
                .allowMainThreadQueries()
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        List<LocationItem> locs = LocationItem
                                .loadJSON(context, "demo_locations.json");
                        getSingleton(context).locationItemDao().insertAll(locs);
                    }
                })
                .build();
    }
}
