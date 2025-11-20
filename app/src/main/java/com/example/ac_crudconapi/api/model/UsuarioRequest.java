package com.example.ac_crudconapi.api.model;

public class UsuarioRequest {
    public String nombre;
    public String email;
    public int rolId;
    
    public UsuarioRequest() {
    }
    
    public UsuarioRequest(String nombre, String email, int rolId) {
        this.nombre = nombre;
        this.email = email;
        this.rolId = rolId;
    }
}

