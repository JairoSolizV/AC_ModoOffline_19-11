package com.example.ac_crudconapi.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(
    tableName = "usuarios",
    foreignKeys = @ForeignKey(
        entity = Rol.class,
        parentColumns = "id",
        childColumns = "rolId",
        onDelete = ForeignKey.RESTRICT
    ),
    indices = {@Index("rolId")}
)
public class Usuario {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String nombre;
    
    public String email;
    
    public int rolId;
    
    // Campo para el modo offline
    public boolean esPendienteSincronizar;
    
    public Usuario() {
    }
    
    public Usuario(int id, String nombre, String email, int rolId, boolean esPendienteSincronizar) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rolId = rolId;
        this.esPendienteSincronizar = esPendienteSincronizar;
    }
}

