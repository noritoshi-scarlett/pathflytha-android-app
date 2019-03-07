package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects;

import java.util.ArrayList;
import java.util.List;

public class Graph {

    public enum EdgeType {
        EDGES_NORMAL,
        EDGES_BAD,
        EDGES_OVER
    }

    private final List<GraphPoint> vertexes;
    private final List<GraphEdge> edges;
    private final List<GraphEdge> edgesBad;
    private final List<OverGraphEdge> edgesOver;
    private final List<GraphConnector> circlesChecked;
    private final List<GraphConnector> circlesAll;

    Graph(  List<GraphPoint> vertexes,
            List<GraphEdge> edges,
            List<GraphEdge> edgesBad,
            List<OverGraphEdge> edgesOver,
            List<GraphConnector> circlesChecked,
            List<GraphConnector> circlesAll) {

        this.vertexes = vertexes;
        this.edges = edges;
        this.edgesBad = edgesBad;
        this.edgesOver = edgesOver;
        this.circlesChecked = circlesChecked;
        this.circlesAll = circlesAll;
    }

    public List<GraphPoint> getVertexes() { return vertexes; }
    public List<GraphEdge> getEdges() { return edges; }
    public List<GraphEdge> getEdgesBad() { return edgesBad; }
    public List<OverGraphEdge> getEdgesOver() { return edgesOver; }
    public List<GraphConnector> getCirclesChecked() { return circlesChecked; }
    public List<GraphConnector> getCirclesAll() { return circlesAll; }

    public static final class GraphBuilder {

        private List<GraphPoint> vertexes;
        private List<GraphEdge> edges;
        private List<GraphEdge> edgesBad;
        private List<OverGraphEdge> edgesOver;

        private GraphBuilder() {
            this.vertexes = new ArrayList<>();
            this.edges = new ArrayList<>();
            this.edgesBad = new ArrayList<>();
            this.edgesOver = new ArrayList<>();
        }

        public static  GraphBuilder init() {
            return new GraphBuilder();
        }

        public void addVertex(GraphPoint vertex) {
            this.vertexes.add(vertex);
        }

        public void addEdge(GraphEdge edge, EdgeType edgeType) {
            switch (edgeType) {
                case EDGES_NORMAL: this.edges.add(edge);
                case EDGES_BAD: this.edgesBad.add(edge);
                case EDGES_OVER: {
                    if (edge instanceof OverGraphEdge) {
                        this.edgesOver.add((OverGraphEdge) edge);
                    }
                }
            }
        }

        public Graph build(List<GraphConnector> connectorsChecked, List<GraphConnector> connectorsAll) {
            return new Graph(this.vertexes,
                    this.edges,
                    this.edgesBad,
                    this.edgesOver,
                    connectorsChecked,
                    connectorsAll);
        }

        public List<GraphPoint> getVertexes() { return vertexes; }
        public List<GraphEdge> getEdges() { return edges; }
        public List<GraphEdge> getEdgesBad() { return edgesBad; }
        public List<OverGraphEdge> getEdgesOver() { return edgesOver; }

    }
}
