package com.ajjpj.asysmon;

import com.ajjpj.asysmon.config.ADefaultConfigFactory;
import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.*;
import com.ajjpj.asysmon.measure.environment.AEnvironmentData;
import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.ajjpj.asysmon.util.AList;
import com.ajjpj.asysmon.util.AShutdownable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * This class is the point of contact for an application to ASysMon. There are basically two ways to use it:
 *
 * <ul>
 *     <li> Use the static get() method to access it as a singleton. That is simple and convenient, and it is
 *          sufficient for many applications. If it is used that way, all configuration must be done through
 *          the static methods of ADefaultSysMonConfig. </li>
 *     <li> Create and manage your own instance (or instances) by calling the constructor, passing in your
 *          configuration. This is for maximum flexibility, but you lose some convenience. </li>
 * </ul>
 *
 * @author arno
 */
public class ASysMon implements AShutdownable {
    private final ASysMonConfig config;
    private volatile AList<ADataSink> handlers = AList.nil();
    private volatile AList<AScalarMeasurer> scalarMeasurers = AList.nil();

    private final ThreadLocal<AMeasurementHierarchy> hierarchyPerThread = new ThreadLocal<AMeasurementHierarchy>();

    public static ASysMon get() {
        // this class has the sole purpose of providing really lazy init of the singleton instance
        return ASysMonInstanceHolder.INSTANCE;
    }

    public ASysMon(ASysMonConfig config) {
        this.config = config;

        for(AScalarMeasurer m: config.initialScalarMeasurers) {
            scalarMeasurers = scalarMeasurers.cons(m);
        }

        for(ADataSink h: config.initialDataSinks) {
            this.handlers = this.handlers.cons(h);
        }
    }

    public ASysMonConfig getConfig() {
        return config;
    }

    synchronized void addScalarMeasurer(AScalarMeasurer m) {
        scalarMeasurers = scalarMeasurers.cons(m);
    }

    synchronized void addDataSink(ADataSink handler) {
        handlers = handlers.cons(handler);
    }

    private ADataSink getCompositeDataSink() {
        return new ADataSink() {
            @Override public void onStartedHierarchicalMeasurement(String identifier) {
                for(ADataSink handler: handlers) {
                    handler.onStartedHierarchicalMeasurement(identifier);
                }
            }

            @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
                hierarchyPerThread.remove();

                for(ADataSink handler: handlers) {
                    handler.onFinishedHierarchicalMeasurement(data);
                }
            }

            @Override public void shutdown() {
            }
        };
    }

    private AMeasurementHierarchy getMeasurementHierarchy() {
        final AMeasurementHierarchy candidate = hierarchyPerThread.get();
        if(candidate != null) {
            return candidate;
        }

        final AMeasurementHierarchy result = new AMeasurementHierarchyImpl(config, getCompositeDataSink());
        hierarchyPerThread.set(result);
        return result;
    }

    public <E extends Exception> void measure(String identifier, AMeasureCallbackVoid<E> callback) throws E {
        final ASimpleMeasurement m = start(identifier);
        try {
            callback.call(m);
        } finally {
            m.finish();
        }
    }

    public <R, E extends Exception> R measure(String identifier, AMeasureCallback<R,E> callback) throws E {
        final ASimpleMeasurement m = start(identifier);
        try {
            return callback.call(m);
        } finally {
            m.finish();
        }
    }

    public ASimpleMeasurement start(String identifier) {
        return start(identifier, true);
    }
    public ASimpleMeasurement start(String identifier, boolean serial) {
        return getMeasurementHierarchy().start(identifier, serial);
    }

    /**
     * This is for the rare case that measurement data was collected by other means and should be 'injected'
     *  into A-SysMon. If you do not understand this, this method is probably not for you.
     */
    public void injectSyntheticMeasurement(AHierarchicalDataRoot d) {
        getCompositeDataSink().onStartedHierarchicalMeasurement(d.getRootNode().getIdentifier());
        getCompositeDataSink().onFinishedHierarchicalMeasurement(d);
    }

    public ACollectingMeasurement startCollectingMeasurement(String identifier) {
        return startCollectingMeasurement(identifier, true);
    }
    public ACollectingMeasurement startCollectingMeasurement(String identifier, boolean serial) {
        return getMeasurementHierarchy().startCollectingMeasurement(identifier, serial);
    }

    public Map<String, AScalarDataPoint> getScalarMeasurements() { //TODO rename to distinguish from 'add' etc.
        final Map<String, AScalarDataPoint> result = new TreeMap<String, AScalarDataPoint>();
        if(config.isGloballyDisabled()) {
            return result;
        }
        final long now = System.currentTimeMillis();
        for(AScalarMeasurer measurer: scalarMeasurers) {
            measurer.contributeMeasurements(result, now);
        }
        //TODO protect against exceptions (per measurer)
        //TODO limit duration per measurer
        return result;
    }

    public Map<AList<String>, AEnvironmentData> getEnvironmentMeasurements() throws IOException {
        final Map<AList<String>, AEnvironmentData> result = new HashMap<AList<String>, AEnvironmentData>();
        if(config.isGloballyDisabled()) {
            return result;
        }

        for(AEnvironmentMeasurer m: config.environmentMeasurers) {
            m.contributeMeasurements(result);
            //TODO protect against exceptions (per measurer)
            //TODO limit duration per measurer
        }
        return result;
    }

    @Override public void shutdown() {
        //TODO log

        for(ADataSink handler: handlers) {
            try {
                handler.shutdown();
            } catch (Exception e) {
                e.printStackTrace(); //TODO log
            }
        }
        for(AScalarMeasurer m: scalarMeasurers) {
            try {
                m.shutdown();
            } catch (Exception e) {
                e.printStackTrace(); //TODO log
            }
        }

        //TODO log
    }

    /**
     * this class has the sole purpose of providing really lazy init of the singleton instance
     */
    private static class ASysMonInstanceHolder {
        public static final ASysMon INSTANCE = new ASysMon (new ADefaultConfigFactory().getConfig());
    }
}

