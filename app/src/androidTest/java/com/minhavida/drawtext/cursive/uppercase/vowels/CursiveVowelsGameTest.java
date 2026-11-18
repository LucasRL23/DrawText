package com.minhavida.drawtext.cursive.uppercase.vowels;

import com.minhavida.drawtext.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.PathParser;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CursiveVowelsGameTest {
    private static final int[] GRADED_GUIDES = {R.drawable.dotted_letter_cursive_e, R.drawable.dotted_letter_cursive_i,
            R.drawable.dotted_letter_cursive_o, R.drawable.dotted_letter_cursive_u};
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void packagedModelRecognizesTheGradedVowels() throws Exception {
        String letters = "EIOU";
        for (int i = 0; i < letters.length(); i++) {
            float[] points = sampleGuide(GRADED_GUIDES[i]);
            try (ActivityScenario<CursiveLetterActivity> scenario = launch(letters.substring(i, i + 1))) {
                scenario.onActivity(activity -> {
                    CursiveLetterView canvas = activity.findViewById(R.id.cursive_canvas);
                    assertNull(canvas.getModelPixels(28));
                    submit(activity, points);
                    assertEquals(context.getString(R.string.cursive_correct), status(activity));
                });
            }
        }
    }

    @Test
    public void revisedAAllowsTracingWithoutOldModelFeedback() {
        try (ActivityScenario<CursiveLetterActivity> scenario = launch("A")) {
            scenario.onActivity(activity -> {
                assertEquals(context.getString(R.string.cursive_a_correction_in_review), status(activity));
                assertEquals(View.GONE, activity.findViewById(R.id.check_cursive_drawing).getVisibility());
                assertTrue(activity.findViewById(R.id.show_cursive_tracing).performClick());
                assertTrue(activity.findViewById(R.id.clear_cursive_drawing).performClick());
            });
        }
    }

    @Test
    public void wrongVowelDoesNotAdvanceAndFiveSuccessesHideGuide() throws Exception {
        float[] correct = sampleGuide(GRADED_GUIDES[0]);
        float[] wrong = sampleGuide(GRADED_GUIDES[1]);
        try (ActivityScenario<CursiveLetterActivity> scenario = launch("E")) {
            scenario.onActivity(activity -> {
                assertTrue(((Toolbar) activity.findViewById(R.id.toolbar)).getMenu()
                        .findItem(R.id.menu_nivel1).isChecked());
                submit(activity, wrong);
                assertEquals(context.getString(R.string.cursive_retry), status(activity));
            });
            waitForNextAttempt(scenario);
            for (int completed = 0; completed < 5; completed++) {
                int count = completed;
                scenario.onActivity(activity -> {
                    assertEquals(context.getString(R.string.cursive_guide_progress, count, 5), status(activity));
                    submit(activity, correct);
                    assertEquals(context.getString(R.string.cursive_correct), status(activity));
                    activity.findViewById(R.id.check_cursive_drawing).performClick();
                });
                waitForNextAttempt(scenario);
            }
            scenario.onActivity(activity -> {
                assertEquals(context.getString(R.string.cursive_guide_hidden), status(activity));
                Toolbar toolbar = activity.findViewById(R.id.toolbar);
                assertTrue(toolbar.getMenu().performIdentifierAction(R.id.menu_nivel5, 0));
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals(context.getString(R.string.cursive_guide_hidden), status(activity));
                assertFalse(activity.findViewById(R.id.show_cursive_tracing).isEnabled());
                assertTrue(((Toolbar) activity.findViewById(R.id.toolbar)).getMenu()
                        .findItem(R.id.menu_nivel5).isChecked());
                assertEquals(View.GONE, activity.findViewById(R.id.cursive_feedback).getVisibility());
                submit(activity, correct);
                assertEquals(context.getString(R.string.cursive_correct), status(activity));
            });
        }
    }

    private ActivityScenario<CursiveLetterActivity> launch(String letter) {
        Intent intent = new Intent(context, CursiveLetterActivity.class);
        intent.putExtra(CursiveLetterActivity.EXTRA_LETTER, letter);
        return ActivityScenario.launch(intent);
    }

    private void submit(CursiveLetterActivity activity, float[] points) {
        CursiveLetterView canvas = activity.findViewById(R.id.cursive_canvas);
        canvas.restoreDrawing(points);
        View check = activity.findViewById(R.id.check_cursive_drawing);
        assertTrue(check.isEnabled());
        check.performClick();
    }

    private String status(CursiveLetterActivity activity) {
        return ((TextView) activity.findViewById(R.id.cursive_status)).getText().toString();
    }

    private void waitForNextAttempt(ActivityScenario<CursiveLetterActivity> scenario) {
        AtomicBoolean enabled = new AtomicBoolean();
        long deadline = SystemClock.uptimeMillis() + 6000;
        while (!enabled.get() && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(50);
            scenario.onActivity(activity -> enabled.set(activity.findViewById(R.id.check_cursive_drawing).isEnabled()));
        }
        assertTrue("Feedback should finish and unlock the next attempt", enabled.get());
    }

    private float[] sampleGuide(int resource) throws Exception {
        ArrayList<Float> points = new ArrayList<>();
        float[] coordinates = new float[2];
        try (XmlResourceParser xml = context.getResources().getXml(resource)) {
            while (xml.next() != XmlPullParser.END_DOCUMENT) {
                if (xml.getEventType() != XmlPullParser.START_TAG || !"path".equals(xml.getName())) continue;
                String data = xml.getAttributeValue("http://schemas.android.com/apk/res/android", "pathData");
                Path path = PathParser.createPathFromPathData(data);
                PathMeasure measure = new PathMeasure(path, false);
                points.add(Float.NaN);
                points.add(Float.NaN);
                int intervals = (int) Math.ceil(measure.getLength() / 3f);
                for (int i = 0; i <= intervals; i++) {
                    measure.getPosTan(i * measure.getLength() / intervals, coordinates, null);
                    points.add(coordinates[0]);
                    points.add(coordinates[1]);
                }
            }
        }
        float[] result = new float[points.size()];
        for (int i = 0; i < result.length; i++) result[i] = points.get(i);
        return result;
    }
}
