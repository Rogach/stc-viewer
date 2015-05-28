import javax.swing.*;
import javax.swing.event.*;
import java.awt.Color;
import java.util.*;

public class MainFrame extends JFrame {
    public static int MAX_IMAGE_SIZE = 500;

    RenderPanel renderPanel;
    JSlider headingSlider;
    JSlider pitchSlider;

    public MainFrame() {
        this.setTitle("stc viewer");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setContentPane(contentPane);

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.setLayout(layout);

        renderPanel = new RenderPanel();
        headingSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 360, 180);
        headingSlider.addChangeListener(e -> {
                if (!headingSlider.getValueIsAdjusting()) {
                    updateRender();
                }
            });
        pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pitchSlider.addChangeListener(e -> {
                if (!pitchSlider.getValueIsAdjusting()) {
                    updateRender();
                }
            });

        layout.setHorizontalGroup(layout.createSequentialGroup()
                                  .addGroup(layout.createParallelGroup()
                                            .addComponent(renderPanel, 0, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                                            .addComponent(headingSlider, 0, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                                            )
                                  .addGroup(layout.createParallelGroup()
                                            .addComponent(pitchSlider, 20, 20, 20))
                                  );
        layout.setVerticalGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup()
                                          .addComponent(renderPanel, 0, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                                          .addComponent(pitchSlider, 0, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                                          )
                                .addGroup(layout.createParallelGroup()
                                          .addComponent(headingSlider, 20, 20, 20))
                                );
        this.pack();
        updateRender();
    }

    public void updateRender() {
        RenderParams params = new RenderParams();
        params.heading = Math.toRadians(headingSlider.getValue());
        params.pitch = Math.toRadians(pitchSlider.getValue());
        params.surfaceFile = "data/lh.inflated";
        params.stcFile = "data/pas_45_kanizsa-lh.stc";
        renderPanel.updateRender(params);
    }

}
