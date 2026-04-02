package com.kauan.proj_lvb_dankau;

import java.util.Random;

public class SensorData {

    private static final Random random = new Random();

    // Light level in lux: 10 to 10000
    public static float getLightLevel() {
        return 10 + random.nextFloat() * 9990;
    }

    // Temperature in Celsius: 10 to 45
    public static float getTemperature() {
        return 10 + random.nextFloat() * 35;
    }

    // Humidity in percent: 10 to 99
    public static float getHumidity() {
        return 10 + random.nextFloat() * 89;
    }

    // Returns 0.0 to 1.0 normalized light level
    public static float getLightNormalized() {
        return Math.min(getLightLevel() / 10000f, 1f);
    }

    // Returns 0.0 to 1.0 normalized temperature
    public static float getTemperatureNormalized() {
        return Math.min((getTemperature() - 10f) / 35f, 1f);
    }

    // Returns 0.0 to 1.0 normalized humidity
    public static float getHumidityNormalized() {
        return getHumidity() / 100f;
    }
}
