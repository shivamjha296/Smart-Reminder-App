package com.example.medianotes22;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViewNotesActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    private NotesDatabaseHelper dbHelper;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes);
        setTitle(R.string.notes_list_title);

        RecyclerView recyclerView = findViewById(R.id.recyclerNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new NotesDatabaseHelper(this);
        adapter = new NoteAdapter(dbHelper.getAllNotes(), this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotes(true);
    }

    @Override
    public void onNoteClicked(Note note) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, note.getId());
        startActivity(intent);
    }

    private void refreshNotes(boolean showEmptyToast) {
        List<Note> notes = dbHelper.getAllNotes();
        adapter.updateNotes(notes);
        if (showEmptyToast && notes.isEmpty()) {
            Toast.makeText(this, "No notes saved yet", Toast.LENGTH_SHORT).show();
        }
    }
}
