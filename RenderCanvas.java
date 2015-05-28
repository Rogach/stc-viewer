import javafx.scene.canvas.*;
import javafx.embed.swing.SwingFXUtils;

public class RenderCanvas extends Canvas {

    private RenderParams params;

    public void updateRender(RenderParams params) {
        this.params = params;
        updateRender();
    }

    public void updateRender() {
        if (params != null) {
            GraphicsContext g = this.getGraphicsContext2D();
            params.width = (int) this.getWidth();
            params.height = (int) this.getHeight();
            Renderer r = new Renderer(params);
            g.drawImage(SwingFXUtils.toFXImage(r.render(), null), 0, 0);
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
