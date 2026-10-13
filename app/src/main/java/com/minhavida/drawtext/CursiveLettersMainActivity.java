package com.minhavida.drawtext;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/** Entry point for cursive exercises, which require their own guides and model. */
public class CursiveLettersMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_categories);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.cursive_letters);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        findViewById(R.id.card_letter_vowel).setOnClickListener(v ->
                showComingSoon(R.string.cursive_vowels));
        findViewById(R.id.card_letter_consonant).setOnClickListener(v ->
                showComingSoon(R.string.cursive_consonants));
        ((android.widget.TextView) findViewById(R.id.category_vowels_title))
                .setText(R.string.cursive_vowels_coming_soon);
        ((android.widget.TextView) findViewById(R.id.category_consonants_title))
                .setText(R.string.cursive_consonants_coming_soon);
    }

    private void showComingSoon(int title) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(R.string.cursive_coming_soon)
                .setPositiveButton(android.R.string.ok, null)
                .show();
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
