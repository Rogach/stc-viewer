import javafx.application.Application;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.geometry.*;

public class Main extends Application {

    public static int MAX_IMAGE_SIZE = 500;

    public static void main(String[] args) throws Exception {
        launch(new String[] {});
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("stc viewer");

        GroupLayoutPane root = new GroupLayoutPane();
        root.setPadding(new Insets(5));

        Button saveButton = new Button("Save image");
        saveButton.setOnAction(event -> {
                throw new UnsupportedOperationException("unimpl");
            });

        Canvas renderCanvas = new Canvas() {
                @Override
                public void resize(double width, double height) {
                    if (this.widthProperty().get() != width || this.heightProperty().get() != height) {
                        this.widthProperty().set(width);
                        this.heightProperty().set(height);
                        draw(this);
                    }
                }
            };

        Slider headingSlider = new Slider(0, 360, 180);
        headingSlider.setOrientation(Orientation.HORIZONTAL);

        Slider pitchSlider = new Slider(0, 360, 180);
        pitchSlider.setOrientation(Orientation.VERTICAL);

        root.getChildren().add(saveButton);
        root.getChildren().add(renderCanvas);
        root.getChildren().add(pitchSlider);
        root.getChildren().add(headingSlider);

        root.setHorizontalGroup(root.createParallelGroup()
                                .addGroup(root.createSequentialGroup()
                                          .addNode(saveButton, GroupLayoutPane.PREFERRED_SIZE)
                                          .addGlue())
                                .addGroup(root.createSequentialGroup()
                                          .addNode(renderCanvas, 100, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                                          .addGap(4)
                                          .addNode(pitchSlider, 16))
                                .addGroup(root.createSequentialGroup()
                                          .addNode(headingSlider, 0, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                                          .addGap(20)));

        root.setVerticalGroup(root.createSequentialGroup()
                              .addNode(saveButton)
                              .addGap(5)
                              .addGroup(root.createParallelGroup()
                                        .addNode(renderCanvas, 100, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                                        .addNode(pitchSlider, 0, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE))
                              .addGap(4)
                              .addNode(headingSlider, 16));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    public static void draw(Canvas c) {
        System.out.printf("draw(%s,%s)\n", c.getWidth(), c.getHeight());
        GraphicsContext g = c.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        g.setStroke(Color.RED);
        g.strokeLine(0, 0, c.getWidth(), c.getHeight());
        g.strokeLine(c.getWidth(), 0, 0, c.getHeight());
    }

}
