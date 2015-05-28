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
import org.controlsfx.control.*;
import javafx.beans.binding.*;

public class Main extends Application {

    public static int MAX_IMAGE_SIZE = 500;

    public static void main(String[] args) throws Exception {
        launch(new String[] {});
    }

    Surface surf;
    Stc stc;

    RenderCanvas renderCanvas;
    Slider headingSlider;
    Slider pitchSlider;

    ToggleButton leftHemi;
    ToggleButton rightHemi;

    Slider timeSlider;

    RangeSlider thresholdSlider;

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
        headingSlider.valueProperty().addListener(e -> {
                if (!headingSlider.isValueChanging()) {
                    updateRender();
                }
            });
        headingSlider.valueChangingProperty().addListener((e, wasChanging, isChanging) -> {
                if (!isChanging) {
                    updateRender();
                }
            });

        pitchSlider = new Slider(-90, 90, 0);
        pitchSlider.setOrientation(Orientation.VERTICAL);
        pitchSlider.valueProperty().addListener(e -> {
                if (!pitchSlider.isValueChanging()) {
                    updateRender();
                }
            });
        pitchSlider.valueChangingProperty().addListener((e, wasChanging, isChanging) -> {
                if (!isChanging) {
                    updateRender();
                }
            });

        GridPane form = new GridPane();
        form.setHgap(5);
        form.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        form.getColumnConstraints().add(col1);

        form.add(new Label("Hemisphere"), 0, 0);

        HBox hemiButtons = new HBox();
        form.add(hemiButtons, 1, 0);

        leftHemi = new ToggleButton("Left");
        leftHemi.setSelected(true);
        leftHemi.setFocusTraversable(false);
        leftHemi.setOnAction(event -> {
                leftHemi.setSelected(true);
                rightHemi.setSelected(false);
                loadHemisphere("lh");
                updateRender();
            });
        leftHemi.setStyle("-fx-border-radius: 10 0 0 10; -fx-background-radius: 10 0 0 10; -fx-padding: 5 10 5 12;");
        hemiButtons.getChildren().add(leftHemi);

        rightHemi = new ToggleButton("Right");
        rightHemi.setFocusTraversable(false);
        rightHemi.setOnAction(event -> {
                rightHemi.setSelected(true);
                leftHemi.setSelected(false);
                loadHemisphere("rh");
                updateRender();
            });
        rightHemi.setStyle("-fx-border-radius: 0 10 10 0; -fx-background-radius: 0 10 10 0; -fx-padding: 5 12 5 10;");
        hemiButtons.getChildren().add(rightHemi);

        form.add(new Label("Time"), 0, 1);

        timeSlider = new Slider();
        timeSlider.valueProperty().addListener(v -> {
                if (!timeSlider.isValueChanging()) {
                    updateRender();
                }
            });
        form.setHgrow(timeSlider, Priority.ALWAYS);
        form.add(timeSlider, 1, 1);

        form.add(new Label("Thresholds"), 0, 2);

        thresholdSlider = new RangeSlider();
        thresholdSlider.setLowValue(0);
        thresholdSlider.setHighValue(1);
        thresholdSlider.lowValueProperty().addListener(v -> {
                if (!thresholdSlider.isLowValueChanging()) {
                    updateRender();
                }
            });
        thresholdSlider.lowValueChangingProperty().addListener((v, wasChanging, isChanging) -> {
                if (!isChanging && !thresholdSlider.isHighValueChanging()) {
                    updateRender();
                }
            });
        thresholdSlider.highValueProperty().addListener(v -> {
                 if (!thresholdSlider.isHighValueChanging()) {
                    updateRender();
                }
            });
        thresholdSlider.highValueChangingProperty().addListener((v, wasChanging, isChanging) -> {
                if (!isChanging && !thresholdSlider.isLowValueChanging()) {
                    updateRender();
                }
            });
        form.setHgrow(thresholdSlider, Priority.ALWAYS);
        form.add(thresholdSlider, 1, 2);

        Label thresholdsLabel = new Label("12 --- 20");
        thresholdsLabel.textProperty().bind(Bindings.format("%.3e --- %.3e", thresholdSlider.lowValueProperty(), thresholdSlider.highValueProperty()));
        form.add(thresholdsLabel, 1, 3);
        form.setHalignment(thresholdsLabel, HPos.CENTER);

        root.getChildren().add(saveButton);
        root.getChildren().add(renderCanvas);
        root.getChildren().add(pitchSlider);
        root.getChildren().add(headingSlider);
        root.getChildren().add(form);

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
                                          .addGap(20))
                                .addNode(form));

        root.setVerticalGroup(root.createSequentialGroup()
                              .addNode(saveButton)
                              .addGap(5)
                              .addGroup(root.createParallelGroup()
                                        .addNode(renderCanvas, 100, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                                        .addNode(pitchSlider, 0, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE))
                              .addGap(4)
                              .addNode(headingSlider, 16)
                              .addGap(5)
                              .addNode(form));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        loadHemisphere("lh");
        updateRender();
    }

    public void loadHemisphere(String hemi) {
        try {
            surf = Surface.load("data/" + hemi + ".inflated");
            stc = Stc.load("data/pas_45_kanizsa-" + hemi + ".stc");

            timeSlider.setMin(stc.tmin);
            timeSlider.setMax(stc.tmin + (stc.data.length - 1) * stc.tstep);
            timeSlider.setSnapToTicks(true);
            timeSlider.setShowTickMarks(true);
            timeSlider.setShowTickLabels(true);
            timeSlider.setMajorTickUnit(50);
            timeSlider.setMinorTickCount(9);
            timeSlider.setBlockIncrement(stc.tstep);

            thresholdSlider.setMin(0);
            double maxValue = 0;
            for (int t = 0; t < stc.data.length; t++) {
                for (int i = 0; i < stc.data[t].length; i++) {
                    if (stc.data[t][i] > maxValue) maxValue = stc.data[t][i];
                }
            }
            thresholdSlider.setMax(maxValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateRender() {
        RenderParams params = new RenderParams();
        params.heading = Math.toRadians(headingSlider.getValue());
        params.pitch = Math.toRadians(pitchSlider.getValue());
        params.surf = surf;
        params.stc = stc;
        params.time = (int) Math.round((timeSlider.getValue() - stc.tmin) / stc.tstep);
        params.lowThreshold = thresholdSlider.getLowValue();
        params.highThreshold = thresholdSlider.getHighValue();
        renderCanvas.updateRender(params);
    }

}
