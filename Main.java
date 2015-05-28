import javafx.application.Application;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
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

    RenderCanvas renderCanvas;
    Slider headingSlider;
    Slider pitchSlider;

    @Override
    public void start(Stage stage) {
        stage.setTitle("stc viewer");

        GroupLayoutPane root = new GroupLayoutPane();
        root.setStyle("-fx-background-color: #aaa");
        root.setPadding(new Insets(5));

        Button saveButton = new Button("Save image");
        saveButton.setOnAction(event -> {
                throw new UnsupportedOperationException("unimpl");
            });

        renderCanvas = new RenderCanvas();

        headingSlider = new Slider(0, 360, 180);
        headingSlider.setOrientation(Orientation.HORIZONTAL);
        headingSlider.valueChangingProperty().addListener((e, wasChanging, isChanging) -> {
                if (!isChanging) {
                    updateRender();
                }
            });

        pitchSlider = new Slider(-90, 90, 0);
        pitchSlider.setOrientation(Orientation.VERTICAL);
        pitchSlider.valueChangingProperty().addListener((e, wasChanging, isChanging) -> {
                if (!isChanging) {
                    updateRender();
                }
            });

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
        updateRender();
    }

    public void updateRender() {
        RenderParams params = new RenderParams();
        params.heading = Math.toRadians(headingSlider.getValue());
        params.pitch = Math.toRadians(pitchSlider.getValue());
        renderCanvas.updateRender(params);
    }

}
