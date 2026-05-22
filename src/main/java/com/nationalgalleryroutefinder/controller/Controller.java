package com.nationalgalleryroutefinder.controller;

import com.nationalgalleryroutefinder.algos.BFS;
import com.nationalgalleryroutefinder.algos.BFSPixel;
import com.nationalgalleryroutefinder.algos.Dijkstra;
import com.nationalgalleryroutefinder.graph.Graph;
import com.nationalgalleryroutefinder.main.Application;
import com.nationalgalleryroutefinder.model.MyArrayList;
import com.nationalgalleryroutefinder.model.Point2DInt;
import com.nationalgalleryroutefinder.model.Room;
import com.nationalgalleryroutefinder.util.CSVLoader;
import com.nationalgalleryroutefinder.util.FXUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    BorderPane borderPane;
    @FXML
    private Label statusLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private Pane imageOverlayPane;

    private WritableImage displayedImage;
    private Image blackAndWhiteImage;

    private int startRoomID;
    private int endRoomID;

    private Graph<Room> graph;

    private Point2DInt startPoint = null;
    private Point2DInt endPoint = null;
    private EventHandler<MouseEvent> pointSelectionHandler;

    // FXML fields must be initialized here, not the constructor, otherwise they will load too early.
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imageView.setOnMouseClicked(this::onImageViewMouseClicked);

        pixelBFSGetCoords();

        configureImageViewSize(1200, 600);

        loadFloorPlanImage();
        loadBlackAndWhiteImage();

        createGraph();
    }

    // -------------------------------------------------------------------------
    //  FXML Methods
    // -------------------------------------------------------------------------
    @FXML
    private void showBlackAndWhiteImage() {
        Pane imageContainer = new Pane(new ImageView(blackAndWhiteImage));
        FXUtils.showPopupWindow("Gallery Floor Plan BW", imageContainer, blackAndWhiteImage.getWidth(), blackAndWhiteImage.getHeight(), false);
    }

    @FXML
    private void showBFSPath() {
        startRoomID = Integer.parseInt(FXUtils.showInputDialog("BFS Path", "Enter the ID of the starting room", "Room ID:", "1"));
        endRoomID = Integer.parseInt(FXUtils.showInputDialog("BFS Path", "Enter the ID of the ending room", "Room ID:", "66"));

        List<Room> path = BFS.traverse(graph, startRoomID, endRoomID);

        if (path.isEmpty()) {
            setStatusBar("No BFS path found from room " + startRoomID + " to room " + endRoomID, true);
            return;
        }

        float animationDuration = Float.parseFloat(FXUtils.showInputDialog("BFS Path", "Enter the duration of the animation (in seconds)", "Duration:", "5"));
        drawPathBFS(path, Duration.seconds(animationDuration));
    }

    @FXML
    public void showDijkstraPath() {
        startRoomID = Integer.parseInt(FXUtils.showInputDialog("Dijkstra Path", "Enter the ID of the starting room", "Room ID:", "1"));
        endRoomID = Integer.parseInt(FXUtils.showInputDialog("Dijkstra Path", "Enter the ID of the ending room", "Room ID:", "8"));

        MyArrayList<Room> avoidedRooms = new MyArrayList<>();
        // ask how many rooms to avoid, then collect each room ID to avoid
        int numAvoid = Integer.parseInt(FXUtils.showInputDialog("Dijkstra Path", "Enter the number of rooms to avoid", "Number:", "3"));

        for (int i = 0; i < numAvoid; i++) {
            int avoidID = Integer.parseInt(FXUtils.showInputDialog("Dijkstra Path", "Enter the ID of the room you want to Avoid", "Room ID:", "2"));
            avoidedRooms.add(graph.getVertex(avoidID).getData());
        }

        List<Room> path = Dijkstra.traverse(graph, startRoomID, endRoomID, avoidedRooms);

        if (path.isEmpty()) {
            setStatusBar("No Dijkstra path found from room " + startRoomID + " to room " + endRoomID, true);
            return;
        }

        float animationDuration = Float.parseFloat(FXUtils.showInputDialog("Dijkstra Path", "Enter the duration of the animation (in seconds)", "Duration:", "5"));
        drawPathBFS(path, Duration.seconds(animationDuration));
    }

    @FXML
    public void selectPoints() {
        startPoint = null;
        endPoint = null;

        imageView.removeEventHandler(MouseEvent.MOUSE_CLICKED, pointSelectionHandler);
        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, pointSelectionHandler);

        setStatusBar("Click to select the start and end points", true);
    }

    @FXML
    private void showBFSWithPixels() throws IOException {

        BufferedImage bwImage = ImageIO.read(
                new File("src/main/resources/images/gallery_floor_plan_bw.png")
        );

        if (startPoint == null || endPoint == null) {
            System.out.println("ya done goofed");
            return;
        }

        Point2DInt start = new Point2DInt(startPoint.x(), startPoint.y());
        Point2DInt end = new Point2DInt(endPoint.x(), endPoint.y());

        List<Point2DInt> path = BFSPixel.traverse(bwImage, start, end);

        clearPath();
        float animationDuration = Float.parseFloat(FXUtils.showInputDialog("BFS Path", "Enter the duration of the animation (in seconds)", "Duration:", "5"));

        drawPathBFSPixel(path, Duration.seconds(animationDuration));
    }

    @FXML
    private void quitApplication() {
        System.exit(0);
    }

    // -------------------------------------------------------------------------
    // Event Handlers
    // -------------------------------------------------------------------------

    protected void onImageViewMouseClicked(MouseEvent event) {
        // left mouse button click
        if (event.getButton() == MouseButton.PRIMARY) {
            setStatusBar("Clicked Point: " + "x = " + event.getX() + ", " + "y = " + event.getY(), true);
        }
    }

    // -------------------------------------------------------------------------
    // Utility Methods
    // -------------------------------------------------------------------------
    public void setStatusBar(String message, boolean visible) {
        if (statusLabel == null) return;

        if (message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be blank");
        }

        statusLabel.setText(message);
        statusLabel.setVisible(visible);
    }

    // used to get cords for BFS with pixels
    private void pixelBFSGetCoords() {
        pointSelectionHandler = mouseEvent -> {

            double displayedWidth = imageView.getBoundsInLocal().getWidth();
            double displayedHeight = imageView.getBoundsInLocal().getHeight();

            double scaleX = imageView.getImage().getWidth() / displayedWidth;
            double scaleY = imageView.getImage().getHeight() / displayedHeight;

            int x = (int) (mouseEvent.getX() * scaleX);
            int y = (int) (mouseEvent.getY() * scaleY);

            x = (int) Math.max(0, Math.min(x, imageView.getImage().getWidth() - 1));
            y = (int) Math.max(0, Math.min(y, imageView.getImage().getHeight() - 1));

            if (startPoint == null) {
                startPoint = new Point2DInt(x, y);
            }
            else if (endPoint == null) {
                endPoint = new Point2DInt(x, y);
                imageView.removeEventHandler(MouseEvent.MOUSE_CLICKED, pointSelectionHandler);
            }
        };
    }

    private void drawPathBFS(List<Room> path, Duration duration) {
        // clear the path from the image view
        clearPath();

        System.out.println("Path Size: " + path.size());

        // set the duration of each step in the path
        Duration stepDuration = duration.divide(path.size());
        Timeline timeline = new Timeline();

        // the animated line path that will be drawn on the image view
        Path animatedLinePath = new Path();
        animatedLinePath.setStroke(Color.BLUE);
        animatedLinePath.setStrokeWidth(2);
        animatedLinePath.setManaged(false);

        double scale = Math.min(imageView.getFitWidth() / imageView.getImage().getWidth(), imageView.getFitHeight() / imageView.getImage().getHeight());

        // scale the coordinates of the path to the size of the image view
        double displayedWidth = imageView.getImage().getWidth() * scale;
        double displayedHeight = imageView.getImage().getHeight() * scale;

        double offsetX = (imageView.getFitWidth() - displayedWidth) / 2;
        double offsetY = (imageView.getFitHeight() - displayedHeight) / 2;

        // get the coordinates of the first and last rooms in the path

        Room start = path.get(0);
        Room end = path.get(path.size() - 1);

        double startX = start.getX() * scale + offsetX;
        double startY = start.getY() * scale + offsetY;

        double endX = end.getX() * scale + offsetX;
        double endY = end.getY() * scale + offsetY;

        System.out.println("Path Start: " + start.getX() + ", " + start.getY());
        System.out.println("Path End: " + end.getX() + ", " + end.getY());

        Circle startRoomCircle = new Circle(startX, startY, 5);
        startRoomCircle.setFill(Color.GREEN);
        startRoomCircle.setManaged(false);

        Circle endRoomCircle = new Circle(endX, endY, 5);
        endRoomCircle.setFill(Color.RED);
        endRoomCircle.setManaged(false);

        imageOverlayPane.getChildren().addAll(animatedLinePath, startRoomCircle, endRoomCircle);

        // move to the first room in the path
        animatedLinePath.getElements().add(new MoveTo(startX, startY));

        // skip the first room because it is already drawn
        for (int i = 1; i < path.size(); i++) {
            // get the coordinates of the next room in the path
            Room room = path.get(i);

            double x = room.getX() * scale + offsetX;
            double y = room.getY() * scale + offsetY;

            // create a key frame for each step in the path
            KeyFrame keyFrame = new KeyFrame (
                    // add a delay between each step
                    stepDuration.multiply(i + 1),
                    _ -> {
                        // draw a line to the next room
                        LineTo lineToNextRoom = new LineTo(x, y);

                        animatedLinePath.getElements().add(lineToNextRoom);
                    });

            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }

    private void drawPathBFSPixel(List<Point2DInt> path, Duration duration) {
        // clear the path from the image view
        clearPath();

        System.out.println("Path Size: " + path.size());

        if (path.isEmpty()) return;

        // set the duration of each step in the path
        Duration stepDuration = duration.divide(path.size());
        Timeline timeline = new Timeline();

        // animated path
        Path animatedLinePath = new Path();
        animatedLinePath.setStroke(Color.BLUE);
        animatedLinePath.setStrokeWidth(2);
        animatedLinePath.setManaged(false);

        double scale = Math.min(
                imageView.getFitWidth() / imageView.getImage().getWidth(),
                imageView.getFitHeight() / imageView.getImage().getHeight()
        );

        double displayedWidth = imageView.getImage().getWidth() * scale;
        double displayedHeight = imageView.getImage().getHeight() * scale;

        double offsetX = (imageView.getFitWidth() - displayedWidth) / 2;
        double offsetY = (imageView.getFitHeight() - displayedHeight) / 2;

        // start and end
        Point2DInt start = path.getFirst();
        Point2DInt end = path.getLast();

        double startX = start.x() * scale + offsetX;
        double startY = start.y() * scale + offsetY;

        double endX = end.x() * scale + offsetX;
        double endY = end.y() * scale + offsetY;

        Circle startCircle = new Circle(startX, startY, 5);
        startCircle.setFill(Color.GREEN);
        startCircle.setManaged(false);

        Circle endCircle = new Circle(endX, endY, 5);
        endCircle.setFill(Color.RED);
        endCircle.setManaged(false);

        imageOverlayPane.getChildren().addAll(animatedLinePath, startCircle, endCircle);

        // move to start
        animatedLinePath.getElements().add(new MoveTo(startX, startY));

        for (int i = 1; i < path.size(); i++) {
            Point2DInt p = path.get(i);

            double x = p.x() * scale + offsetX;
            double y = p.y() * scale + offsetY;

            KeyFrame keyFrame = new KeyFrame(
                    stepDuration.multiply(i + 1),
                    _ -> animatedLinePath.getElements().add(new LineTo(x, y))
            );

            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }

    private void clearPath() {
        imageOverlayPane.getChildren().removeIf(node -> node instanceof Circle || node instanceof Path);
    }

    private void createGraph() {
        CSVLoader loader = new CSVLoader();

        try {
            graph = loader.loadGraph(getCSVFilePath("rooms.csv"), getCSVFilePath("exhibits.csv"), getCSVFilePath("edges.csv"));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCSVFilePath(String csvFileName) {
        return "src/main/resources/csv/" + csvFileName;
    }

    private void loadFloorPlanImage() {
        // resize the image to fit the center pane container
        Image floorPlanColorImage = loadImage("/images/gallery_floor_plan.png");

        // initialize the image view with the display image
        displayedImage = new WritableImage(floorPlanColorImage.getPixelReader(), (int) floorPlanColorImage.getWidth(), (int) floorPlanColorImage.getHeight());
        imageView.setImage(displayedImage);
    }

    private void loadBlackAndWhiteImage() {
        blackAndWhiteImage = loadImage("/images/gallery_floor_plan_bw.png");
    }

    private Image loadImage(String resourcePath) {
        InputStream imageStream = Application.class.getResourceAsStream(resourcePath);

        if (imageStream == null) {
            throw new IllegalStateException("Could not find image resource: " + resourcePath);
        }

        Image image = new Image(imageStream);

        if (image.isError()) {
            throw new IllegalStateException("Could not load image resource: " + resourcePath, image.getException());
        }

        return image;
    }

    private void configureImageViewSize(double width, double height) {
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        imageOverlayPane.setPrefWidth(width);
        imageOverlayPane.setPrefHeight(height);
        imageOverlayPane.setMaxWidth(width);
        imageOverlayPane.setMaxHeight(height);
    }
}
