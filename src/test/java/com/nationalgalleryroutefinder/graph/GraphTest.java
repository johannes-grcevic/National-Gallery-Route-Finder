package com.nationalgalleryroutefinder.graph;

import com.nationalgalleryroutefinder.model.MyArrayList;
import com.nationalgalleryroutefinder.model.Room;
import com.nationalgalleryroutefinder.util.CSVLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GraphTest {
    private Vertices<Room> v18;
    private Vertices<Room> v19;
    private List<Vertices<Room>> allVertices = new MyArrayList<>();

    @BeforeEach
    void setup() throws IOException {
        CSVLoader loader = new CSVLoader();

        Graph<Room> graph = loader.loadGraph(
                "src/main/resources/csv/rooms.csv",
                "src/main/resources/csv/exhibits.csv",
                "src/main/resources/csv/edges.csv"
        );

        v18 = graph.getVertex(18);
        v19 = graph.getVertex(19);

        allVertices = graph.getAllVertices();
    }

    @Test
    void testUndirectedEdge() {
        // v18 connects to: 15, 19, 21, 22, 24
        assertEquals(5, v18.getEdges().size());
        // v19 connects to: 18, 20
        assertEquals(2, v19.getEdges().size());

        // test edge destinations
        assertEquals(15, v18.getEdges().get(0).getDestination().getData().getId());
        assertEquals(19, v18.getEdges().get(1).getDestination().getData().getId());
        assertEquals(18, v19.getEdges().get(0).getDestination().getData().getId());

        // test weights
        assertEquals(1, v18.getEdges().get(0).getWeight());
        assertEquals(1, v19.getEdges().get(0).getWeight());
    }

    @Test
    void getAllVertices() {
        assertFalse(allVertices.isEmpty());
    }
}