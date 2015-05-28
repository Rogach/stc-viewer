import javax.swing.*;
import java.awt.Graphics;
import java.awt.image.*;

public class RenderPanel extends JPanel {

    private RenderParams params;

    public void updateRender(RenderParams params) {
        this.params = params;
        repaint();
    }

    private Renderer oldRenderer = null;

    @Override
    public void paintComponent(Graphics g) {
        if (params != null) {
            params.width = (int) this.getWidth();
            params.height = (int) this.getHeight();
            Renderer r = new Renderer(params, oldRenderer);
            try {
                long stt = System.currentTimeMillis();
                BufferedImage render = r.render();
                long end = System.currentTimeMillis();
                System.out.printf("rendering took %d ms\n", end - stt);

                g.drawImage(render, 0, 0, null);
                oldRenderer = r;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
