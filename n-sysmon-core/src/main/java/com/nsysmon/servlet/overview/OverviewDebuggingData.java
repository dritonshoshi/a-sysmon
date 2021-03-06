package com.nsysmon.servlet.overview;

import com.ajjpj.afoundation.io.AJsonSerHelperForNSysmon;
import com.nsysmon.NSysMon;
import com.nsysmon.NSysMonApi;
import com.nsysmon.config.log.NSysMonLogger;
import com.nsysmon.config.presentation.APresentationMenuEntry;
import com.nsysmon.config.presentation.APresentationPageDefinition;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OverviewDebuggingData implements APresentationPageDefinition {
    private volatile NSysMonApi sysMon;
    private static final NSysMonLogger LOG = NSysMonLogger.get(OverviewDebuggingData.class);


    @Override
    public String getId() {
        return "overviewDebuggingData";
    }

    @Override
    public String getShortLabel() {
        return "Debugging";
    }

    @Override
    public String getFullLabel() {
        return "Debugging Data";
    }

    @Override
    public String getHtmlFileName() {
        return "overviewdebugging.html";
    }

    @Override
    public String getControllerName() {
        return "CtrlOverviewDebugging";
    }

    @Override
    public boolean handleRestCall(String service, List<String> params, AJsonSerHelperForNSysmon json) throws Exception {
        if ("getData".equals(service)) {
            serveData(json);
            return true;
        } else if ("startOverrideCollectTooltips".equals(service)) {
            NSysMon.get().getConfig().startOverrideCollectTooltips();
            serveData(json);
            return true;
        } else if ("stopOverrideCollectTooltips".equals(service)) {
            NSysMon.get().getConfig().stopOverrideCollectTooltips();
            serveData(json);
            return true;
        } else if ("startOverrideSqlParameters".equals(service)) {
            NSysMon.get().getConfig().startOverrideSqlParameters();
            serveData(json);
            return true;
        } else if ("stopOverrideSqlParameters".equals(service)) {
            NSysMon.get().getConfig().stopOverrideSqlParameters();
            serveData(json);
            return true;
        }
        return false;
    }

    @Override
    public void init(NSysMonApi sysMon) {
        this.sysMon = sysMon;

    }

    //Test at http://localhost:8181/nsysmon/_$_nsysmon_$_/rest/overviewDebuggingData/getData
    private void serveData(final AJsonSerHelperForNSysmon json) throws IOException {
        json.startObject();

        addPageDefinitions(json);
        addConfiguration(json);

        json.endObject();
    }

    private void addConfiguration(final AJsonSerHelperForNSysmon json) throws IOException {
        json.writeKey("configurationParameters");
        json.startArray();

        addConfigEntry("averagingDelayForScalarsMillis", sysMon.getConfig().averagingDelayForScalarsMillis, json);
        addConfigEntry("dataSinkTimeoutNanos", sysMon.getConfig().dataSinkTimeoutNanos, json);
        addConfigEntry("durationOfOneTimedScalar", sysMon.getConfig().durationOfOneTimedScalar, json);
        addConfigEntry("maxNestedMeasurements", sysMon.getConfig().maxNestedMeasurements, json);
        addConfigEntry("maxNumDataSinkTimeouts", sysMon.getConfig().maxNumDataSinkTimeouts, json);
        addConfigEntry("maxNumMeasurementsPerHierarchy", sysMon.getConfig().maxNumMeasurementsPerHierarchy, json);
        addConfigEntry("maxNumMeasurementsPerTimedScalar", sysMon.getConfig().maxNumMeasurementsPerTimedScalar, json);
        addConfigEntry("maxNumMeasurementTimeouts", sysMon.getConfig().maxNumMeasurementTimeouts, json);
        addConfigEntry("measurementTimeoutNanos", sysMon.getConfig().measurementTimeoutNanos, json);

        addConfigEntry("collectSqlParameters (Configuration)", sysMon.getConfig().configuredCollectSqlParameters(), json);
        addConfigEntry("collectTooltips (Configuration)", sysMon.getConfig().configuredCollectTooltips(), json);
        addConfigEntry("current collectSqlParameters", sysMon.getConfig().collectSqlParameters(), json);
        addConfigEntry("current collectTooltips", sysMon.getConfig().collectTooltips(), json);

        json.endArray();
    }

    private void addConfigEntry(String key, int value, AJsonSerHelperForNSysmon json) throws IOException {
        json.startObject();
        json.writeKey("key");
        json.writeStringLiteral(key);
        json.writeKey("value");
        json.writeNumberLiteral(value, 0);
        json.endObject();
    }

    private void addConfigEntry(String key, boolean value, AJsonSerHelperForNSysmon json) throws IOException {
        json.startObject();
        json.writeKey("key");
        json.writeStringLiteral(key);
        json.writeKey("value");
        json.writeBooleanLiteral(value);
        json.endObject();
    }

    private void addConfigEntry(String key, long value, AJsonSerHelperForNSysmon json) throws IOException {
        json.startObject();
        json.writeKey("key");
        json.writeStringLiteral(key);
        json.writeKey("value");
        json.writeNumberLiteral(value, 0);
        json.endObject();
    }

    private void addPageDefinitions(final AJsonSerHelperForNSysmon json) throws IOException {
        final Map<String, APresentationPageDefinition> pageDefs = new ConcurrentHashMap<>();
        for (APresentationMenuEntry menuEntry : sysMon.getConfig().presentationMenuEntries) {
            for (APresentationPageDefinition pageDef : menuEntry.pageDefinitions) {
                pageDefs.put(pageDef.getId(), pageDef);
            }
        }

        json.writeKey("pageDefinitions");
        json.startArray();

        for (APresentationPageDefinition value : pageDefs.values()) {
            //LOG.info("processing" + stringAPresentationPageDefinitionEntry.getKey());
            json.startObject();

            json.writeKey("id");
            json.writeStringLiteral(value.getId());

            json.writeKey("fullLabel");
            json.writeStringLiteral(value.getFullLabel());

            json.endObject();
        }
        json.endArray();
    }

}
