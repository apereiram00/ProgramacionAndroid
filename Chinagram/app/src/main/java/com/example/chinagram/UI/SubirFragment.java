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
import com.example.chinagram.Model.PostViewModel;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentSubirBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SubirFragment extends Fragment {

    private FragmentSubirBinding binding;
    private PostViewModel postViewModel;
    private String idUsuario; // Renombrado: userId -> idUsuario
    private ActivityResultLauncher<Intent> lanzadorSeleccionImagen; // Renombrado: pickImageLauncher -> lanzadorSeleccionImagen
    private Uri uriImagenSeleccionada; // Renombrado: selectedImageUri -> uriImagenSeleccionada

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSubirBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idUsuario = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        binding.elegirPost.setOnClickListener(v -> abrirGaleria()); // Renombrado: openGallery -> abrirGaleria
        binding.uploadButton.setOnClickListener(v -> subirPublicacion()); // Renombrado: uploadPost -> subirPublicacion

        lanzadorSeleccionImagen = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                uriImagenSeleccionada = result.getData().getData();
                if (uriImagenSeleccionada != null) {
                    Log.d("SubirFragment", "Imagen seleccionada: " + uriImagenSeleccionada);
                    mostrarVistaPrevia(uriImagenSeleccionada); // Renombrado: showPreview -> mostrarVistaPrevia
                }
            }
        });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        lanzadorSeleccionImagen.launch(intent);
    }

    private void mostrarVistaPrevia(Uri uriImagen) { // Renombrado: imageUri -> uriImagen
        Glide.with(this)
                .load(uriImagen)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(binding.previewImageView);
    }

    private void subirPublicacion() {
        if (uriImagenSeleccionada != null) {
            String titulo = binding.titleEditText.getText().toString().trim(); // Renombrado: title -> titulo
            if (titulo.isEmpty()) {
                titulo = "Nueva publicación";
            }
            postViewModel.uploadPost(uriImagenSeleccionada, idUsuario, titulo);
            postViewModel.getPostUploadCompleted().observe(getViewLifecycleOwner(), completado -> { // Renombrado: completed -> completado
                if (completado != null && completado) {
                    Log.d("SubirFragment", "Publicación subida exitosamente, navegando al perfil");
                    Navigation.findNavController(binding.getRoot()).popBackStack(R.id.perfilFragment, false);
                }
            });
        } else {
            Log.e("SubirFragment", "No hay imagen seleccionada para subir");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}