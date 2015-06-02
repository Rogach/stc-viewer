import javafx.application.Application;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import javafx.geometry.*;
import org.controlsfx.control.*;
import javafx.beans.binding.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import javafx.collections.*;
import java.util.*;

public class Main extends Application {

    public static int MAX_IMAGE_SIZE = 500;

    public static void main(String[] args) throws Exception {
        launch(new String[] {});
    }

    private boolean haltRendering = false;

    Surface surf;
    Stc stc;

    RenderCanvas renderCanvas;
    Slider headingSlider;
    Slider pitchSlider;

    ToggleButton leftHemi;
    ToggleButton rightHemi;

    Slider timeSlider;

    RangeSlider thresholdSlider;

    ListView<StcHolder> stcList;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("stc viewer");

        GroupLayoutPane root = new GroupLayoutPane();
        root.setStyle("-fx-background-color: #aaa");
        root.setPadding(new Insets(5));

        Button saveButton = new Button("Save image");
        saveButton.setOnAction(event -> {
                FileChooser fch = new FileChooser();
                fch.setTitle("Select destination file");
                fch.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files", "*.png"));
                File selectedFile = fch.showSaveDialog(stage);
                if (selectedFile != null) {
                    if (!selectedFile.getPath().toLowerCase().endsWith(".png")) {
                        selectedFile = new File(selectedFile.getPath() + ".png");
                    }
                    RenderParams params = getRenderParams();
                    params.width = MAX_IMAGE_SIZE;
                    params.height = MAX_IMAGE_SIZE;
                    try {
                        BufferedImage img = renderCanvas.renderImage(params);
                        ImageIO.write(img, "png", selectedFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
                if (selectStcForHemisphere("lh")) {
                    leftHemi.setSelected(true);
                    rightHemi.setSelected(false);
                } else {
                    leftHemi.setSelected(false);
                    rightHemi.setSelected(true);
                }
            });
        leftHemi.setStyle("-fx-border-radius: 10 0 0 10; -fx-background-radius: 10 0 0 10; -fx-padding: 5 10 5 12;");
        hemiButtons.getChildren().add(leftHemi);

        rightHemi = new ToggleButton("Right");
        rightHemi.setFocusTraversable(false);
        rightHemi.setOnAction(event -> {
                if (selectStcForHemisphere("rh")) {
                    rightHemi.setSelected(true);
                    leftHemi.setSelected(false);
                } else {
                    rightHemi.setSelected(false);
                    leftHemi.setSelected(true);
                }
            });
        rightHemi.setStyle("-fx-border-radius: 0 10 10 0; -fx-background-radius: 0 10 10 0; -fx-padding: 5 12 5 10;");
        hemiButtons.getChildren().add(rightHemi);

        form.add(new Label("Time"), 0, 1);

        timeSlider = new Slider();
        timeSlider.valueProperty().addListener(v -> {
                updateRender();
            });
        timeSlider.setSnapToTicks(true);
        timeSlider.setShowTickMarks(true);
        timeSlider.setShowTickLabels(true);
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

        ObservableList<StcHolder> stcFiles = FXCollections.observableArrayList();

        HBox stcManipulation = new HBox(5);
        form.add(stcManipulation, 0, 4);
        form.setColumnSpan(stcManipulation, 2);

        Button addStcs = new Button("Add stc file");
        addStcs.setOnAction(e -> {
                FileChooser fch = new FileChooser();
                fch.setTitle("Select stc files");
                fch.getExtensionFilters().add(new FileChooser.ExtensionFilter("stc files", "*.stc"));
                List<File> selectedFiles = fch.showOpenMultipleDialog(stage);
                if (selectedFiles != null) {
                    StcHolder selectedStc = stcList.getSelectionModel().getSelectedItem();
                    for (File f : selectedFiles) {
                        stcFiles.add(new StcHolder(f));
                        FXCollections.sort(stcFiles, (a, b) -> a.toString().compareTo(b.toString()));
                        if (selectedStc != null) {
                            stcList.getSelectionModel().select(selectedStc);
                        }
                    }
                }
            });
        stcManipulation.getChildren().add(addStcs);

        Button clearStcs = new Button("Clear list");
        clearStcs.setOnAction(e -> {
                stcFiles.clear();
            });
        stcManipulation.getChildren().add(clearStcs);

        stcList = new ListView<StcHolder>();
        stcList.setMinHeight(138);
        stcList.setPrefHeight(138);
        stcList.setMaxHeight(138);
        stcList.setItems(stcFiles);
        stcList.getSelectionModel().selectedItemProperty().addListener(e -> {
                StcHolder selectedStc = stcList.getSelectionModel().getSelectedItem();
                if (selectedStc != null) {
                    haltRendering = true;
                    try {
                        Stc stc = selectedStc.getStc();
                        timeSlider.setMin(stc.tmin);
                        timeSlider.setMax(stc.tmin + (stc.data.length - 1) * stc.tstep);
                        timeSlider.setMajorTickUnit(stc.tstep * 10);
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
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    if (selectedStc.getHemisphere().equals("lh")) {
                        leftHemi.setSelected(true);
                        rightHemi.setSelected(false);
                    } else {
                        leftHemi.setSelected(false);
                        rightHemi.setSelected(true);
                    }
                    haltRendering = false;
                }
                updateRender();
            });
        form.add(stcList, 0, 5);
        form.setColumnSpan(stcList, 2);
        form.setHgrow(stcList, Priority.ALWAYS);

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
        updateRender();
    }

    public boolean selectStcForHemisphere(String hemi) {
        StcHolder currentStc = stcList.getSelectionModel().getSelectedItem();
        if (currentStc != null) {
            String targetPath = currentStc.file.getAbsolutePath().replaceAll("(?i)[lr]h.stc", hemi + ".stc").toLowerCase();
            for (StcHolder stc : stcList.getItems()) {
                if (stc.file.getAbsolutePath().equals(targetPath)) {
                    stcList.getSelectionModel().select(stc);
                    return true;
                }
            }
        }
        return false;
    }

    public RenderParams getRenderParams() {
        RenderParams params = new RenderParams();
        params.heading = Math.toRadians(headingSlider.getValue());
        params.pitch = Math.toRadians(pitchSlider.getValue());
        StcHolder selectedStc = stcList.getSelectionModel().getSelectedItem();
        if (selectedStc != null) {
            try {
                params.surf = selectedStc.getSurface();
                params.stc = selectedStc.getStc();
                params.time = (int) Math.round((timeSlider.getValue() - params.stc.tmin) / params.stc.tstep);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        params.lowThreshold = thresholdSlider.getLowValue();
        params.highThreshold = thresholdSlider.getHighValue();
        return params;
    }

    public void updateRender() {
        if (!haltRendering) {
            renderCanvas.updateRender(getRenderParams());
        }
    }

}
