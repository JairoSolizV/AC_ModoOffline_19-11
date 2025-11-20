package com.example.ac_crudconapi.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ac_crudconapi.database.dao.RolDao;
import com.example.ac_crudconapi.database.dao.UsuarioDao;
import com.example.ac_crudconapi.database.entity.Rol;
import com.example.ac_crudconapi.database.entity.Usuario;

@Database(entities = {Rol.class, Usuario.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RolDao rolDao();
    public abstract UsuarioDao usuarioDao();
    
    private static AppDatabase INSTANCE;
    
    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "app_database"
            )
            .allowMainThreadQueries() // Permitir consultas en el hilo principal (solo para desarrollo)
            .build();
        }
        return INSTANCE;
    }
}

