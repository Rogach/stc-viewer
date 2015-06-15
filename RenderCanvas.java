import javafx.scene.canvas.*;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import javafx.scene.paint.*;

public class RenderCanvas extends Canvas {

    private RenderParams params;

    public void updateRender(RenderParams params) {
        this.params = params;
        updateRender();
    }

    private Renderer oldRenderer = null;

    public BufferedImage renderImage(RenderParams params) throws Exception {
        Renderer r = new Renderer(params, oldRenderer);
        return r.render();
    }

    public void updateRender() {
        GraphicsContext g = this.getGraphicsContext2D();
        if (params != null && params.surf != null && params.stc != null) {
            RenderParams newParams = params.copy();
            newParams.width = (int) this.getWidth();
            newParams.height = (int) this.getHeight();
            Renderer r = new Renderer(newParams, oldRenderer);
            try {
                long stt = System.currentTimeMillis();
                BufferedImage render = r.render();
                r.dispose();
                long end = System.currentTimeMillis();
                System.out.printf("rendering took %d ms\n", end - stt);

                g.drawImage(SwingFXUtils.toFXImage(render, null), 0, 0);
                oldRenderer = r;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            g.setFill(Color.BLACK);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
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
