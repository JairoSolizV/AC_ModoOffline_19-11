package com.example.ac_crudconapi.api;

import com.example.ac_crudconapi.api.model.RolResponse;
import com.example.ac_crudconapi.api.model.UsuarioRequest;
import com.example.ac_crudconapi.api.model.UsuarioResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    // GET /api/roles
    @GET("api/roles")
    Call<List<RolResponse>> obtenerRoles();
    
    // GET /api/usuarios
    @GET("api/usuarios")
    Call<List<UsuarioResponse>> obtenerUsuarios();
    
    // POST /api/usuarios
    @POST("api/usuarios")
    Call<UsuarioResponse> crearUsuario(@Body UsuarioRequest usuario);
}

