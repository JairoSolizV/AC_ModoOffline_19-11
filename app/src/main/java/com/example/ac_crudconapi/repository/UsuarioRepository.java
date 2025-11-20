package com.example.ac_crudconapi.repository;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ac_crudconapi.api.RetrofitClient;
import com.example.ac_crudconapi.api.model.RolResponse;
import com.example.ac_crudconapi.api.model.UsuarioRequest;
import com.example.ac_crudconapi.api.model.UsuarioResponse;
import com.example.ac_crudconapi.database.AppDatabase;
import com.example.ac_crudconapi.database.entity.Rol;
import com.example.ac_crudconapi.database.entity.Usuario;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuarioRepository {
    private static final String TAG = "UsuarioRepository";
    private AppDatabase database;
    private Context context;
    private ExecutorService executorService;
    
    public UsuarioRepository(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    // Verificar si hay conexión a internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    // FLUJO DE LECTURA: Obtener Roles
    public void obtenerRoles(OnRolesLoadedListener listener) {
        if (isNetworkAvailable()) {
            // Intentar obtener de la API
            RetrofitClient.getInstance().getApiService().obtenerRoles().enqueue(
                new Callback<List<RolResponse>>() {
                    @Override
                    public void onResponse(Call<List<RolResponse>> call, Response<List<RolResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Descargar datos y sobrescribir en SQLite
                            List<RolResponse> rolesResponse = response.body();
                            Log.d(TAG, "Roles recibidos de la API: " + rolesResponse.size());
                            executorService.execute(() -> {
                                try {
                                    List<Rol> roles = convertirRolesResponse(rolesResponse);
                                    database.rolDao().insertarTodos(roles);
                                    Log.d(TAG, "Roles insertados en SQLite: " + roles.size());
                                    
                                    // Notificar al listener
                                    List<Rol> rolesLocales = database.rolDao().obtenerTodos();
                                    if (listener != null) {
                                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                            listener.onRolesLoaded(rolesLocales != null ? rolesLocales : new java.util.ArrayList<>());
                                        });
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error al procesar roles de la API", e);
                                    obtenerRolesDesdeSQLite(listener);
                                }
                            });
                        } else {
                            Log.w(TAG, "Respuesta de API no exitosa: " + response.code());
                            // Si falla, obtener de SQLite
                            obtenerRolesDesdeSQLite(listener);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<RolResponse>> call, Throwable t) {
                        Log.e(TAG, "Error al obtener roles de la API", t);
                        // Si falla, obtener de SQLite
                        obtenerRolesDesdeSQLite(listener);
                    }
                }
            );
        } else {
            // Red apagada, obtener de SQLite
            obtenerRolesDesdeSQLite(listener);
        }
    }
    
    private void obtenerRolesDesdeSQLite(OnRolesLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<Rol> roles = database.rolDao().obtenerTodos();
                Log.d(TAG, "Roles obtenidos de SQLite: " + (roles != null ? roles.size() : 0));
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onRolesLoaded(roles != null ? roles : new java.util.ArrayList<>());
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener roles de SQLite", e);
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onRolesLoaded(new java.util.ArrayList<>());
                    });
                }
            }
        });
    }
    
    // FLUJO DE LECTURA: Obtener Usuarios
    public void obtenerUsuarios(OnUsuariosLoadedListener listener) {
        if (isNetworkAvailable()) {
            // Intentar obtener de la API
            RetrofitClient.getInstance().getApiService().obtenerUsuarios().enqueue(
                new Callback<List<UsuarioResponse>>() {
                    @Override
                    public void onResponse(Call<List<UsuarioResponse>> call, Response<List<UsuarioResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Descargar datos y sobrescribir en SQLite
                            List<UsuarioResponse> usuariosResponse = response.body();
                            executorService.execute(() -> {
                                // Limpiar usuarios existentes y agregar los nuevos
                                database.usuarioDao().eliminarTodos();
                                List<Usuario> usuarios = convertirUsuariosResponse(usuariosResponse);
                                database.usuarioDao().insertarTodos(usuarios);
                                
                                // Notificar al listener
                                List<Usuario> usuariosLocales = database.usuarioDao().obtenerTodos();
                                if (listener != null) {
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                        listener.onUsuariosLoaded(usuariosLocales);
                                    });
                                }
                            });
                        } else {
                            // Si falla, obtener de SQLite
                            obtenerUsuariosDesdeSQLite(listener);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<UsuarioResponse>> call, Throwable t) {
                        Log.e(TAG, "Error al obtener usuarios de la API", t);
                        // Si falla, obtener de SQLite
                        obtenerUsuariosDesdeSQLite(listener);
                    }
                }
            );
        } else {
            // Red apagada, obtener de SQLite
            obtenerUsuariosDesdeSQLite(listener);
        }
    }
    
    private void obtenerUsuariosDesdeSQLite(OnUsuariosLoadedListener listener) {
        executorService.execute(() -> {
            List<Usuario> usuarios = database.usuarioDao().obtenerTodos();
            if (listener != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    listener.onUsuariosLoaded(usuarios);
                });
            }
        });
    }
    
    // FLUJO DE ESCRITURA: Guardar Usuario
    public void guardarUsuario(String nombre, String email, int rolId, OnUsuarioSavedListener listener) {
        UsuarioRequest usuarioRequest = new UsuarioRequest(nombre, email, rolId);
        
        if (isNetworkAvailable()) {
            // Intentar POST a la API
            RetrofitClient.getInstance().getApiService().crearUsuario(usuarioRequest).enqueue(
                new Callback<UsuarioResponse>() {
                    @Override
                    public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Exitoso: guardar en SQLite con esPendienteSincronizar = false
                            UsuarioResponse usuarioResponse = response.body();
                            executorService.execute(() -> {
                                Usuario usuario = new Usuario(
                                    usuarioResponse.id,
                                    usuarioResponse.nombre,
                                    usuarioResponse.email,
                                    usuarioResponse.rolId,
                                    false // No está pendiente de sincronizar
                                );
                                database.usuarioDao().insertar(usuario);
                                
                                if (listener != null) {
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                        listener.onUsuarioSaved(true, "Usuario guardado exitosamente");
                                    });
                                }
                            });
                        } else {
                            // Falló: guardar en SQLite con esPendienteSincronizar = true
                            guardarUsuarioOffline(nombre, email, rolId, listener);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                        Log.e(TAG, "Error al guardar usuario en la API", t);
                        // Falló: guardar en SQLite con esPendienteSincronizar = true
                        guardarUsuarioOffline(nombre, email, rolId, listener);
                    }
                }
            );
        } else {
            // Modo offline: guardar en SQLite con esPendienteSincronizar = true
            guardarUsuarioOffline(nombre, email, rolId, listener);
        }
    }
    
    private void guardarUsuarioOffline(String nombre, String email, int rolId, OnUsuarioSavedListener listener) {
        executorService.execute(() -> {
            Usuario usuario = new Usuario(0, nombre, email, rolId, true); // Pendiente de sincronizar
            long id = database.usuarioDao().insertar(usuario);
            
            if (listener != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    listener.onUsuarioSaved(true, "Usuario guardado localmente. Se sincronizará cuando haya conexión.");
                });
            }
        });
    }
    
    // RESINCRONIZACIÓN: Enviar Usuarios Pendientes
    public void sincronizarUsuariosPendientes(OnSincronizacionListener listener) {
        executorService.execute(() -> {
            List<Usuario> usuariosPendientes = database.usuarioDao().obtenerPendientesSincronizar();
            
            if (usuariosPendientes.isEmpty()) {
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onSincronizacionCompleta(0, "No hay usuarios pendientes de sincronizar");
                    });
                }
                return;
            }
            
            if (!isNetworkAvailable()) {
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onSincronizacionCompleta(0, "No hay conexión a internet");
                    });
                }
                return;
            }
            
            int sincronizados = 0;
            int total = usuariosPendientes.size();
            
            for (Usuario usuario : usuariosPendientes) {
                UsuarioRequest request = new UsuarioRequest(usuario.nombre, usuario.email, usuario.rolId);
                
                try {
                    Response<UsuarioResponse> response = RetrofitClient.getInstance()
                        .getApiService()
                        .crearUsuario(request)
                        .execute();
                    
                    if (response.isSuccessful() && response.body() != null) {
                        UsuarioResponse usuarioResponse = response.body();
                        // Actualizar el usuario local con el ID del servidor y marcar como sincronizado
                        usuario.id = usuarioResponse.id;
                        usuario.esPendienteSincronizar = false;
                        database.usuarioDao().actualizar(usuario);
                        sincronizados++;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al sincronizar usuario: " + usuario.nombre, e);
                }
            }
            
            final int finalSincronizados = sincronizados;
            if (listener != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    listener.onSincronizacionCompleta(finalSincronizados, 
                        "Sincronizados: " + finalSincronizados + " de " + total);
                });
            }
        });
    }
    
    // Métodos de conversión
    private List<Rol> convertirRolesResponse(List<RolResponse> rolesResponse) {
        List<Rol> roles = new java.util.ArrayList<>();
        if (rolesResponse != null) {
            for (RolResponse r : rolesResponse) {
                if (r != null) {
                    roles.add(new Rol(r.id, r.nombre != null ? r.nombre : "Sin nombre"));
                }
            }
        }
        return roles;
    }
    
    private List<Usuario> convertirUsuariosResponse(List<UsuarioResponse> usuariosResponse) {
        List<Usuario> usuarios = new java.util.ArrayList<>();
        for (UsuarioResponse u : usuariosResponse) {
            usuarios.add(new Usuario(u.id, u.nombre, u.email, u.rolId, false));
        }
        return usuarios;
    }
    
    // Interfaces de callback
    public interface OnRolesLoadedListener {
        void onRolesLoaded(List<Rol> roles);
    }
    
    public interface OnUsuariosLoadedListener {
        void onUsuariosLoaded(List<Usuario> usuarios);
    }
    
    public interface OnUsuarioSavedListener {
        void onUsuarioSaved(boolean exito, String mensaje);
    }
    
    public interface OnSincronizacionListener {
        void onSincronizacionCompleta(int sincronizados, String mensaje);
    }
}

