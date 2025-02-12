package com.example.as7room.UI;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.as7room.R;
import com.example.as7room.databinding.FragmentAgregarTareaBinding;
import com.example.as7room.Model.Tarea;
import android.graphics.drawable.Drawable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FragmentAgregarTarea extends Fragment {

    private FragmentAgregarTareaBinding binding;
    private String rutaImagenSeleccionada = "";
    private TareasViewModel tareasViewModel;
    private Calendar calendario = Calendar.getInstance();

    private FirebaseAuth mAuth;
    private FirebaseUser usuarioActual;

    private final ActivityResultLauncher<String> obtenerImagenLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::manejarImagenSeleccionada
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAgregarTareaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        usuarioActual = mAuth.getCurrentUser();

        tareasViewModel = new ViewModelProvider(this).get(TareasViewModel.class);
        binding.btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        binding.btnFecha.setOnClickListener(v -> seleccionarFecha());
        binding.btnGuardar.setOnClickListener(v -> guardarTarea());
    }

    private void abrirGaleria() {
        obtenerImagenLauncher.launch("image/*");
    }

    private void manejarImagenSeleccionada(Uri uriImagen) {
        if (uriImagen != null) {
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(uriImagen);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                binding.imagenTarea.setImageBitmap(bitmap);
                rutaImagenSeleccionada = uriImagen.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_question, null);
            binding.imagenTarea.setImageDrawable(drawable);
        }
    }

    private void seleccionarFecha() {
        int dia = calendario.get(Calendar.DAY_OF_MONTH);
        int mes = calendario.get(Calendar.MONTH);
        int año = calendario.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view, year, month, dayOfMonth) -> {
                    calendario.set(year, month, dayOfMonth);
                    String fecha = dayOfMonth + "/" + (month + 1) + "/" + year;
                    binding.btnFecha.setText(fecha);
                },
                año, mes, dia
        );
        datePickerDialog.show();
    }

    private void guardarTarea() {
        String nombre = binding.editNombre.getText().toString();
        String descripcion = binding.editDescripcion.getText().toString();
        String fecha = binding.btnFecha.getText().toString();

        if (nombre.isEmpty() || descripcion.isEmpty() || fecha.equals("Seleccionar fecha")) {
            binding.editNombre.setError("Este campo es obligatorio");
            binding.editDescripcion.setError("Este campo es obligatorio");
            return;
        }

        String autor = (usuarioActual != null) ? usuarioActual.getEmail() : "anonymous";

        byte[] imagenBytes = convertirImagenABytes(rutaImagenSeleccionada);
        Tarea nuevaTarea = new Tarea(nombre, descripcion, calendario.getTimeInMillis(), imagenBytes, autor);
        tareasViewModel.insertar(nuevaTarea);
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    private byte[] convertirImagenABytes(String uriImagen) {
        if (uriImagen.isEmpty()) {
            Bitmap defaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_question);
            return convertirBitmapABytes(defaultImage);
        }
        try {
            Uri uri = Uri.parse(uriImagen);
            ContentResolver resolver = getActivity().getContentResolver();
            InputStream inputStream = resolver.openInputStream(uri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return inputStream.readAllBytes();
            } else {
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                return bytes;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] convertirBitmapABytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
