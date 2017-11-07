package com.nsysmon.config;

import com.nsysmon.config.appinfo.AApplicationInfoProvider;
import com.nsysmon.config.presentation.APresentationMenuEntry;
import com.nsysmon.config.presentation.APresentationPageDefinition;
import com.nsysmon.datasink.ADataSink;
import com.nsysmon.measure.environment.AEnvironmentMeasurer;
import com.nsysmon.measure.http.AHttpRequestAnalyzer;
import com.nsysmon.measure.http.ASimpleHttpRequestAnalyzer;
import com.nsysmon.measure.scalar.AScalarMeasurer;
import com.nsysmon.util.timer.ASystemNanoTimer;
import com.nsysmon.util.timer.ATimer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author arno
 */
public class NSysMonConfigBuilder {
    private AApplicationInfoProvider appInfo;

    private int averagingDelayForScalarsMillis = 1_000;

    private int maxNestedMeasurements = 100;
    private int maxNumMeasurementsPerHierarchy = 100_000;
    private int maxNumMeasurementsPerTimedScalar = 500;
    private int maxNumMeasurementsForCockpit = 50;

    private long measurementTimeoutNanos = 20_000_000;
    private int maxNumMeasurementTimeouts = 3;
    private int durationOfOneTimedScalar = 300;

    private long dataSinkTimeoutNanos = 100_000;
    private int maxNumDataSinkTimeouts = 3;

    private boolean collectSqlParameters = false;
    private boolean collectTooltips = false;
    private boolean correlationFlowDisabled = false;

    private String pathDatafiles = "/tmp";

    private ATimer timer = new ASystemNanoTimer();
    private AHttpRequestAnalyzer httpRequestAnalyzer = new ASimpleHttpRequestAnalyzer();

    private final List<AEnvironmentMeasurer> environmentMeasurers = new ArrayList<>();
    private final List<AScalarMeasurer> scalarMeasurers = new ArrayList<>();
    private final List<AScalarMeasurer> scalarTimedMeasurers = new ArrayList<>();
    private final List<ADataSink> dataSinks = new ArrayList<>();

    private final List<APresentationMenuEntry> presentationMenuEntries = new ArrayList<>();
    private String defaultPage;
    private Map<String, String> additionalConfigurationParameters;
    private Map<String, Object> timedScalarMonitoringParameters = new HashMap<>();

    public NSysMonConfigBuilder(AApplicationInfoProvider appInfo) {
        this.appInfo = appInfo;
    }

    @SuppressWarnings("unused")
    public NSysMonConfigBuilder setApplicationInfo(AApplicationInfoProvider appInfo) {
        this.appInfo = appInfo;
        return this;
    }

    public NSysMonConfigBuilder setTimer(ATimer timer) {
        this.timer = timer;
        return this;
    }

    public NSysMonConfigBuilder setAveragingDelayForScalarsMillis(int averagingDelayForScalarsMillis) {
        this.averagingDelayForScalarsMillis = averagingDelayForScalarsMillis;
        return this;
    }

    public NSysMonConfigBuilder setMaxNestedMeasurements (int maxNestedMeasurements) {
        this.maxNestedMeasurements = maxNestedMeasurements;
        return this;
    }

    public NSysMonConfigBuilder setMaxNumMeasurementsPerHierarchy (int maxNum) {
        this.maxNumMeasurementsPerHierarchy = maxNum;
        return this;
    }

    public NSysMonConfigBuilder setMaxNumMeasurementsPerTimedScalar(int maxNum) {
        this.maxNumMeasurementsPerTimedScalar = maxNum;
        return this;
    }
    public NSysMonConfigBuilder setMaxNumMeasurementsForCockpit(int maxNum) {
        this.maxNumMeasurementsForCockpit = maxNum;
        return this;
    }

    public NSysMonConfigBuilder setDurationOfOneTimedScalar(int number) {
        this.durationOfOneTimedScalar = number;
        return this;
    }

    public NSysMonConfigBuilder setMeasurementTimeoutNanos(long measurementTimeoutNanos) {
        this.measurementTimeoutNanos = measurementTimeoutNanos;
        return this;
    }

    public boolean isCollectTooltips() {
        return collectTooltips;
    }

    public void setCollectTooltips(boolean collectTooltips) {
        this.collectTooltips = collectTooltips;
    }

    public void setCollectSqlParameters(boolean collectSqlParameters) {
        this.collectSqlParameters = collectSqlParameters;
    }

    void setCorrelationFlowDisabled(boolean correlationFlowDisabled) {
        this.correlationFlowDisabled = correlationFlowDisabled;
    }

    public boolean isCollectSqlParameters() {
        return collectSqlParameters;
    }

    public NSysMonConfigBuilder setHttpRequestAnalyzer(AHttpRequestAnalyzer httpRequestAnalyzer) {
        this.httpRequestAnalyzer = httpRequestAnalyzer;
        return this;
    }

    public NSysMonConfigBuilder setMaxNumMeasurementTimeouts(int maxNumMeasurementTimeouts) {
        this.maxNumMeasurementTimeouts = maxNumMeasurementTimeouts;
        return this;
    }

    public NSysMonConfigBuilder setDataSinkTimeoutNanos(long dataSinkTimeoutNanos) {
        this.dataSinkTimeoutNanos = dataSinkTimeoutNanos;
        return this;
    }

    public NSysMonConfigBuilder setMaxNumDataSinkTimeouts(int maxNumDataSinkTimeouts) {
        this.maxNumDataSinkTimeouts = maxNumDataSinkTimeouts;
        return this;
    }

    public NSysMonConfigBuilder addEnvironmentMeasurer(AEnvironmentMeasurer environmentMeasurer) {
        this.environmentMeasurers.add(environmentMeasurer);
        return this;
    }

    public NSysMonConfigBuilder addScalarMeasurer(AScalarMeasurer scalarMeasurer) {
        this.scalarMeasurers.add(scalarMeasurer);
        return this;
    }

    public NSysMonConfigBuilder addScalarTimedMeasurer(AScalarMeasurer scalarTimedMeasurer) {
        this.scalarTimedMeasurers.add(scalarTimedMeasurer);
        return this;
    }

    public NSysMonConfigBuilder addDataSink(ADataSink dataSink) {
        this.dataSinks.add(dataSink);
        return this;
    }

    public NSysMonConfigBuilder setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
        return this;
    }

    public NSysMonConfigBuilder addPresentationMenuEntry(String label, List<APresentationPageDefinition> entries) {
        presentationMenuEntries.add(new APresentationMenuEntry(label, entries));
        return this;
    }

    public String getPathDatafiles() {
        return pathDatafiles;
    }

    public void setPathDatafiles(String pathDatafiles) {
        this.pathDatafiles = pathDatafiles;
    }

    @SuppressWarnings("unused")
    public NSysMonConfigBuilder addPresentationMenuEntry(String label, APresentationPageDefinition... entries) {
        return addPresentationMenuEntry(label, Arrays.asList(entries));
    }

    public NSysMonConfig build() {
        return new NSysMonConfig(
                appInfo,
                averagingDelayForScalarsMillis, durationOfOneTimedScalar,
                maxNestedMeasurements, maxNumMeasurementsPerHierarchy, maxNumMeasurementsPerTimedScalar,
                maxNumMeasurementsForCockpit, measurementTimeoutNanos, maxNumMeasurementTimeouts,
                dataSinkTimeoutNanos, maxNumDataSinkTimeouts,
                timer, httpRequestAnalyzer,
                environmentMeasurers, scalarMeasurers, scalarTimedMeasurers, dataSinks,
                defaultPage, presentationMenuEntries, additionalConfigurationParameters,
                collectSqlParameters, collectTooltips, pathDatafiles, timedScalarMonitoringParameters, correlationFlowDisabled
                );
    }

    public void addTimedScalarMonitoringParameter(String key, Long value) {
        this.timedScalarMonitoringParameters.put(key, value);
    }

    public void addTimedScalarMonitoringParameter(String key, Boolean value) {
        this.timedScalarMonitoringParameters.put(key, value);
    }

    public void setAdditionalConfigurationParameters(Map<String, String> additionalConfigurationParameters) {
        this.additionalConfigurationParameters = additionalConfigurationParameters;
    }

}
