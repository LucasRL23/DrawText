package com.minhavida.drawtext;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CardView cardNumber = findViewById(R.id.card_numbers);
        cardNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent  = new Intent(MainActivity.this, NumberMainActivity.class);
                CheckBox cb = cardNumber.findViewById(R.id.cb_enable_sound_numbers);
                intent.putExtra("enable_auto_sound", cb.isChecked());
                startActivity(intent);
                //finish();
            }
        });

        findViewById(R.id.card_print_letters).setOnClickListener(v ->
                startActivity(new Intent(this, PrintLettersMainActivity.class)));
        findViewById(R.id.card_cursive_letters).setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.cursive_letters)
                        .setMessage(R.string.cursive_coming_soon)
                        .setPositiveButton(android.R.string.ok, null)
                        .show());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getResources().getString(R.string.app_name));
            bar.setDisplayHomeAsUpEnabled(false);
            bar.setHomeButtonEnabled(false);
        }
    }
}
