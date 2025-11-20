package com.example.ac_crudconapi;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ac_crudconapi.database.AppDatabase;
import com.example.ac_crudconapi.database.entity.Rol;
import com.example.ac_crudconapi.database.entity.Usuario;
import com.example.ac_crudconapi.repository.UsuarioRepository;
import com.example.ac_crudconapi.ui.UsuarioAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextInputEditText editTextNombre;
    private TextInputEditText editTextEmail;
    private Spinner spinnerRol;
    private Button btnGuardar;
    private Button btnSincronizar;
    private RecyclerView recyclerViewUsuarios;
    
    private UsuarioRepository repository;
    private AppDatabase database;
    private UsuarioAdapter adapter;
    private List<Rol> roles;
    private ArrayAdapter<Rol> rolSpinnerAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Inicializar componentes
        initViews();
        
        // Inicializar base de datos y repositorio
        database = AppDatabase.getInstance(this);
        repository = new UsuarioRepository(this);
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Cargar roles y usuarios al iniciar
        cargarRoles();
        cargarUsuarios();
        
        // Configurar listeners
        btnGuardar.setOnClickListener(v -> guardarUsuario());
        btnSincronizar.setOnClickListener(v -> sincronizarPendientes());
    }
    
    private void initViews() {
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextEmail = findViewById(R.id.editTextEmail);
        spinnerRol = findViewById(R.id.spinnerRol);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnSincronizar = findViewById(R.id.btnSincronizar);
        recyclerViewUsuarios = findViewById(R.id.recyclerViewUsuarios);
    }
    
    private void setupRecyclerView() {
        adapter = new UsuarioAdapter(new ArrayList<>(), database);
        recyclerViewUsuarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsuarios.setAdapter(adapter);
    }
    
    private void cargarRoles() {
        // Primero intentar cargar desde la base de datos local inmediatamente
        new Thread(() -> {
            List<Rol> rolesLocales = database.rolDao().obtenerTodos();
            runOnUiThread(() -> {
                if (rolesLocales != null && !rolesLocales.isEmpty()) {
                    actualizarSpinner(rolesLocales);
                }
            });
        }).start();
        
        // Luego intentar sincronizar con la API
        repository.obtenerRoles(new UsuarioRepository.OnRolesLoadedListener() {
            @Override
            public void onRolesLoaded(List<Rol> rolesList) {
                if (rolesList == null) {
                    rolesList = new ArrayList<>();
                }
                
                // Log para depurar
                android.util.Log.d("MainActivity", "Roles cargados: " + rolesList.size());
                
                if (rolesList.isEmpty()) {
                    // Si no hay roles, intentar insertar roles por defecto
                    insertarRolesPorDefecto();
                } else {
                    actualizarSpinner(rolesList);
                }
            }
        });
    }
    
    private void actualizarSpinner(List<Rol> rolesList) {
        roles = rolesList;
        
        // Configurar Spinner
        rolSpinnerAdapter = new ArrayAdapter<>(
            MainActivity.this,
            android.R.layout.simple_spinner_item,
            roles
        );
        rolSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(rolSpinnerAdapter);
        
        // Notificar cambio
        if (rolSpinnerAdapter != null) {
            rolSpinnerAdapter.notifyDataSetChanged();
        }
    }
    
    private void insertarRolesPorDefecto() {
        new Thread(() -> {
            List<Rol> rolesPorDefecto = new ArrayList<>();
            rolesPorDefecto.add(new Rol(1, "Administrador"));
            rolesPorDefecto.add(new Rol(2, "Usuario"));
            rolesPorDefecto.add(new Rol(3, "Invitado"));
            
            database.rolDao().insertarTodos(rolesPorDefecto);
            
            runOnUiThread(() -> {
                actualizarSpinner(rolesPorDefecto);
                Toast.makeText(MainActivity.this, 
                    "Roles por defecto cargados. Conecte a la API para sincronizar.", 
                    Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
    
    private void cargarUsuarios() {
        repository.obtenerUsuarios(new UsuarioRepository.OnUsuariosLoadedListener() {
            @Override
            public void onUsuariosLoaded(List<Usuario> usuarios) {
                adapter.updateUsuarios(usuarios);
            }
        });
    }
    
    private void guardarUsuario() {
        String nombre = editTextNombre.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un nombre", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un email", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Rol rolSeleccionado = (Rol) spinnerRol.getSelectedItem();
        if (rolSeleccionado == null) {
            Toast.makeText(this, "Por favor seleccione un rol", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int rolId = rolSeleccionado.id;
        
        // Deshabilitar botón mientras se guarda
        btnGuardar.setEnabled(false);
        
        repository.guardarUsuario(nombre, email, rolId, new UsuarioRepository.OnUsuarioSavedListener() {
            @Override
            public void onUsuarioSaved(boolean exito, String mensaje) {
                btnGuardar.setEnabled(true);
                
                if (exito) {
                    Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                    // Limpiar formulario
                    editTextNombre.setText("");
                    editTextEmail.setText("");
                    // Recargar usuarios
                    cargarUsuarios();
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + mensaje, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void sincronizarPendientes() {
        btnSincronizar.setEnabled(false);
        
        repository.sincronizarUsuariosPendientes(new UsuarioRepository.OnSincronizacionListener() {
            @Override
            public void onSincronizacionCompleta(int sincronizados, String mensaje) {
                btnSincronizar.setEnabled(true);
                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                // Recargar usuarios para actualizar la lista
                cargarUsuarios();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando se vuelve a la actividad
        cargarUsuarios();
    }
}
