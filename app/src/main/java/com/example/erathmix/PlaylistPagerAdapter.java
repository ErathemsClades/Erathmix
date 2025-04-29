package com.example.erathmix;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PlaylistPagerAdapter extends FragmentStateAdapter {

    public PlaylistPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (position == 0) ? new CurrentPlaylistFragment() : new AllSongsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
