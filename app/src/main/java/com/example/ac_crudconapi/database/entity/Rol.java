package com.example.ac_crudconapi.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "roles")
public class Rol {
    @PrimaryKey
    public int id;
    
    public String nombre;
    
    public Rol() {
    }
    
    public Rol(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    
    @Override
    public String toString() {
        return nombre != null ? nombre : "Rol " + id;
    }
}

