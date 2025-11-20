package com.example.ac_crudconapi.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ac_crudconapi.database.entity.Usuario;

import java.util.List;

@Dao
public interface UsuarioDao {
    @Query("SELECT * FROM usuarios")
    List<Usuario> obtenerTodos();
    
    @Query("SELECT * FROM usuarios WHERE id = :id")
    Usuario obtenerPorId(int id);
    
    @Query("SELECT * FROM usuarios WHERE esPendienteSincronizar = 1")
    List<Usuario> obtenerPendientesSincronizar();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(Usuario usuario);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodos(List<Usuario> usuarios);
    
    @Update
    void actualizar(Usuario usuario);
    
    @Query("DELETE FROM usuarios")
    void eliminarTodos();
}

