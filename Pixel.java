import java.awt.Color;

public class Pixel {
    public double depth;
    public Triangle tri;
    public double normalCos;
    public Color color;

    public Pixel(double depth, Triangle tri, double normalCos, Color color) {
        this.depth = depth;
        this.tri = tri;
        this.normalCos = normalCos;
        this.color = color;
    }
}
