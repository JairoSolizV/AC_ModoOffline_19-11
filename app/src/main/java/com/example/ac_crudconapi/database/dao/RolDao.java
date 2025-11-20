package com.example.ac_crudconapi.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ac_crudconapi.database.entity.Rol;

import java.util.List;

@Dao
public interface RolDao {
    @Query("SELECT * FROM roles")
    List<Rol> obtenerTodos();
    
    @Query("SELECT * FROM roles WHERE id = :id")
    Rol obtenerPorId(int id);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Rol rol);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodos(List<Rol> roles);
}

