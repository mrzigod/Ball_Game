package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM savedtime")
    List<SavedTime> getAll();

    @Query("SELECT * FROM savedtime WHERE level = :levelid ORDER BY TIME" )
    List<SavedTime> loadLevelTimes(int levelid);

    @Insert
    void insert(SavedTime time);

    @Delete
    void delete(SavedTime user);
}
