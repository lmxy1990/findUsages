package com.pan.io.findusages.data;

import java.util.ArrayList;
import java.util.List;

public class UsageCallerNode {

    private final UsageCall call;

    private final List<UsageCallerNode> nextNodes;

    public UsageCallerNode(UsageCall call) {
        this.call = call;
        this.nextNodes = new ArrayList<>();
    }

    public UsageCall getCall() {
        return call;
    }

    public List<UsageCallerNode> getNextNodes() {
        return nextNodes;
    }

    public void addNextNode(UsageCallerNode caller) {
        nextNodes.add(caller);
    }
}
