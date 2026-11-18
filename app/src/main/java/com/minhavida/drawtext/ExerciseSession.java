package com.minhavida.drawtext;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

/** Shared level selection and feedback for numbers and print-letter exercises. */
public final class ExerciseSession {
    private static final String LEVEL_STATE = "exercise_level";
    private static final String SUCCESSES_STATE = "exercise_successes";
    private static final int[] LEVEL_ITEMS = {R.id.menu_nivel1, R.id.menu_nivel2,
            R.id.menu_nivel3, R.id.menu_nivel4, R.id.menu_nivel5};
    private static final double[] CONFIDENCE = {0.8, 0.9, 0.97, 0.99, 0.999};
    private static final int SUCCESSES_TO_HIDE_GUIDE = 5;

    private final AppCompatActivity activity;
    private final CanvasView canvas;
    private final ImageView feedback;
    private final View check;
    private final View clear;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaPlayer sound;
    private int levelIndex;
    private int successes;
    private boolean feedbackShowing;

    public ExerciseSession(AppCompatActivity activity, Bundle savedState) {
        this.activity = activity;
        canvas = activity.findViewById(R.id.canvas);
        feedback = activity.findViewById(R.id.iv_feedback);
        feedback.setVisibility(View.GONE);
        check = activity.findViewById(R.id.bt_find);
        clear = activity.findViewById(R.id.bt_clear);
        if (savedState != null) {
            levelIndex = Math.max(0, Math.min(CONFIDENCE.length - 1, savedState.getInt(LEVEL_STATE)));
            successes = Math.max(0, Math.min(SUCCESSES_TO_HIDE_GUIDE, savedState.getInt(SUCCESSES_STATE)));
        }
    }

    public boolean createMenu(Menu menu) {
        activity.getMenuInflater().inflate(R.menu.options_menu, menu);
        menu.findItem(LEVEL_ITEMS[levelIndex]).setChecked(true);
        return true;
    }

    public boolean selectLevel(MenuItem item) {
        for (int index = 0; index < LEVEL_ITEMS.length; index++) {
            if (item.getItemId() == LEVEL_ITEMS[index]) {
                levelIndex = index;
                item.setChecked(true);
                return true;
            }
        }
        return false;
    }

    public double getRequiredConfidence() {
        return CONFIDENCE[levelIndex];
    }

    public float getGuideOpacity() {
        return 1f - (float) successes / SUCCESSES_TO_HIDE_GUIDE;
    }

    public boolean isFeedbackShowing() {
        return feedbackShowing;
    }

    public void showFeedback(boolean correct, Runnable reloadGuide) {
        if (feedbackShowing) return;
        feedbackShowing = true;
        if (correct) successes = Math.min(SUCCESSES_TO_HIDE_GUIDE, successes + 1);
        check.setEnabled(false);
        clear.setEnabled(false);
        canvas.setEnabled(false);
        feedback.setImageResource(correct ? R.drawable.like : R.drawable.dislike);
        feedback.setVisibility(View.VISIBLE);
        feedback.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.zoom_out));
        playSound(correct ? R.raw.correct_answer : R.raw.wrong_answer, correct ? 0.025f : 0.05f);
        handler.postDelayed(() -> {
            releaseSound();
            feedback.clearAnimation();
            feedback.setVisibility(View.GONE);
            canvas.clearCanvas();
            if (correct) reloadGuide.run();
            canvas.setEnabled(true);
            clear.setEnabled(true);
            check.setEnabled(true);
            feedbackShowing = false;
        }, 2000);
    }

    public void playSound(int resource, float volume) {
        releaseSound();
        sound = MediaPlayer.create(activity, resource);
        if (sound != null) {
            sound.setVolume(volume, volume);
            sound.setOnCompletionListener(player -> releaseSound());
            sound.start();
        }
    }

    private void releaseSound() {
        if (sound != null) {
            sound.release();
            sound = null;
        }
    }

    public void saveState(Bundle state) {
        state.putInt(LEVEL_STATE, levelIndex);
        state.putInt(SUCCESSES_STATE, successes);
    }

    public void close() {
        handler.removeCallbacksAndMessages(null);
        releaseSound();
    }
}
