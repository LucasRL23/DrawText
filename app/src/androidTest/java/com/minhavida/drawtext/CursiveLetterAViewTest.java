package com.minhavida.drawtext;

import android.content.Context;
import android.view.MotionEvent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CursiveLetterAViewTest {
    private CursiveLetterAView newView(int width, int height) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        CursiveLetterAView view = new CursiveLetterAView(context, null);
        view.layout(0, 0, width, height);
        return view;
    }

    private void touch(CursiveLetterAView view, int action, float x, float y) {
        MotionEvent event = MotionEvent.obtain(0, 1, action, x, y, 0);
        view.onTouchEvent(event);
        event.recycle();
    }

    @Test
    public void drawingSurvivesResizeAndScreenRecreation() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            CursiveLetterAView original = newView(640, 720);
            touch(original, MotionEvent.ACTION_DOWN, 100, 200);
            touch(original, MotionEvent.ACTION_MOVE, 180, 300);
            touch(original, MotionEvent.ACTION_UP, 220, 400);
            float[] saved = original.getDrawingPoints();
            assertTrue(saved.length > 0);
            original.layout(0, 0, 960, 1080);
            assertArrayEquals(saved, original.getDrawingPoints(), 0f);
            CursiveLetterAView recreated = newView(320, 360);
            recreated.restoreDrawing(saved);
            assertArrayEquals(saved, recreated.getDrawingPoints(), 0f);
        });
    }

    @Test
    public void clearingDuringDemonstrationRemovesInk() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            CursiveLetterAView view = newView(320, 360);
            touch(view, MotionEvent.ACTION_DOWN, 70, 280);
            touch(view, MotionEvent.ACTION_UP, 130, 150);
            view.showTracing();
            view.clearDrawing();
            assertEquals(0, view.getDrawingPoints().length);
        });
    }
}
