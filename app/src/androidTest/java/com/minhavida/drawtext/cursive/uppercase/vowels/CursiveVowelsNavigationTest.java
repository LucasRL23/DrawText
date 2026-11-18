package com.minhavida.drawtext.cursive.uppercase.vowels;

import com.minhavida.drawtext.R;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class CursiveVowelsNavigationTest {
    @Test
    public void allFiveCardsOpenMatchingPracticeAndReturn() {
        int[] cards = {R.id.card_cursive_a, R.id.card_cursive_e, R.id.card_cursive_i,
                R.id.card_cursive_o, R.id.card_cursive_u};
        String[] letters = {"A", "E", "I", "O", "U"};
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        try (ActivityScenario<CursiveVowelsMainActivity> scenario = ActivityScenario.launch(CursiveVowelsMainActivity.class)) {
            for (int i = 0; i < cards.length; i++) {
                onView(withId(cards[i])).perform(scrollTo(), click());
                onView(withText(context.getString(R.string.cursive_letter_title, letters[i])))
                        .check(matches(isDisplayed()));
                onView(withId(R.id.cursive_canvas)).check(matches(isDisplayed()));
                pressBack();
                onView(withId(cards[i])).check(matches(isDisplayed()));
            }
        }
    }

    @Test
    public void selectedVowelAndDrawingSurviveActivityRecreation() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, CursiveLetterActivity.class);
        intent.putExtra(CursiveLetterActivity.EXTRA_LETTER, "U");
        final float[][] saved = new float[1][];
        try (ActivityScenario<CursiveLetterActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                CursiveLetterView canvas = activity.findViewById(R.id.cursive_canvas);
                canvas.restoreDrawing(new float[]{Float.NaN, Float.NaN, 50f, 60f, 100f, 180f});
                saved[0] = canvas.getDrawingPoints();
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals(context.getString(R.string.cursive_letter_title, "U"), activity.getSupportActionBar().getTitle());
                CursiveLetterView canvas = activity.findViewById(R.id.cursive_canvas);
                assertArrayEquals(saved[0], canvas.getDrawingPoints(), 0f);
            });
        }
    }
}
