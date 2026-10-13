package com.minhavida.drawtext;

import android.content.Context;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PrintLetterResourcesTest {
    @Test
    public void everyPrintLetterGuideCanBeFoundAndInflated() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        for (char letter = 'a'; letter <= 'z'; letter++) {
            String name = "dotted_letter_print_" + letter;
            int resource = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
            assertNotEquals("Missing print-letter guide: " + letter, 0, resource);
            assertNotNull("Could not load guide: " + letter, AppCompatResources.getDrawable(context, resource));
        }
    }
}
