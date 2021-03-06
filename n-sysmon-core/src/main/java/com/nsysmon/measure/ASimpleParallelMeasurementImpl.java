package com.nsysmon.measure;

import com.nsysmon.config.log.NSysMonLogger;
import com.nsysmon.data.AHierarchicalData;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author arno
 */
class ASimpleParallelMeasurementImpl implements ASimpleMeasurement {
    private static final NSysMonLogger log = NSysMonLogger.get(ASimpleParallelMeasurementImpl.class);

    private final long startTimeMillis = System.currentTimeMillis();
    private final long startTimeNanos; // this number has no absolute meaning and is useful only for measuring differences
    private final String identifier;

    private final Map<String, String> parameters = new TreeMap<>();

    private final AMeasurementHierarchy hierarchy;
    private final List<AHierarchicalData> childrenOfParent;

    private boolean isFinished = false;

    ASimpleParallelMeasurementImpl(AMeasurementHierarchy hierarchy, long startTimeNanos, String identifier, List<AHierarchicalData> childrenOfParent) {
        this.hierarchy = hierarchy;
        this.startTimeNanos = startTimeNanos;
        this.identifier = identifier;
        this.childrenOfParent = childrenOfParent;
    }

    @Override public void addParameter(String identifier, String value) {
        if(parameters.put(identifier, value) != null) {
            log.warn("duplicate parameter " + identifier + " for a measurement");
        }
    }

    public void finish() {
        if(isFinished) {
            log.error (new IllegalStateException("a simple measurement can be finished only once."));
            return;
        }

        isFinished = true;
        hierarchy.finish(this);
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    List<AHierarchicalData> getChildrenOfParent() {
        return childrenOfParent;
    }
}
