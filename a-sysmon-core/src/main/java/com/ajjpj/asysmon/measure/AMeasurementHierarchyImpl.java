package com.ajjpj.asysmon.measure;

import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.processing.ADataSink;
import com.ajjpj.asysmon.timer.ATimer;
import com.ajjpj.asysmon.util.ArrayStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This class collects a tree of hierarchical measurements, i.e. it lives in a single thread.
 *
 * @author arno
 */
public class AMeasurementHierarchyImpl implements com.ajjpj.asysmon.measure.AMeasurementHierarchy {
    private final ATimer timer;
    private final ADataSink dataSink;

    private final ArrayStack<ASimpleMeasurement> unfinished = new ArrayStack<com.ajjpj.asysmon.measure.ASimpleMeasurement>();
    private final ArrayStack<List<AHierarchicalData>> childrenStack = new ArrayStack<List<AHierarchicalData>>();

    private boolean isFinished = false;

    public AMeasurementHierarchyImpl(ATimer timer, ADataSink dataSink) {
        this.timer = timer;
        this.dataSink = dataSink;
    }

    private void checkNotFinished() {
        if(isFinished) {
            throw new IllegalStateException("measurements must not be reused - this measurement is already closed");
        }
    }

    @Override public ASimpleMeasurement start(String identifier, boolean disjoint) {
        checkNotFinished();

        final ASimpleMeasurement result = new ASimpleMeasurement(this, disjoint, timer.getCurrentNanos(), identifier);
        unfinished.push(result);
        childrenStack.push(new ArrayList<AHierarchicalData>());
        return result;
    }

    @Override public void finish(ASimpleMeasurement measurement) {
        checkNotFinished();

        if (unfinished.peek() != measurement) {
            //TODO this is a bug in using code - how to deal with it?!
            throw new IllegalStateException("measurements must be strictly nested");
        }

        final long finishedTimestamp = timer.getCurrentNanos();

        unfinished.pop();
        final List<AHierarchicalData> children = childrenStack.pop();
        final AHierarchicalData newData = new AHierarchicalData(measurement.isDisjoint(), measurement.getStartTimeMillis(), finishedTimestamp - measurement.getStartTimeNanos(), measurement.getIdentifier(), measurement.getParameters(), children);

        if(unfinished.isEmpty()) {
            isFinished = true;
            dataSink.onFinishedHierarchicalData(newData);
        }
        else {
            childrenStack.peek().add(newData);
        }
    }

    @Override
    public ACollectingMeasurement startCollectingMeasurement(String identifier, boolean disjoint) {
        checkNotFinished();
        if(unfinished.isEmpty()) {
            throw new IllegalStateException("currently no support for top-level collecting measurements"); //TODO what is a good way to get around this?
        }

        return new ACollectingMeasurement(timer, this, disjoint, identifier, childrenStack.peek());
    }

    @Override public void finish(ACollectingMeasurement m) {
        checkNotFinished();

        final List<AHierarchicalData> children = new ArrayList<AHierarchicalData>();
        for(String detailIdentifier: m.getDetails().keySet()) {
            final ACollectingMeasurement.Detail detail = m.getDetails().get(detailIdentifier);
            //TODO how to store m.getNum()?
            children.add(new AHierarchicalData(true, m.getStartTimeMillis(), detail.getTotalNanos(), detailIdentifier, Collections.<String, String>emptyMap(), Collections.<AHierarchicalData>emptyList()));
        }

        final AHierarchicalData newData = new AHierarchicalData(m.isDisjoint(), m.getStartTimeMillis(), m.getTotalDurationNanos(), m.getIdentifier(), m.getParameters(), children);
        m.getChildrenOfParent().add(newData);
    }
}

//TODO limit stack depth - if deeper than limit, discard and print error message --> prevent memory leak!
//TODO mechanism for applications to say 'we *should* be finished now'