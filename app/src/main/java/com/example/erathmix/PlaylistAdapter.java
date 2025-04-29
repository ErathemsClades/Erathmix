package com.example.erathmix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private final List<String> playlistNames;
    private final OnItemClickListener listener;

    // Listener interface for click events
    public interface OnItemClickListener {
        void onItemClick(String playlistName);
    }

    // Adapter constructor
    public PlaylistAdapter(List<String> playlistNames, OnItemClickListener listener) {
        this.playlistNames = playlistNames;
        this.listener = listener;
    }

    @Override
    public PlaylistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlaylistAdapter.ViewHolder holder, int position) {
        String playlistName = playlistNames.get(position);
        holder.textView.setText(playlistName);

        // Handle item click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(playlistName));
    }

    @Override
    public int getItemCount() {
        return playlistNames.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);
        }
    }
}
