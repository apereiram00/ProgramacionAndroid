package com.example.as7room.UI;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.as7room.Model.Tarea;
import com.example.as7room.Model.TareasRepositorio;
import com.example.as7room.R;
import com.example.as7room.databinding.FragmentDetalleTareaBinding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FragmentDetalleTarea extends Fragment {

    private FragmentDetalleTareaBinding binding;
    private Tarea tarea;
    private TareasRepositorio repositorio;

    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imagenUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imagenUri);
                        binding.imagenDetalle.setImageBitmap(bitmap);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        byte[] imagenBytes = outputStream.toByteArray();
                        tarea.setImagen(imagenBytes);

                        repositorio.actualizar(tarea);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetalleTareaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repositorio = new TareasRepositorio(requireActivity().getApplication());

        if (getArguments() != null) {
            tarea = (Tarea) getArguments().getSerializable("tarea");
            binding.textoNombreDetalle.setText(tarea.getNombre());
            binding.textoDescripcionDetalle.setText(tarea.getDescripcion());

            if (tarea.getImagen() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(tarea.getImagen(), 0, tarea.getImagen().length);
                binding.imagenDetalle.setImageBitmap(bitmap);
            } else {
                binding.imagenDetalle.setImageResource(R.drawable.ic_question);
            }
        }
        binding.btnCambiarImagen.setOnClickListener(v -> abrirGaleria());

        if (tarea.isCompletada()) {
            binding.textoEstadoDetalle.setText("Estado: Completada");
        } else {
            binding.textoEstadoDetalle.setText("Estado: No Completada");
        }

        binding.btnVolverALista.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });
    }


    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        seleccionarImagenLauncher.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
