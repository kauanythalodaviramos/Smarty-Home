package com.kauan.proj_lvb_dankau;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private SensorButtonView btnLight, btnTemp, btnHum;
    private FlaskButtonView btnFlask;
    private Button btnBack, btnSettings;
    private Handler handler = new Handler();
    private Runnable updateRunnable;

    private float currentLight = 0f;
    private float currentTemp = 0f;
    private float currentHum = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnLight = findViewById(R.id.btn_light);
        btnTemp = findViewById(R.id.btn_temp);
        btnHum = findViewById(R.id.btn_hum);
        btnFlask = findViewById(R.id.btn_flask);
        btnBack = findViewById(R.id.btn_back);
        btnSettings = findViewById(R.id.btn_settings);

        // Set sensor types
        btnLight.setSensorType(SensorButtonView.TYPE_LIGHT);
        btnTemp.setSensorType(SensorButtonView.TYPE_TEMP);
        btnHum.setSensorType(SensorButtonView.TYPE_HUM);

        // Button click listeners
        btnLight.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(() -> showSensorPopup("light"), 150);
        });

        btnTemp.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(() -> showSensorPopup("temp"), 150);
        });

        btnHum.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(() -> showSensorPopup("hum"), 150);
        });

        btnFlask.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(this::showAllSensorsPopup, 150);
        });

        btnBack.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }, 150);
        });

        btnSettings.setOnClickListener(v -> animatePress(v));

        // Start live data updates
        startDataUpdates();
    }

    private void startDataUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                currentLight = SensorData.getLightNormalized();
                currentTemp = SensorData.getTemperatureNormalized();
                currentHum = SensorData.getHumidityNormalized();

                btnLight.setLevel(currentLight);
                btnTemp.setLevel(currentTemp);
                btnHum.setLevel(currentHum);
                btnFlask.setLevels(currentLight, currentTemp, currentHum);

                handler.postDelayed(this, 3000);
            }
        };
        handler.post(updateRunnable);
    }

    private void showSensorPopup(String type) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sensor);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.6f);
        }

        TextView tvTitle = dialog.findViewById(R.id.tv_sensor_title);
        TextView tvValue = dialog.findViewById(R.id.tv_sensor_value);
        FrameLayout chartContainer = dialog.findViewById(R.id.chart_container);
        Button btnClose = dialog.findViewById(R.id.btn_close_sensor);

        switch (type) {
            case "light":
                tvTitle.setText("Light Level");
                tvTitle.setTextColor(Color.parseColor("#ffeaae"));
                float lightLux = SensorData.getLightLevel();
                tvValue.setText(String.format("%.0f lx", lightLux));
                tvValue.setTextColor(Color.parseColor("#ffeaae"));
                addLogLogChart(chartContainer, "#ffeaae");
                break;
            case "temp":
                tvTitle.setText("Temperature");
                tvTitle.setTextColor(Color.parseColor("#e95e3f"));
                float temp = SensorData.getTemperature();
                tvValue.setText(String.format("%.1f °C", temp));
                tvValue.setTextColor(Color.parseColor("#e95e3f"));
                addBellChart(chartContainer, "#e95e3f");
                break;
            case "hum":
                tvTitle.setText("Humidity");
                tvTitle.setTextColor(Color.parseColor("#338cca"));
                float hum = SensorData.getHumidity();
                tvValue.setText(String.format("%.0f %%", hum));
                tvValue.setTextColor(Color.parseColor("#338cca"));
                addGaugeChart(chartContainer, "#338cca", hum);
                break;
        }

        btnClose.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(dialog::dismiss, 150);
        });

        dialog.show();
    }

    private void showAllSensorsPopup() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_all_sensors);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        FrameLayout chartLight = dialog.findViewById(R.id.chart_all_light);
        FrameLayout chartTemp = dialog.findViewById(R.id.chart_all_temp);
        FrameLayout chartHum = dialog.findViewById(R.id.chart_all_hum);
        Button btnCloseAll = dialog.findViewById(R.id.btn_close_all);

        addLogLogChart(chartLight, "#ffeaae");
        addBellChart(chartTemp, "#e95e3f");
        addGaugeChart(chartHum, "#338cca", SensorData.getHumidity());

        btnCloseAll.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(() -> {
                dialog.dismiss();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }, 150);
        });

        dialog.setOnDismissListener(d ->
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));

        dialog.show();
    }

    private void addLogLogChart(FrameLayout container, String colorHex) {
        LineChart chart = new LineChart(this);
        List<Entry> entries = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            float x = (float) Math.log10(i * 500);
            float y = (float) Math.log10(SensorData.getLightLevel());
            entries.add(new Entry(x, y + (float)(Math.random() * 0.5)));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Light (log lx)");
        dataSet.setColor(Color.parseColor(colorHex));
        dataSet.setFillColor(Color.parseColor(colorHex));
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(2f);
        dataSet.setFillAlpha(80);
        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.getXAxis().setTextColor(Color.DKGRAY);
        chart.getAxisLeft().setTextColor(Color.DKGRAY);
        chart.getAxisRight().setEnabled(false);
        chart.animateX(800);
        container.addView(chart, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void addBellChart(FrameLayout container, String colorHex) {
        LineChart chart = new LineChart(this);
        List<Entry> entries = new ArrayList<>();
        float mu = 28f, sigma = 8f;
        for (int i = 0; i <= 50; i++) {
            float x = 10 + i * 0.7f;
            float y = (float) Math.exp(-0.5 * Math.pow((x - mu) / sigma, 2));
            entries.add(new Entry(x, y));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Temp curve");
        dataSet.setColor(Color.parseColor(colorHex));
        dataSet.setFillColor(Color.parseColor(colorHex));
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(2f);
        dataSet.setFillAlpha(80);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.getXAxis().setTextColor(Color.DKGRAY);
        chart.getAxisLeft().setTextColor(Color.DKGRAY);
        chart.getAxisRight().setEnabled(false);
        chart.animateX(800);
        container.addView(chart, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void addGaugeChart(FrameLayout container, String colorHex, float humValue) {
        PieChart chart = new PieChart(this);
        float normalizedHum = Math.min(humValue / 100f, 1f);
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(normalizedHum * 180f));
        entries.add(new PieEntry((1f - normalizedHum) * 180f));
        entries.add(new PieEntry(180f)); // bottom half hidden

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor(colorHex),
                Color.parseColor("#e0e0e0"),
                Color.parseColor("#ffffff")
        );
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(0f);

        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.setRotationAngle(270f);
        chart.setHoleRadius(65f);
        chart.setTransparentCircleRadius(0f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setCenterText(String.format("%.0f%%", humValue));
        chart.setCenterTextSize(16f);
        chart.setCenterTextColor(Color.parseColor(colorHex));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.animateY(800);
        container.addView(chart, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void animatePress(View view) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator sx = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.93f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.93f, 1f);
        set.playTogether(sx, sy);
        set.setDuration(200);
        set.setInterpolator(new OvershootInterpolator(3f));
        set.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updateRunnable != null)
            handler.removeCallbacks(updateRunnable);
    }
}
