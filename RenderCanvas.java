import javafx.scene.canvas.*;
import javafx.embed.swing.SwingFXUtils;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class RenderCanvas extends Canvas {

    private RenderParams params;

    public void updateRender(RenderParams params) {
        this.params = params;
        updateRender();
    }

    private Renderer oldRenderer = null;

    public void updateRender() {
        if (params != null) {
            GraphicsContext g = this.getGraphicsContext2D();
            params.width = (int) this.getWidth();
            params.height = (int) this.getHeight();
            Renderer r = new Renderer(params, oldRenderer);
            try {
                long stt = System.currentTimeMillis();
                BufferedImage render = r.render();
                long end = System.currentTimeMillis();
                System.out.printf("rendering took %d ms\n", end - stt);

                g.drawImage(SwingFXUtils.toFXImage(render, null), 0, 0);
                oldRenderer = r;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void resize(double width, double height) {
        if (this.widthProperty().get() != width || this.heightProperty().get() != height) {
            this.widthProperty().set(width);
            this.heightProperty().set(height);
            updateRender();
        }
    }

}
