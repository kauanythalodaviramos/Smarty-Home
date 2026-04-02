package com.kauan.proj_lvb_dankau;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class FlaskButtonView extends View {

    private float lightLevel = 0.5f;
    private float tempLevel  = 0.5f;
    private float humLevel   = 0.5f;
    private float waveOffset = 0f;

    private final Paint bgPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lightPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tempPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint humPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final android.os.Handler waveHandler = new android.os.Handler();
    private final Runnable waveRunnable = () -> {
        waveOffset += 0.06f;
        if (waveOffset > (float)(2 * Math.PI)) waveOffset = 0f;
        invalidate();
        waveHandler.postDelayed(this.waveRunnable, 40);
    };

    public FlaskButtonView(Context ctx) { super(ctx); init(); }
    public FlaskButtonView(Context ctx, android.util.AttributeSet a) { super(ctx, a); init(); }
    public FlaskButtonView(Context ctx, android.util.AttributeSet a, int d) { super(ctx, a, d); init(); }

    private void init() {
        bgPaint.setColor(0xFF4A4C51);
        bgPaint.setStyle(Paint.Style.FILL);

        strokePaint.setColor(Color.WHITE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(4f);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);

        shadowPaint.setColor(0x22000000);
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStrokeWidth(14f);
        shadowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(14f,
                android.graphics.BlurMaskFilter.Blur.NORMAL));

        lightPaint.setColor(0x99FFEAAE);
        lightPaint.setStyle(Paint.Style.FILL);

        tempPaint.setColor(0x99E95E3F);
        tempPaint.setStyle(Paint.Style.FILL);

        humPaint.setColor(0x99338CCA);
        humPaint.setStyle(Paint.Style.FILL);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
        waveHandler.postDelayed(waveRunnable, 40);
    }

    public void setLevels(float light, float temp, float hum) {
        this.lightLevel = light;
        this.tempLevel  = temp;
        this.humLevel   = hum;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        float cx = w / 2f;

        // Flask shape path
        float neckTop   = h * 0.08f;
        float neckBot   = h * 0.38f;
        float neckW     = w * 0.22f;
        float bodyBot   = h * 0.88f;
        float bodyW     = w * 0.78f;
        float shoulderY = h * 0.48f;

        Path flask = new Path();
        flask.moveTo(cx - neckW, neckTop);
        flask.lineTo(cx - neckW, neckBot);
        flask.cubicTo(cx - neckW, shoulderY, cx - bodyW / 2f, shoulderY, cx - bodyW / 2f, bodyBot - w * 0.12f);
        // bottom arc
        flask.cubicTo(cx - bodyW / 2f, bodyBot, cx + bodyW / 2f, bodyBot, cx + bodyW / 2f, bodyBot - w * 0.12f);
        flask.cubicTo(cx + bodyW / 2f, shoulderY, cx + neckW, shoulderY, cx + neckW, neckBot);
        flask.lineTo(cx + neckW, neckTop);
        flask.close();

        // Shadow
        canvas.drawPath(flask, shadowPaint);

        // Background fill
        canvas.save();
        canvas.clipPath(flask);
        canvas.drawRect(0, 0, w, h, bgPaint);

        // Mixed liquid fill inside flask body
        float bodyTop = shoulderY;
        float bodyHeight = bodyBot - bodyTop;

        // Layer 1: light (yellow) — bottom
        float lH = bodyHeight * lightLevel * 0.4f;
        canvas.drawRect(cx - bodyW / 2f, bodyBot - lH, cx + bodyW / 2f, bodyBot, lightPaint);

        // Layer 2: humidity (blue) — middle
        float hH = bodyHeight * humLevel * 0.35f;
        canvas.drawRect(cx - bodyW / 2f, bodyBot - lH - hH, cx + bodyW / 2f, bodyBot - lH, humPaint);

        // Layer 3: temp (red) — top
        float tH = bodyHeight * tempLevel * 0.3f;
        canvas.drawRect(cx - bodyW / 2f, bodyBot - lH - hH - tH, cx + bodyW / 2f, bodyBot - lH - hH, tempPaint);

        canvas.restore();

        // Stroke outline
        canvas.drawPath(flask, strokePaint);

        // Neck top line
        Paint neckLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        neckLine.setColor(Color.WHITE);
        neckLine.setStyle(Paint.Style.STROKE);
        neckLine.setStrokeWidth(4f);
        neckLine.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(cx - neckW - 4f, neckTop + 2f, cx + neckW + 4f, neckTop + 2f, neckLine);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        waveHandler.removeCallbacks(waveRunnable);
    }
}
