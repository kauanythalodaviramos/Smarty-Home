package com.kauan.proj_lvb_dankau;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class SensorButtonView extends View {

    public static final int TYPE_LIGHT = 0;
    public static final int TYPE_TEMP  = 1;
    public static final int TYPE_HUM   = 2;

    private int sensorType = TYPE_LIGHT;
    private float level = 0f;        // 0.0 – 1.0
    private float animLevel = 0f;    // animated value
    private float waveOffset = 0f;

    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint liquidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ValueAnimator levelAnimator;
    private final android.os.Handler waveHandler = new android.os.Handler();
    private final Runnable waveRunnable = () -> {
        waveOffset += 0.08f;
        if (waveOffset > (float)(2 * Math.PI)) waveOffset = 0f;
        invalidate();
        waveHandler.postDelayed(this.waveRunnable, 30);
    };

    // Colours per type
    private static final int[] LIQUID_COLORS = {
            0xFFFFEAAE, // light  – warm yellow
            0xFFE95E3F, // temp   – orange-red
            0xFF338CCA  // hum    – blue
    };

    // Emote text per type
    private static final String[] EMOTES = { "🕯", "🔥", "🧪" };

    public SensorButtonView(Context context) { super(context); init(); }
    public SensorButtonView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public SensorButtonView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }

    private void init() {
        bgPaint.setColor(0xFF4A4C51);
        bgPaint.setStyle(Paint.Style.FILL);

        strokePaint.setColor(Color.WHITE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(4f);

        shadowPaint.setColor(0x22000000);
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStrokeWidth(18f);
        shadowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(18f,
                android.graphics.BlurMaskFilter.Blur.NORMAL));

        liquidPaint.setStyle(Paint.Style.FILL);
        liquidPaint.setAntiAlias(true);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(5f);
        ringPaint.setAntiAlias(true);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        waveHandler.postDelayed(waveRunnable, 30);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setSensorType(int type) {
        this.sensorType = type;
        liquidPaint.setColor(LIQUID_COLORS[type]);
        ringPaint.setColor(LIQUID_COLORS[type]);
        invalidate();
    }

    public void setLevel(float newLevel) {
        this.level = Math.max(0f, Math.min(1f, newLevel));
        if (levelAnimator != null) levelAnimator.cancel();
        levelAnimator = ValueAnimator.ofFloat(animLevel, this.level);
        levelAnimator.setDuration(1200);
        levelAnimator.setInterpolator(new DecelerateInterpolator());
        levelAnimator.addUpdateListener(a -> {
            animLevel = (float) a.getAnimatedValue();
            invalidate();
        });
        levelAnimator.start();
        // Ring opacity update
        ringPaint.setAlpha((int)(51 + animLevel * 204)); // 20%–100%
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        float cx = w / 2f, cy = h / 2f;
        float radius = Math.min(w, h) / 2f - 24f;

        // Outer ring with opacity
        ringPaint.setAlpha((int)(51 + animLevel * 204));
        canvas.drawCircle(cx, cy, radius + 18f, ringPaint);

        // Soft shadow
        canvas.drawCircle(cx, cy, radius, shadowPaint);

        // Background circle
        canvas.drawCircle(cx, cy, radius, bgPaint);

        // Liquid fill with wave
        if (animLevel > 0.01f) {
            float fillHeight = radius * 2f * animLevel;
            float waterTop = cy + radius - fillHeight;
            Path liquidPath = new Path();
            liquidPath.moveTo(cx - radius, cy + radius);
            liquidPath.lineTo(cx - radius, waterTop);

            int segments = 30;
            float segW = (radius * 2f) / segments;
            for (int i = 0; i <= segments; i++) {
                float px = (cx - radius) + i * segW;
                float py = waterTop + (float) Math.sin(waveOffset + i * 0.4f) * 6f;
                liquidPath.lineTo(px, py);
            }
            liquidPath.lineTo(cx + radius, cy + radius);
            liquidPath.close();

            // Clip to circle
            Path circlePath = new Path();
            circlePath.addCircle(cx, cy, radius - 2f, Path.Direction.CW);
            canvas.save();
            canvas.clipPath(circlePath);
            canvas.drawPath(liquidPath, liquidPaint);
            canvas.restore();
        }

        // White stroke border
        canvas.drawCircle(cx, cy, radius, strokePaint);

        // Emote
        String emote = EMOTES[sensorType];
        float textSize = radius * 0.65f;
        textPaint.setTextSize(textSize);
        textPaint.setColor(Color.WHITE);
        // Draw emoji centered
        float textY = cy - (textPaint.ascent() + textPaint.descent()) / 2f;
        canvas.drawText(emote, cx, textY, textPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        waveHandler.removeCallbacks(waveRunnable);
        if (levelAnimator != null) levelAnimator.cancel();
    }
}
