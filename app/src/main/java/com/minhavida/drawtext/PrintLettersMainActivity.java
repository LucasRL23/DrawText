package com.minhavida.drawtext;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/** Groups the existing print-letter exercises by vowel and consonant. */
public class PrintLettersMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_categories);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.print_letters);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        findViewById(R.id.card_letter_vowel).setOnClickListener(v ->
                openLetters(PrintLetterVowelMainActivity.class, R.id.cb_enable_sound_vowels));
        findViewById(R.id.card_letter_consonant).setOnClickListener(v ->
                openLetters(PrintLetterConsonantMainActivity.class, R.id.cb_enable_sound_consonants));
    }

    private void openLetters(Class<?> activity, int soundCheckbox) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("enable_auto_sound", ((CheckBox) findViewById(soundCheckbox)).isChecked());
        startActivity(intent);
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
