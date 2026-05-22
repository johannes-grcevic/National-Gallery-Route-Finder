package com.nationalgalleryroutefinder.model;

import com.nationalgalleryroutefinder.graph.Graph;
import com.nationalgalleryroutefinder.util.CSVLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class ExhibitTest {

    private Graph<Room> graph;
    private Room room18;
    private Exhibit exhibit;

    @BeforeEach
    void setup() throws IOException {
        CSVLoader loader = new CSVLoader();
        graph = loader.loadGraph(
                "src/main/resources/csv/rooms.csv",
                "src/main/resources/csv/exhibits.csv",
                "src/main/resources/csv/edges.csv"
        );

        room18 = graph.getVertex(18).getData();
        // room18 exhibits are stored in a MyHashtable, get first exhibit by title
        exhibit = room18.getExhibit("Drunken Silenus supported by Satyrs");
    }

    @Test
    void getTitle() {
        assertEquals("Drunken Silenus supported by Satyrs", exhibit.getTitle());
    }

    @Test
    void getArtist() {
        assertEquals("Anthony van Dyck", exhibit.getArtist());
    }

    @Test
    void setTitle() {
        exhibit.setTitle("New Title");
        assertEquals("New Title", exhibit.getTitle());
    }

    @Test
    void setArtist() {
        exhibit.setArtist("New Artist");
        assertEquals("New Artist", exhibit.getArtist());
    }
}