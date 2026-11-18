package com.minhavida.drawtext;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.minhavida.drawtext.print.uppercase.consonants.PrintLetterConsonantActivity;
import com.minhavida.drawtext.print.uppercase.vowels.PrintLetterVowelActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExerciseSessionTest {
    @Test
    public void numbersKeepSingleAttemptAndSelectedLevel() throws Exception {
        verifyExercise(NumberActivity.class, "number", 1, "1", R.id.iv_number);
    }

    @Test
    public void printVowelsKeepSingleAttemptAndSelectedLevel() throws Exception {
        verifyExercise(PrintLetterVowelActivity.class, "letter", 0, "A", R.id.iv_letter_vowel);
    }

    @Test
    public void printConsonantsKeepSingleAttemptAndSelectedLevel() throws Exception {
        verifyExercise(PrintLetterConsonantActivity.class, "letter", 1, "B", R.id.iv_letter_consonant);
    }

    private <T extends AppCompatActivity> void verifyExercise(
            Class<T> activityClass, String extra, int value, String label, int guideId) throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, activityClass).putExtra(extra, value);
        Field classifierField = activityClass.getDeclaredField("tfClassifier");
        classifierField.setAccessible(true);
        FakeClassifier classifier = new FakeClassifier(label);
        try (ActivityScenario<T> scenario = ActivityScenario.launch(intent)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                replaceClassifier(activity, classifierField, classifier);
                Toolbar toolbar = activity.findViewById(R.id.toolbar);
                assertTrue(toolbar.getMenu().findItem(R.id.menu_nivel1).isChecked());
                CanvasView canvas = activity.findViewById(R.id.canvas);
                canvas.buildDrawingCache();
                View check = activity.findViewById(R.id.bt_find);
                check.performClick();
                check.performClick();
                check.performClick();
                assertEquals(1, classifier.calls);
                assertFalse(check.isEnabled());
                assertFalse(activity.findViewById(R.id.bt_clear).isEnabled());
                assertFalse(canvas.isEnabled());
                MotionEvent touch = MotionEvent.obtain(0, 1, MotionEvent.ACTION_DOWN, 10, 10, 0);
                try {
                    assertFalse(canvas.onTouchEvent(touch));
                } finally {
                    touch.recycle();
                }
            });
            waitForFeedback(scenario);
            scenario.onActivity(activity -> {
                assertEquals(0.8f, ((ImageView) activity.findViewById(guideId)).getAlpha(), 0.001f);
                assertTrue(activity.findViewById(R.id.canvas).isEnabled());
                Toolbar toolbar = activity.findViewById(R.id.toolbar);
                toolbar.getMenu().performIdentifierAction(R.id.menu_nivel5, 0);
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                replaceClassifier(activity, classifierField, classifier);
                assertTrue(((Toolbar) activity.findViewById(R.id.toolbar)).getMenu()
                        .findItem(R.id.menu_nivel5).isChecked());
                assertEquals(0.8f, ((ImageView) activity.findViewById(guideId)).getAlpha(), 0.001f);
                classifier.confidence = 0.85f;
                ((CanvasView) activity.findViewById(R.id.canvas)).buildDrawingCache();
                activity.findViewById(R.id.bt_find).performClick();
            });
            waitForFeedback(scenario);
            scenario.onActivity(activity -> {
                assertEquals(0.8f, ((ImageView) activity.findViewById(guideId)).getAlpha(), 0.001f);
                classifier.confidence = 1f;
                ((CanvasView) activity.findViewById(R.id.canvas)).buildDrawingCache();
                activity.findViewById(R.id.bt_find).performClick();
                assertFalse(activity.findViewById(R.id.bt_find).isEnabled());
            });
            // Recreating during feedback must consume the attempt exactly once.
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals(0.6f, ((ImageView) activity.findViewById(guideId)).getAlpha(), 0.001f);
                assertEquals(View.GONE, activity.findViewById(R.id.iv_feedback).getVisibility());
                assertTrue(activity.findViewById(R.id.bt_find).isEnabled());
            });
        }
    }

    private void replaceClassifier(AppCompatActivity activity, Field field, ImageClassifier replacement) {
        try {
            ((ImageClassifier) field.get(activity)).close();
            field.set(activity, replacement);
        } catch (IllegalAccessException error) {
            throw new AssertionError(error);
        }
    }

    private void waitForFeedback(ActivityScenario<?> scenario) {
        AtomicBoolean ready = new AtomicBoolean();
        long deadline = SystemClock.uptimeMillis() + 6000;
        while (!ready.get() && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(50);
            scenario.onActivity(activity -> ready.set(activity.findViewById(R.id.bt_find).isEnabled()));
        }
        assertTrue("Feedback should finish and enable the next attempt", ready.get());
    }

    private static class FakeClassifier extends ImageClassifier {
        private final String label;
        private float confidence = 1f;
        private int calls;

        FakeClassifier(String label) {
            this.label = label;
        }

        @Override
        public Classification recognize(float[] pixels, int channels) {
            calls++;
            Classification result = new Classification();
            result.update(confidence, label);
            return result;
        }
    }
}
