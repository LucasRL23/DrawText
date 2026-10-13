package com.minhavida.drawtext;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CursiveLetterAActivity extends AppCompatActivity {
    private static final String DRAWING_STATE = "cursive_a_drawing";
    private CursiveLetterAView practice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cursive_letter_a);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.cursive_a_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        practice = findViewById(R.id.cursive_a_canvas);
        if (savedInstanceState != null) {
            practice.restoreDrawing(savedInstanceState.getFloatArray(DRAWING_STATE));
        }
        findViewById(R.id.show_cursive_tracing).setOnClickListener(v -> practice.showTracing());
        findViewById(R.id.clear_cursive_drawing).setOnClickListener(v -> practice.clearDrawing());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putFloatArray(DRAWING_STATE, practice.getDrawingPoints());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
