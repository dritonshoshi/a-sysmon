package com.nsysmon.measure.scalar;

import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.function.AFunction1;
import com.ajjpj.afoundation.io.AFile;
import com.nsysmon.NSysMon;
import com.nsysmon.data.AScalarDataPoint;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author arno
 */
public class ACpuUtilizationMeasurer implements AScalarMeasurer {
    public static final AFile PROC_STAT_FILE = new AFile("/proc/stat", Charset.defaultCharset());
    public static final AFile PROC_STAT_FILE_MACOS_ = new AFile("sysctl -a hw", Charset.defaultCharset());

    public static final AFile PROC_CPUINFO_FILE = new AFile("/proc/cpuinfo", Charset.defaultCharset());

    public static final String KEY_PREFIX = "cpu:";
    public static final String KEY_MEMENTO = KEY_PREFIX;
    public static final String KEY_AVAILABLE = KEY_PREFIX + "available";
    public static final String KEY_ALL_USED = KEY_PREFIX + "all-used";
    public static final String KEY_PREFIX_MHZ = KEY_PREFIX + "freq-mhz:";
    public static final String KEY_SELF_KERNEL = KEY_PREFIX + "self-kernel";

    @Override public void prepareMeasurements(Map<String, Object> mementos) throws IOException {
        //this measurement isn't working on windows
        if (NSysMon.isWindows()){
           return;
        }
        if (NSysMon.isMacOS()){
            return;
        }
        mementos.put(KEY_MEMENTO, createSnapshot());
    }

    private void fillForWindows(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) operatingSystemMXBean;
            long value = bean.getAvailableProcessors() * 1000;
            data.put(KEY_AVAILABLE, new AScalarDataPoint(timestamp, KEY_AVAILABLE, Math.max(0, value), 1));
            value = (long) (bean.getSystemCpuLoad() * 1000);
            data.put(KEY_SELF_KERNEL, new AScalarDataPoint(timestamp, KEY_SELF_KERNEL, Math.max(0, value), 1));
        }
    }

    @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) throws IOException {
        //this measurement isn't working on windows
        if (NSysMon.isWindows()){
            fillForWindows(data, timestamp, mementos);
            return;
        }
        if (NSysMon.isMacOS()){
            fillForWindows(data, timestamp, mementos);
            return;
        }
        final Map<String, Snapshot> allCurrent = createSnapshot();
        @SuppressWarnings("unchecked")
        final Map<String, Snapshot> allPrev = (Map<String, Snapshot>) mementos.get(KEY_MEMENTO);

        final int numCpus = allCurrent.size() - 1;
        final Snapshot current = allCurrent.get("cpu");
        final Snapshot prev = allPrev.get("cpu");

        final long diffTime = current.timestamp - prev.timestamp;
        if(diffTime <= 0) {
            return;
        }

        final long idleJiffies   = current.idle   - prev.idle;
        final long stolenJiffies = current.stolen - prev.stolen;

        // 'baseline' - 100% for a single CPU, <# cpus>*100% for 'total'
        final long fullJiffies = diffTime * numCpus / 10;

        // reduce the theoretical 'full' capacity by 'stolen' cycles
        final long availJiffies = fullJiffies - stolenJiffies;

        final long usedJiffies = availJiffies - idleJiffies;

        final long usedPerMill = usedJiffies * 10;

        data.put(KEY_AVAILABLE, new AScalarDataPoint(timestamp, KEY_AVAILABLE, availJiffies / (diffTime / 10) * 1000, 1));
        data.put(KEY_ALL_USED, new AScalarDataPoint(timestamp, KEY_ALL_USED, usedPerMill, 1));

        contributeFreq(data, timestamp);
    }

    private void contributeFreq(Map<String, AScalarDataPoint> data, long timestamp) throws IOException {
        final Map<String, AtomicInteger> counter = new HashMap<>();

        for(String line: PROC_CPUINFO_FILE.lines()) {
            if(! line.contains("MHz")) {
                continue;
            }
            final String mhz = line.substring(line.indexOf(':') + 1).trim();
            if(! counter.containsKey(mhz)) {
                counter.put(mhz, new AtomicInteger());
            }
            counter.get(mhz).incrementAndGet();
        }

        for(String mhz: counter.keySet()) {
            final String key = KEY_PREFIX_MHZ + mhz;
            data.put(key, new AScalarDataPoint(timestamp, key, counter.get(mhz).intValue(), 0));
        }
    }

    private Map<String, Snapshot> createSnapshot() throws IOException {
        return PROC_STAT_FILE.iterate((AFunction1<Iterator<String>, Map<String, Snapshot>, RuntimeException>) iter -> {
            final Map<String, Snapshot> result = new HashMap<>();

            while(iter.hasNext()) {
                final String line = iter.next();
                final String[] split = line.split("\\s+");

                if(split[0].startsWith("cpu")) {
                    final long idle = Long.valueOf(split[4]);
                    final long stolen = split.length >= 8 ? Long.valueOf(split[8]) : 0;
                    result.put(split[0], new Snapshot(idle, stolen));
                }
            }

            return result;
        });
    }

    @Override public void shutdown() throws Exception {
    }

    static class Snapshot {
        public final long timestamp = System.currentTimeMillis();
        public final long idle;
        public final long stolen;

        Snapshot(long idle, long stolen) {
            this.idle = idle;
            this.stolen = stolen;
        }
    }

    @Override public AOption<Long> getTimeoutInMilliSeconds() {
        return AOption.none();
    }

    @Override
    public List<String> getConfigurationParameters() {
        return Arrays.asList(KEY_SELF_KERNEL, KEY_ALL_USED);
    }

}
