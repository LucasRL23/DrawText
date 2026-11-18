package com.minhavida.drawtext.cursive.uppercase.vowels;

import com.minhavida.drawtext.R;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.XmlResourceParser;
import androidx.core.graphics.PathParser;
import org.xmlpull.v1.XmlPullParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

/** A resource-driven cursive letter guide, independent of the print-letter classifier. */
public class CursiveLetterView extends View {
    private static final float WIDTH = 320f;
    private static final float HEIGHT = 360f;
    private static final float TOP_GUIDE = 76f;
    private static final int INPUT_SUPERSAMPLING = 4;
    private final Paint guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint inkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint middleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint modelInkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path[] guide;
    private final Path drawing = new Path();
    private final ArrayList<Float> points = new ArrayList<>();
    private ValueAnimator demonstration;
    private float progress = -1f;
    private float scale = 1f;
    private float offsetX;
    private float offsetY;
    private boolean drawingStroke;

    public CursiveLetterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        configureStroke(guidePaint, Color.rgb(177, 164, 203), 9f);
        guidePaint.setPathEffect(new DashPathEffect(new float[]{2f, 13f}, 0f));
        configureStroke(inkPaint, Color.rgb(89, 53, 158), 6f);
        configureStroke(modelInkPaint, Color.WHITE, 1.5f);
        configureStroke(linePaint, Color.rgb(224, 218, 236), 1f);
        middleLinePaint.set(linePaint);
        middleLinePaint.setPathEffect(new DashPathEffect(new float[]{8f, 6f}, 0f));
        labelPaint.setColor(Color.rgb(89, 53, 158));
        labelPaint.setTextSize(16f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        setLetter('A');
        setBackgroundColor(Color.WHITE);
        setFocusable(true);
    }

    public void setLetter(char letter) {
        int resource;
        switch (letter) {
            case 'E': resource = R.drawable.dotted_letter_cursive_e; break;
            case 'I': resource = R.drawable.dotted_letter_cursive_i; break;
            case 'O': resource = R.drawable.dotted_letter_cursive_o; break;
            case 'U': resource = R.drawable.dotted_letter_cursive_u; break;
            default: resource = R.drawable.dotted_letter_cursive_a; break;
        }
        ArrayList<Path> strokes = new ArrayList<>();
        try (XmlResourceParser xml = getResources().getXml(resource)) {
            while (xml.next() != XmlPullParser.END_DOCUMENT) {
                if (xml.getEventType() == XmlPullParser.START_TAG && "path".equals(xml.getName())) {
                    String data = xml.getAttributeValue("http://schemas.android.com/apk/res/android", "pathData");
                    Path path = PathParser.createPathFromPathData(data);
                    if (path == null) throw new IllegalStateException("Missing guide path");
                    strokes.add(path);
                }
            }
            if (strokes.isEmpty()) throw new IllegalStateException("Empty letter guide");
            guide = strokes.toArray(new Path[0]);
            clearDrawing();
        } catch (java.io.IOException | org.xmlpull.v1.XmlPullParserException error) {
            throw new IllegalStateException("Unable to load cursive letter drawable", error);
        }
    }
    private static void configureStroke(Paint paint, int color, float width) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        scale = Math.max(0.01f, Math.min(w / WIDTH, h / HEIGHT));
        offsetX = (w - WIDTH * scale) / 2f;
        offsetY = (h - HEIGHT * scale) / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);
        canvas.drawLine(20, TOP_GUIDE, 300, TOP_GUIDE, linePaint);
        canvas.drawLine(20, 179, 300, 179, middleLinePaint);
        canvas.drawLine(20, 282, 300, 282, linePaint);
        for (Path stroke : guide) canvas.drawPath(stroke, guidePaint);
        for (int i = 0; i < guide.length; i++) {
            float[] start = new float[2];
            new PathMeasure(guide[i], false).getPosTan(0, start, null);
            float labelY = start[1] < TOP_GUIDE + 24f ? start[1] - 16f : start[1] + 20f;
            canvas.drawText(String.valueOf(i + 1), Math.max(12f, start[0] - 18f),
                    Math.min(HEIGHT - 12f, labelY), labelPaint);
        }
        if (progress >= 0f) {
            for (int i = 0; i < guide.length; i++) {
                float part = Math.max(0f, Math.min(1f, progress - i));
                Path visible = new Path();
                PathMeasure measure = new PathMeasure(guide[i], false);
                measure.getSegment(0, measure.getLength() * part, visible, true);
                canvas.drawPath(visible, inkPaint);
            }
        }
        canvas.drawPath(drawing, inkPaint);
        canvas.restore();
    }

    public void showTracing() {
        if (guidePaint.getAlpha() == 0) return;
        stopDemonstration();
        demonstration = ValueAnimator.ofFloat(0f, guide.length);
        demonstration.setDuration(guide.length * 1500L);
        demonstration.addUpdateListener(value -> {
            progress = (float) value.getAnimatedValue();
            invalidate();
        });
        demonstration.start();
    }

    private void stopDemonstration() {
        if (demonstration != null) {
            demonstration.cancel();
            demonstration = null;
        }
        progress = -1f;
    }

    public void setGuideOpacity(float opacity) {
        int alpha = Math.round(opacity * 255f);
        guidePaint.setAlpha(alpha);
        labelPaint.setAlpha(alpha);
        if (alpha == 0) stopDemonstration();
        invalidate();
    }

    // Match the pilot's centered, proportional white-on-black input. Draw only the user's path.
    float[] getModelPixels(int inputSize) {
        RectF bounds = new RectF();
        drawing.computeBounds(bounds, true);
        float extent = Math.max(bounds.width(), bounds.height());
        if (extent <= 0f) return null;

        int bitmapSize = inputSize * INPUT_SUPERSAMPLING;
        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
        int[] argb = new int[bitmapSize * bitmapSize];
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.BLACK);
            canvas.scale(INPUT_SUPERSAMPLING, INPUT_SUPERSAMPLING);
            float center = (inputSize - 1f) / 2f;
            float scaleToModel = 21f / extent;
            canvas.translate(center, center);
            canvas.scale(scaleToModel, scaleToModel);
            canvas.translate(-bounds.centerX(), -bounds.centerY());
            modelInkPaint.setStrokeWidth(1.5f / scaleToModel);
            canvas.drawPath(drawing, modelInkPaint);
            bitmap.getPixels(argb, 0, bitmapSize, 0, 0, bitmapSize, bitmapSize);
        } finally {
            bitmap.recycle();
        }

        float[] pixels = new float[inputSize * inputSize];
        int total = 0;
        for (int y = 0; y < inputSize; y++) {
            for (int x = 0; x < inputSize; x++) {
                int ink = 0;
                for (int row = 0; row < INPUT_SUPERSAMPLING; row++) {
                    int offset = (y * INPUT_SUPERSAMPLING + row) * bitmapSize + x * INPUT_SUPERSAMPLING;
                    for (int col = 0; col < INPUT_SUPERSAMPLING; col++) {
                        ink += Color.red(argb[offset + col]);
                    }
                }
                pixels[y * inputSize + x] = ink / (255f * INPUT_SUPERSAMPLING * INPUT_SUPERSAMPLING);
                total += ink;
            }
        }
        return total > 0 ? pixels : null;
    }

    public void clearDrawing() {
        stopDemonstration();
        drawingStroke = false;
        drawing.reset();
        points.clear();
        invalidate();
    }

    public float[] getDrawingPoints() {
        float[] result = new float[points.size()];
        for (int i = 0; i < result.length; i++) result[i] = points.get(i);
        return result;
    }

    public void restoreDrawing(float[] savedPoints) {
        clearDrawing();
        if (savedPoints == null) return;
        boolean start = true;
        for (int i = 0; i + 1 < savedPoints.length; i += 2) {
            float x = savedPoints[i], y = savedPoints[i + 1];
            points.add(x);
            points.add(y);
            if (Float.isNaN(x)) { start = true; continue; }
            if (start) { drawing.moveTo(x, y); start = false; }
            else drawing.lineTo(x, y);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;
        float x = Math.max(0f, Math.min(WIDTH, (event.getX() - offsetX) / scale));
        float y = Math.max(0f, Math.min(HEIGHT, (event.getY() - offsetY) / scale));
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                stopDemonstration();
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                drawingStroke = true;
                points.add(Float.NaN); points.add(Float.NaN);
                drawing.moveTo(x, y);
                points.add(x); points.add(y);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                if (!drawingStroke) return false;
                drawing.lineTo(x, y);
                points.add(x); points.add(y);
                if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    drawingStroke = false;
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                    performClick();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                drawingStroke = false;
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        stopDemonstration();
        super.onDetachedFromWindow();
    }
}
