// com.example.chinagram.UI.SubirFragment.java
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

public class SubirFragment extends Fragment {

    private FragmentSubirBinding binding;
    private PostViewModel postViewModel;
    private String userId;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri selectedImageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSubirBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        binding.elegirPost.setOnClickListener(v -> openGallery());
        binding.uploadButton.setOnClickListener(v -> uploadPost());

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    Log.d("SubirFragment", "Imagen seleccionada: " + selectedImageUri);
                    showPreview(selectedImageUri);
                }
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void showPreview(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(binding.previewImageView);
    }

    private void uploadPost() {
        if (selectedImageUri != null) {
            String title = binding.titleEditText.getText().toString().trim();
            if (title.isEmpty()) {
                title = "Nueva publicación"; // Título predeterminado si no se ingresa
            }
            postViewModel.uploadPost(selectedImageUri, userId, title);
            postViewModel.getPostUploadCompleted().observe(getViewLifecycleOwner(), completed -> {
                if (completed != null && completed) {
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