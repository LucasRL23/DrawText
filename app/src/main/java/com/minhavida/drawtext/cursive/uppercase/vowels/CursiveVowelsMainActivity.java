package com.minhavida.drawtext.cursive.uppercase.vowels;

import com.minhavida.drawtext.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CursiveVowelsMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cursive_vowels);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.cursive_vowels);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        int[] cards = {R.id.card_cursive_a, R.id.card_cursive_e, R.id.card_cursive_i,
                R.id.card_cursive_o, R.id.card_cursive_u};
        String[] letters = {"A", "E", "I", "O", "U"};
        for (int i = 0; i < cards.length; i++) {
            final String letter = letters[i];
            findViewById(cards[i]).setOnClickListener(v -> {
                Intent intent = new Intent(this, CursiveLetterActivity.class);
                intent.putExtra(CursiveLetterActivity.EXTRA_LETTER, letter);
                startActivity(intent);
            });
        }
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
