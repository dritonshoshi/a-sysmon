package com.nsysmon.data;

import com.ajjpj.afoundation.util.AUUID;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author arno
 */
public class AHierarchicalDataRoot {
    private final AUUID uuid = AUUID.createRandom();
    private final Collection<ACorrelationId> startedFlows;
    private final Collection<ACorrelationId> joinedFlows;
    private final AHierarchicalData root;
    private final boolean wasKilled;

    public AHierarchicalDataRoot(AHierarchicalData root, Collection<ACorrelationId> startedFlows, Collection<ACorrelationId> joinedFlows) {
        this(root, startedFlows, joinedFlows, false);
    }

    public AHierarchicalDataRoot(AHierarchicalData root, Collection<ACorrelationId> startedFlows, Collection<ACorrelationId> joinedFlows, boolean wasKilled) {
        this.startedFlows = new ArrayList<>(startedFlows);
        this.joinedFlows = new ArrayList<>(joinedFlows);
        this.root = root;
        this.wasKilled = wasKilled;
    }

    public AUUID getUuid() {
        return uuid;
    }

    public Collection<ACorrelationId> getStartedFlows() {
        return startedFlows;
    }

    public Collection<ACorrelationId> getJoinedFlows() {
        return joinedFlows;
    }

    public AHierarchicalData getRootNode() {
        return root;
    }

    public boolean isKilled() {
        return wasKilled;
    }

    @Override
    public String toString() {
        return "AHierarchicalDataRoot{" +
                "startedFlows=" + startedFlows +
                ", joinedFlows=" + joinedFlows +
                ", root=" + root +
                ", wasKilled=" + wasKilled +
                '}';
    }
}
