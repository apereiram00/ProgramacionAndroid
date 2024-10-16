package com.example.cumpleaosonboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.cumpleaosonboard.databinding.FragmentCalendarioBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.nextButton.setOnClickListener(v -> {
            String fechaInput = binding.editTextDate.getText().toString().trim();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date fechaNacimiento = sdf.parse(fechaInput);
                int edad = calcularEdad(fechaNacimiento);
                Bundle bundle = new Bundle();
                bundle.putInt("edad", edad);
                Navigation.findNavController(v).navigate(R.id.action_calendarioFragment_to_resultadoFragment, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private int calcularEdad(Date fechaNacimiento) {
        Calendar fechaCalendar = Calendar.getInstance();
        fechaCalendar.setTime(fechaNacimiento);
        Calendar diaActual = Calendar.getInstance();
        int edad = diaActual.get(Calendar.YEAR) - fechaCalendar.get(Calendar.YEAR);
        if (diaActual.get(Calendar.DAY_OF_YEAR) < fechaCalendar.get(Calendar.DAY_OF_YEAR)) {
            edad--;
        }
        return edad;
    }
}
