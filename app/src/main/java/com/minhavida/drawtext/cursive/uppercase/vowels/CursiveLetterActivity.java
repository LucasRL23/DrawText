package com.minhavida.drawtext.cursive.uppercase.vowels;

import com.minhavida.drawtext.Classification;
import com.minhavida.drawtext.ImageClassifier;
import com.minhavida.drawtext.R;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;

public class CursiveLetterActivity extends AppCompatActivity {
    public static final String EXTRA_LETTER = "cursive_letter";
    private static final String DRAWING_STATE = "cursive_drawing";
    private static final String LEVEL_STATE = "cursive_level";
    private static final String SUCCESSES_STATE = "cursive_successes";
    private static final int MODEL_INPUT_SIZE = 28;
    private static final int SUCCESSES_TO_HIDE_GUIDE = 5;
    private static final int[] LEVEL_ITEMS = {R.id.menu_nivel1, R.id.menu_nivel2,
            R.id.menu_nivel3, R.id.menu_nivel4, R.id.menu_nivel5};
    private static final float[] LEVEL_CONFIDENCE = {0.80f, 0.90f, 0.97f, 0.99f, 0.999f};
    private final Handler feedbackHandler = new Handler(Looper.getMainLooper());
    private CursiveLetterView practice;
    private View showTracing;
    private View clear;
    private View check;
    private ImageView feedback;
    private TextView status;
    private ImageClassifier classifier;
    private MediaPlayer feedbackSound;
    private String selected;
    private int levelIndex;
    private int successes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cursive_letter);
        selected = getIntent().getStringExtra(EXTRA_LETTER);
        if (selected == null || selected.length() != 1 || !"AEIOU".contains(selected)) selected = "A";
        String title = getString(R.string.cursive_letter_title, selected);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        practice = findViewById(R.id.cursive_canvas);
        practice.setLetter(selected.charAt(0));
        practice.setContentDescription(getString(R.string.cursive_canvas_description, selected));
        showTracing = findViewById(R.id.show_cursive_tracing);
        clear = findViewById(R.id.clear_cursive_drawing);
        check = findViewById(R.id.check_cursive_drawing);
        feedback = findViewById(R.id.cursive_feedback);
        status = findViewById(R.id.cursive_status);
        if (savedInstanceState != null) {
            levelIndex = savedInstanceState.getInt(LEVEL_STATE);
            successes = savedInstanceState.getInt(SUCCESSES_STATE);
            practice.restoreDrawing(savedInstanceState.getFloatArray(DRAWING_STATE));
        }
        showTracing.setOnClickListener(v -> practice.showTracing());
        clear.setOnClickListener(v -> practice.clearDrawing());
        if ("A".equals(selected)) {
            // The pilot was trained on the previous A shape and grades the revised guide as U.
            ((TextView) findViewById(R.id.cursive_instruction)).setText(R.string.cursive_a_practice_instruction);
            status.setText(R.string.cursive_a_correction_in_review);
            check.setVisibility(View.GONE);
            return;
        }
        check.setOnClickListener(v -> checkDrawing());
        updateGuide();
        try {
            classifier = ImageClassifier.create(getAssets(), "Vogais cursivas",
                    "cnn_cursive_vowels.tflite", "labels_cursive_vowels.txt",
                    MODEL_INPUT_SIZE, null, null, false, 5);
        } catch (IOException | RuntimeException error) {
            Log.e("CursiveLetterActivity", "Unable to load the cursive vowel classifier", error);
            check.setEnabled(false);
            status.setText(R.string.cursive_model_unavailable);
        }
    }

    private void checkDrawing() {
        if (classifier == null || feedback.getVisibility() == View.VISIBLE) return;
        float[] pixels = practice.getModelPixels(MODEL_INPUT_SIZE);
        if (pixels == null) {
            Toast.makeText(this, R.string.cursive_draw_first, Toast.LENGTH_SHORT).show();
            return;
        }
        Classification result = classifier.recognize(pixels, 1);
        boolean correct = selected.equals(result.getLabel())
                && result.getConf() > LEVEL_CONFIDENCE[levelIndex];
        if (correct && successes < SUCCESSES_TO_HIDE_GUIDE) successes++;

        check.setEnabled(false);
        clear.setEnabled(false);
        showTracing.setEnabled(false);
        practice.setEnabled(false);
        feedback.setImageResource(correct ? R.drawable.like : R.drawable.dislike);
        status.setText(correct ? R.string.cursive_correct : R.string.cursive_retry);
        feedback.setVisibility(View.VISIBLE);
        feedback.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_out));

        feedbackSound = MediaPlayer.create(this, correct ? R.raw.correct_answer : R.raw.wrong_answer);
        if (feedbackSound != null) {
            float volume = correct ? 0.025f : 0.05f;
            feedbackSound.setVolume(volume, volume);
            feedbackSound.start();
        }
        feedbackHandler.postDelayed(this::finishFeedback, 2000);
    }

    private void finishFeedback() {
        releaseFeedbackSound();
        feedback.clearAnimation();
        feedback.setVisibility(View.GONE);
        practice.clearDrawing();
        practice.setEnabled(true);
        check.setEnabled(true);
        clear.setEnabled(true);
        updateGuide();
    }

    private void releaseFeedbackSound() {
        if (feedbackSound != null) {
            feedbackSound.release();
            feedbackSound = null;
        }
    }

    private void updateGuide() {
        boolean guideVisible = successes < SUCCESSES_TO_HIDE_GUIDE;
        practice.setGuideOpacity(1f - (float) successes / SUCCESSES_TO_HIDE_GUIDE);
        showTracing.setEnabled(guideVisible);
        status.setText(guideVisible
                ? getString(R.string.cursive_guide_progress, successes, SUCCESSES_TO_HIDE_GUIDE)
                : getString(R.string.cursive_guide_hidden));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (feedback.getVisibility() != View.VISIBLE) {
            outState.putFloatArray(DRAWING_STATE, practice.getDrawingPoints());
        }
        outState.putInt(LEVEL_STATE, levelIndex);
        outState.putInt(SUCCESSES_STATE, successes);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ("A".equals(selected)) return false;
        getMenuInflater().inflate(R.menu.options_menu, menu);
        menu.findItem(LEVEL_ITEMS[levelIndex]).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        for (int index = 0; index < LEVEL_ITEMS.length; index++) {
            if (item.getItemId() == LEVEL_ITEMS[index]) {
                levelIndex = index;
                item.setChecked(true);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        feedbackHandler.removeCallbacksAndMessages(null);
        releaseFeedbackSound();
        if (classifier != null) classifier.close();
        super.onDestroy();
    }
}
