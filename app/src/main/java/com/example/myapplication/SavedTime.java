package com.example.myapplication;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class SavedTime {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name="Player")
    public String player;

    @ColumnInfo(name="Level")
    public int level;

    @ColumnInfo(name="Time")
    public int time;

    @Ignore
    public SavedTime(String player,int level, int time)
    {
        this.player=player;
        this.level=level;
        this.time=time;
    }

    public SavedTime()
    {
        player="";
        id=0;
        level=0;
        time=0;
    }

    @Ignore
    public static SavedTime getSavedTimeFromString(String string)
    {
        String[] a=string.split("@");
        return new SavedTime(a[0],Integer.parseInt(a[1]),Integer.parseInt(a[2]));
    }
    @Ignore
    public static String getStringFromSavedTime(SavedTime st)
    {
        return st.player+"@"+st.level+"@"+st.time;
    }
}
