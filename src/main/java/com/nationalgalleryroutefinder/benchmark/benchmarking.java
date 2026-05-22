package com.nationalgalleryroutefinder.benchmark;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.nationalgalleryroutefinder.graph.Graph;
import com.nationalgalleryroutefinder.graph.Vertices;
import com.nationalgalleryroutefinder.util.CSVLoader;
import com.nationalgalleryroutefinder.model.MyArrayList;
import com.nationalgalleryroutefinder.model.Room;
import com.nationalgalleryroutefinder.algos.BFS;
import org.openjdk.jmh.annotations.*;

@Measurement(iterations = 3, time = 3)
@Warmup(iterations = 3, time = 3)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class benchmarking {

    private Graph<Room> graph;
    private int startID;
    private int endID;

    // empty avoided rooms and waypoints for benchmarking purposes
    private final MyArrayList<Room> avoid = new MyArrayList<>();

    @Setup(Level.Invocation)
    public void setup() throws IOException {
        CSVLoader loader = new CSVLoader();

        graph = loader.loadGraph(
                "src/main/resources/csv/rooms.csv",
                "src/main/resources/csv/exhibits.csv",
                "src/main/resources/csv/edges.csv"
        );

        // pick two different random vertices for start and end each invocation
        List<Vertices<Room>> vertices = graph.getAllVertices();

        int startIndex = (int) (Math.random() * vertices.size());
        int endIndex;

        do endIndex = (int) (Math.random() * vertices.size());
        while (endIndex == startIndex);

        startID = vertices.get(startIndex).getData().getId();
        endID   = vertices.get(endIndex).getData().getId();
    }

    // benchmarks BFS traversal between two random roomss
    @Benchmark
    public void runBFS() {
        BFS.traverse(graph, startID, endID);
    }

}