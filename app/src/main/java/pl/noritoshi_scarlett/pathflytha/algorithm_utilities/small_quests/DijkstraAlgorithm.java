package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphEdge;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.Graph;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphPoint;

public class DijkstraAlgorithm {

    private final List<GraphEdge> edges;
    private Set<GraphPoint> settledNodes;
    private Set<GraphPoint> unSettledNodes;
    private Map<GraphPoint, GraphPoint> predecessors;
    private Map<GraphPoint, Double> distance;

    public DijkstraAlgorithm(Graph graph) {
        // create a copy of the array so that we can operate on this array
        this.edges = new ArrayList<>(graph.getEdges());
    }

    public void execute(GraphPoint source) {
        settledNodes = new HashSet<>();
        unSettledNodes = new HashSet<>();
        distance = new HashMap<>();
        predecessors = new HashMap<>();
        distance.put(source, 0.0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            GraphPoint node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    private void findMinimalDistances(GraphPoint node) {
        List<GraphPoint> adjacentNodes = getNeighbors(node);
        for (GraphPoint target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }

    }

    private double getDistance(GraphPoint node, GraphPoint target) {
        for (GraphEdge edge : edges) {
            if (edge.getFrom().equals(node)
                    && edge.getTo().equals(target)) {
                return edge.getWeight();
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private List<GraphPoint> getNeighbors(GraphPoint node) {
        List<GraphPoint> neighbors = new ArrayList<>();
        for (GraphEdge edge : edges) {
            if (edge.getFrom().equals(node)
                    && !isSettled(edge.getTo())) {
                neighbors.add(edge.getTo());
            }
        }
        return neighbors;
    }

    private GraphPoint getMinimum(Set<GraphPoint> vertexes) {
        GraphPoint minimum = null;
        for (GraphPoint vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(GraphPoint vertex) {
        return settledNodes.contains(vertex);
    }

    private double getShortestDistance(GraphPoint destination) {
        Double d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

    /*
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     */
    public LinkedList<GraphPoint> getPath(GraphPoint target) {
        LinkedList<GraphPoint> path = new LinkedList<>();
        GraphPoint step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

    public GraphEdge findEdgeWithPoint(GraphPoint current, GraphPoint next) {
        if (edges != null) {
            for (int i = 0; i < edges.size(); i++) {
                if (edges.get(i).getFrom() == current && edges.get(i).getTo() == next) {
                    return edges.get(i);
                }
            }
        }
        return null;
    }

}
