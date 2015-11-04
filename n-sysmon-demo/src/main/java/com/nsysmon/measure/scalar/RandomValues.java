package com.nsysmon.measure.scalar;

import com.nsysmon.data.AScalarDataPoint;

import java.util.Map;
import java.util.Random;

public class RandomValues implements AScalarMeasurer {
    public static final String MEASUREMENT_NAME = "RandomNumber";
    private final Random rnd = new Random();

    @Override
    public void prepareMeasurements(Map<String, Object> mementos) throws Exception {
    }

    @Override
    public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) throws Exception {
        long value = rnd.nextInt(100);
        data.put(MEASUREMENT_NAME, new AScalarDataPoint(timestamp, MEASUREMENT_NAME, value, 0));
    }

    @Override
    public void shutdown() throws Exception {

    }
}
