// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.chinagram.Model.PerfilViewModel;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentEditarPerfilBinding;
import com.google.firebase.auth.FirebaseAuth;

public class EditarPerfilFragment extends Fragment {

    private FragmentEditarPerfilBinding binding;
    private PerfilViewModel perfilViewModel;
    private String userId;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditarPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        perfilViewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        // Cargo los datos actuales del usuario
        perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                binding.nombreEditText.setText(usuario.nombre);
                binding.bioEditText.setText(usuario.biografia);
                Glide.with(this)
                        .load(usuario.fotoPerfilUrl.startsWith("drawable://") ? getResourceFromDrawable(usuario.fotoPerfilUrl) : usuario.fotoPerfilUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(binding.fotoPerfilImageView);
            }
        });

        // Configuro el launcher para seleccionar fotos
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    Log.d("EditarPerfilFragment", "Foto seleccionada: " + imageUri);
                    binding.progressBar.setVisibility(View.VISIBLE);
                    Glide.with(this).load(imageUri).into(binding.fotoPerfilImageView); // Muestro la imagen local inmediatamente
                    perfilViewModel.uploadProfileImage(imageUri, userId);
                    perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
                        if (usuario != null && !usuario.fotoPerfilUrl.startsWith("drawable://")) {
                            Log.d("EditarPerfilFragment", "Foto subida exitosamente: " + usuario.fotoPerfilUrl);
                            binding.progressBar.setVisibility(View.GONE);
                            Glide.with(this).load(usuario.fotoPerfilUrl).into(binding.fotoPerfilImageView); // Actualizo con la URL de Imgur
                        }
                    });
                }
            }
        });

        // Listener para cambiar la foto
        binding.cambiarFotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        // Listener para guardar cambios y volver
        binding.aceptarButton.setOnClickListener(v -> {
            String nuevoNombre = binding.nombreEditText.getText().toString().trim();
            String nuevaBio = binding.bioEditText.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                binding.progressBar.setVisibility(View.VISIBLE);
                perfilViewModel.updateProfile(userId, nuevoNombre, nuevaBio);
                perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
                    if (usuario != null && usuario.nombre.equals(nuevoNombre) && usuario.biografia.equals(nuevaBio)) {
                        binding.progressBar.setVisibility(View.GONE);
                        Navigation.findNavController(v).popBackStack(R.id.perfilFragment, false);
                    }
                });
            }
        });
    }

    private int getResourceFromDrawable(String drawableUrl) {
        Log.d("EditarPerfilFragment", "Cargando drawable: " + drawableUrl);
        switch (drawableUrl) {
            case "drawable://xi_jinping": return R.drawable.xi_jinping;
            case "drawable://panda": return R.drawable.panda;
            case "drawable://china_flag": return R.drawable.china_flag;
            case "drawable://panda_feed": return R.drawable.panda_feed;
            case "drawable://rice_field": return R.drawable.rice_field;
            case "drawable://factory": return R.drawable.factory;
            default: return R.drawable.placeholder;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}