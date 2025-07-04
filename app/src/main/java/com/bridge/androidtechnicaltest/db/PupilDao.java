package com.bridge.androidtechnicaltest.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

import io.reactivex.Single;
import kotlinx.coroutines.flow.Flow;

@Dao
public interface PupilDao {

    @Query("SELECT * FROM Pupils ORDER BY name ASC")
    Single<List<Pupil>> getPupils();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Pupil> pupils);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPupil(Pupil pupil);

    @Update
    void updatePupil(Pupil pupil);

    @Delete
    void deletePupil(Pupil pupil);

    @Query("SELECT * FROM Pupils WHERE pending_sync = 1")
    Single<List<Pupil>> getPendingPupils();

    @Query("SELECT * FROM Pupils WHERE pending_sync = 1")
    Flow<List<Pupil>> observePendingPupils();

    @Query("DELETE FROM Pupils")
    void clearAll();

    @Query("SELECT * FROM Pupils WHERE pupil_id = :id")
    Single<Pupil> getPupilById(Long id);

}
