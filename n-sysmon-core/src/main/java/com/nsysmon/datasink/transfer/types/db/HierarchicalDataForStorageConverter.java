package com.nsysmon.datasink.transfer.types.db;

import com.nsysmon.data.AHierarchicalData;
import com.nsysmon.data.AHierarchicalDataRoot;

import java.util.List;

/**
 * Created by torsten on 11.12.2016.
 */
public class HierarchicalDataForStorageConverter {


    public static HierarchicalDataRootForStorage fromRoot(AHierarchicalDataRoot data) {
        return new HierarchicalDataRootForStorage(data.getJoinedFlows(), data.getStartedFlows(), data.isKilled());
    }

    public static HierarchicalDataForStorage fromChild(AHierarchicalData data, long level) {
        return new HierarchicalDataForStorage(data.getIdentifier(), data.getStartTimeMillis(), data.getDurationNanos(), data.getParameters(), !data.getChildren().isEmpty(), level, Long.toString(level), null, null);
    }

    public static void fromChilds(List<AHierarchicalData> dataEntries, List<HierarchicalDataForStorage> rc, long level, String parentIdentifier) {
        int cnt = 0;
        String localIdentifier;
        for (AHierarchicalData dataEntry : dataEntries) {
            localIdentifier = parentIdentifier + "." + cnt++;
            //TODO don't use , data.getDurationNanos() remove the duration of the childs and add it as <self>, too.
            HierarchicalDataForStorage result = new HierarchicalDataForStorage(dataEntry.getIdentifier(), dataEntry.getStartTimeMillis(), dataEntry.getDurationNanos(), dataEntry.getParameters(), !dataEntry.getChildren().isEmpty(), level, localIdentifier, parentIdentifier, null);
            rc.add(result);
            fromChilds(dataEntry.getChildren(), rc, level + 1, localIdentifier);
        }
    }
}
