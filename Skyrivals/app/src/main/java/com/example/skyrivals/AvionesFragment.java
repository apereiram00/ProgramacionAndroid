package com.example.skyrivals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.skyrivals.databinding.FragmentAvionesBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class AvionesFragment extends Fragment {
    private FragmentAvionesBinding binding;
    private DetallesViewModel detallesViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAvionesBinding.inflate(inflater, container, false);
        detallesViewModel = new ViewModelProvider(requireActivity()).get(DetallesViewModel.class);
        setupViewPagerWithTabs();
        return binding.getRoot();
    }

    private void setupViewPagerWithTabs() {
        AvionPagerAdapter adapter = new AvionPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Luftwaffe");
                    break;
                case 1:
                    tab.setText("Usaaf");
                    break;
                case 2:
                    tab.setText("Raf");
                    break;
                case 3:
                    tab.setText("Ussr");
                    break;
            }
        }).attach();
    }
}
