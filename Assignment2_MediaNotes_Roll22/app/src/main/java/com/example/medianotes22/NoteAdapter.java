package com.example.medianotes22;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClicked(Note note);
    }

    private final List<Note> notes;
    private final OnNoteClickListener clickListener;

    public NoteAdapter(List<Note> notes, OnNoteClickListener clickListener) {
        this.notes = new ArrayList<>(notes);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvDescription.setText("Desc: " + safeText(note.getDescription()));
        holder.tvDate.setText("Date: " + safeText(note.getDate()));
        holder.tvExtraField.setText(getExtraFieldLabel() + ": " + safeText(note.getExtraFieldValue()));

        bindMedia(holder, note);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNoteClicked(note);
            }
        });
    }

    private void bindMedia(NoteViewHolder holder, Note note) {
        String mediaPath = note.getImagePath();
        if (mediaPath == null || mediaPath.trim().isEmpty()) {
            holder.tvMediaType.setText("Media: Not available");
            holder.ivMedia.setVisibility(View.VISIBLE);
            holder.ivMedia.setImageResource(android.R.drawable.ic_menu_report_image);
            return;
        }

        File mediaFile = new File(mediaPath);
        if (!mediaFile.exists()) {
            holder.tvMediaType.setText("Media: File missing");
            holder.ivMedia.setVisibility(View.VISIBLE);
            holder.ivMedia.setImageResource(android.R.drawable.ic_menu_report_image);
            return;
        }

        if (note.isVideo()) {
            holder.tvMediaType.setText("Media: Video attached");
            holder.ivMedia.setVisibility(View.VISIBLE);
            holder.ivMedia.setImageResource(android.R.drawable.ic_media_play);
        } else {
            holder.tvMediaType.setText("Media: Image attached");
            holder.ivMedia.setVisibility(View.VISIBLE);
            holder.ivMedia.setImageURI(Uri.fromFile(mediaFile));
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void updateNotes(List<Note> updatedNotes) {
        notes.clear();
        notes.addAll(updatedNotes);
        notifyDataSetChanged();
    }

    private String safeText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value;
    }

    private String getExtraFieldLabel() {
        String column = AssignmentConfig.getExtraFieldColumn().replace('_', ' ');
        String[] parts = column.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvDate;
        TextView tvExtraField;
        TextView tvMediaType;
        ImageView ivMedia;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvExtraField = itemView.findViewById(R.id.tvItemExtraField);
            tvMediaType = itemView.findViewById(R.id.tvItemMediaType);
            ivMedia = itemView.findViewById(R.id.ivItemMedia);
        }
    }
}
