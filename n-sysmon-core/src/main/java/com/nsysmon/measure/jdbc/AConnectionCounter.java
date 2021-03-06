package com.nsysmon.measure.jdbc;


import com.ajjpj.afoundation.collection.immutable.AOption;
import com.nsysmon.config.wiring.ABeanFactory;
import com.nsysmon.data.AScalarDataPoint;
import com.nsysmon.measure.scalar.AScalarMeasurer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arno
 */
@ABeanFactory
public class AConnectionCounter implements AScalarMeasurer, AIConnectionCounter {
    public static final AConnectionCounter INSTANCE = new AConnectionCounter(); //TODO make instance management configurable

    private static final String DEFAULT_POOL_IDENTIFIER = " @@##++ ";
    private final Map<String, AtomicInteger> openPerConnectionPool = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> activePerConnectionPool = new ConcurrentHashMap<>();

    public static AConnectionCounter getInstance() {
        return INSTANCE;
    }

    private AConnectionCounter() {
    }

    public void onOpenConnection(String qualifier) {
        getCounter(qualifier, openPerConnectionPool).incrementAndGet();
    }

    public void onActivateConnection(String qualifier) {
        getCounter(qualifier, activePerConnectionPool).incrementAndGet();
    }

    private AtomicInteger getCounter(String qualifier, Map<String, AtomicInteger> map) {
        if(qualifier == null) {
            qualifier = DEFAULT_POOL_IDENTIFIER;
        }
        AtomicInteger result = map.get(qualifier);
        if(result == null) {
            synchronized (map) {
                result = map.get(qualifier);
                if(result == null) {
                    result = new AtomicInteger(0);
                    map.put(qualifier, result);
                }
            }
        }
        return result;
    }

    public void onCloseConnection(String qualifier) {
        getCounter(qualifier, openPerConnectionPool).decrementAndGet();
    }

    public void onPassivateConnection(String qualifier) {
        getCounter(qualifier, activePerConnectionPool).decrementAndGet();
    }

    @Override
    public void prepareMeasurements(Map<String, Object> mementos) {
    }

    @Override
    public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) {
        for(Map.Entry<String, AtomicInteger> stringAtomicIntegerEntry : openPerConnectionPool.entrySet()) {
            final String key = stringAtomicIntegerEntry.getKey();
            final String ident = (DEFAULT_POOL_IDENTIFIER.equals(key)) ? "JDBC: Open Connections" : ("JDBC: Open Connections (" + key + ')');
            data.put(ident, new AScalarDataPoint(timestamp, ident, stringAtomicIntegerEntry.getValue().get(), 0));
        }
        for(Map.Entry<String, AtomicInteger> stringAtomicIntegerEntry : activePerConnectionPool.entrySet()) {
            final String key = stringAtomicIntegerEntry.getKey();
            final String ident = (DEFAULT_POOL_IDENTIFIER.equals(key)) ? "JDBC: Active Connections" : ("JDBC: Active Connections (" + key + ')');
            data.put(ident, new AScalarDataPoint(timestamp, ident, stringAtomicIntegerEntry.getValue().get(), 0));
        }
    }

    @Override public void shutdown() {
    }

    @Override public AOption<Long> getTimeoutInMilliSeconds() {
        return AOption.none();
    }

    @Override public String getGroupnameOfMeasurement(String measurement) {
        if (measurement != null && measurement.startsWith("JDBC")) {
            return "JDBC";
        }
        return null;
    }

    @Override public String getDescriptionOfMeasurement(String measurement) {
        if (measurement != null && measurement.startsWith("JDBC: Open Connections")) {
            return "Open database connections.";
        }
        else if (measurement != null && measurement.startsWith("JDBC: Active Connections")) {
            return "Active database connections.";
        }
        return null;
    }
}
