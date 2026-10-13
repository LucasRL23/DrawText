package com.minhavida.drawtext;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.XmlResourceParser;
import androidx.core.graphics.PathParser;
import org.xmlpull.v1.XmlPullParser;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

/** A cursive uppercase A guide, independent of the print-letter classifier. */
public class CursiveLetterAView extends View {
    private static final float WIDTH = 320f;
    private static final float HEIGHT = 360f;
    private final Paint guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint inkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path[] guide = new Path[3];
    private final Path drawing = new Path();
    private final ArrayList<Float> points = new ArrayList<>();
    private ValueAnimator demonstration;
    private float progress = -1f;
    private float scale = 1f;
    private float offsetX;
    private float offsetY;
    private boolean drawingStroke;

    public CursiveLetterAView(Context context, AttributeSet attrs) {
        super(context, attrs);
        configureStroke(guidePaint, Color.rgb(177, 164, 203), 9f);
        guidePaint.setPathEffect(new DashPathEffect(new float[]{2f, 13f}, 0f));
        configureStroke(inkPaint, Color.rgb(89, 53, 158), 6f);
        configureStroke(linePaint, Color.rgb(224, 218, 236), 1f);
        labelPaint.setColor(Color.rgb(89, 53, 158));
        labelPaint.setTextSize(16f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        loadGuide();
        setBackgroundColor(Color.WHITE);
        setFocusable(true);
    }

    private void loadGuide() {
        try (XmlResourceParser xml = getResources().getXml(R.drawable.dotted_letter_cursive_a)) {
            int index = 0;
            while (xml.next() != XmlPullParser.END_DOCUMENT) {
                if (xml.getEventType() == XmlPullParser.START_TAG && "path".equals(xml.getName())) {
                    if (index >= guide.length) throw new IllegalStateException("Too many guide strokes");
                    String data = xml.getAttributeValue("http://schemas.android.com/apk/res/android", "pathData");
                    guide[index++] = PathParser.createPathFromPathData(data);
                }
            }
            if (index != guide.length) throw new IllegalStateException("Incomplete A guide");
        } catch (java.io.IOException | org.xmlpull.v1.XmlPullParserException error) {
            throw new IllegalStateException("Unable to load cursive A drawable", error);
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
        canvas.drawLine(20, 76, 300, 76, linePaint);
        canvas.drawLine(20, 195, 300, 195, linePaint);
        canvas.drawLine(20, 282, 300, 282, linePaint);
        for (Path stroke : guide) canvas.drawPath(stroke, guidePaint);
        canvas.drawText("1", 57, 305, labelPaint);
        canvas.drawText("2", 218, 73, labelPaint);
        canvas.drawText("3", 85, 212, labelPaint);
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
        stopDemonstration();
        demonstration = ValueAnimator.ofFloat(0f, 3f);
        demonstration.setDuration(4500);
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
