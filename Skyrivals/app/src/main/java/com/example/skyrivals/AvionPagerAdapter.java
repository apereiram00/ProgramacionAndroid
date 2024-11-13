package com.example.skyrivals;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AvionPagerAdapter extends FragmentStateAdapter {

    public AvionPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new LuftwaffeFragment();
            case 1:
                return new UsaafFragment();
            case 2:
                return new RafFragment();
            case 3:
                return new UssrFragment();
            default:
                return new LuftwaffeFragment(); // Default case
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Número total de pestañas
    }
}
