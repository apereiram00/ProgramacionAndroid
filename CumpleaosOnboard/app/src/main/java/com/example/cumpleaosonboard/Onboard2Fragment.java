package com.example.cumpleaosonboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.cumpleaosonboard.databinding.FragmentOnboard2Binding;

public class Onboard2Fragment extends Fragment {

    private FragmentOnboard2Binding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboard2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.nextButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_onboard2Fragment_to_onboard3Fragment)
        );

        binding.skipButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_onboard2Fragment_to_calendarioFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
