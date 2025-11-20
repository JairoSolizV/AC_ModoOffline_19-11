package com.example.ac_crudconapi.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ac_crudconapi.R;
import com.example.ac_crudconapi.database.AppDatabase;
import com.example.ac_crudconapi.database.entity.Rol;
import com.example.ac_crudconapi.database.entity.Usuario;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {
    private List<Usuario> usuarios;
    private AppDatabase database;
    
    public UsuarioAdapter(List<Usuario> usuarios, AppDatabase database) {
        this.usuarios = usuarios;
        this.database = database;
    }
    
    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarios.get(position);
        holder.bind(usuario);
    }
    
    @Override
    public int getItemCount() {
        return usuarios != null ? usuarios.size() : 0;
    }
    
    public void updateUsuarios(List<Usuario> nuevosUsuarios) {
        this.usuarios = nuevosUsuarios;
        notifyDataSetChanged();
    }
    
    class UsuarioViewHolder extends RecyclerView.ViewHolder {
        private TextView textNombre;
        private TextView textEmail;
        private TextView textRol;
        private TextView textEstado;
        
        UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombre);
            textEmail = itemView.findViewById(R.id.textEmail);
            textRol = itemView.findViewById(R.id.textRol);
            textEstado = itemView.findViewById(R.id.textEstado);
        }
        
        void bind(Usuario usuario) {
            textNombre.setText(usuario.nombre != null ? usuario.nombre : "");
            textEmail.setText(usuario.email != null ? usuario.email : "");
            
            // Obtener el nombre del Rol desde la base de datos
            try {
                Rol rol = database.rolDao().obtenerPorId(usuario.rolId);
                if (rol != null && rol.nombre != null) {
                    textRol.setText("Rol: " + rol.nombre);
                } else {
                    textRol.setText("Rol: No disponible");
                }
            } catch (Exception e) {
                textRol.setText("Rol: Error al cargar");
            }
            
            // Mostrar estado de sincronización
            if (usuario.esPendienteSincronizar) {
                textEstado.setText("⏳ Pendiente de sincronizar");
                textEstado.setVisibility(View.VISIBLE);
            } else {
                textEstado.setVisibility(View.GONE);
            }
        }
    }
}

