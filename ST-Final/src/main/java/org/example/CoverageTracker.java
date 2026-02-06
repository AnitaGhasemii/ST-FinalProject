package org.example;

import java.util.HashSet;
import java.util.Set;

public class CoverageTracker {
    // set for saved node
    // set estfde kardim k tekrari ha hazf she
    private static Set<String> visitedNodes = new HashSet<>();
    // in bakhsh ba code haee k tazrigh kardim run mishe
    public static void log(String nodeId) {
        visitedNodes.add(nodeId);
    }


    public static void clear() {
        visitedNodes.clear();
    }

   // masir haee ke tei shode ta inja
    public static Set<String> getVisitedNodes() {
        return new HashSet<>(visitedNodes);
    }
}

