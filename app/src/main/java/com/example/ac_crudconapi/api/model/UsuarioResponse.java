package com.example.ac_crudconapi.api.model;

public class UsuarioResponse {
    public int id;
    public String nombre;
    public String email;
    public int rolId;
    public RolResponse rol;
    
    public UsuarioResponse() {
    }
    
    public UsuarioResponse(int id, String nombre, String email, int rolId, RolResponse rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rolId = rolId;
        this.rol = rol;
    }
}

