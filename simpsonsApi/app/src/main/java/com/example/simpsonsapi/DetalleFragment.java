package com.example.simpsonsapi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.simpsonsapi.databinding.FragmentDetalleBinding;

public class DetalleFragment extends Fragment {

    private FragmentDetalleBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetalleBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String character = bundle.getString("character");
            String quote = bundle.getString("quote");
            String imageUrl = bundle.getString("imageUrl");
            binding.nombrePersonaje.setText(character);
            binding.quotePersonaje.setText(quote);

            Glide.with(requireContext())
                    .load(imageUrl)
                    .into(binding.imagenPersonaje);
        }

        binding.btnVolver.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
