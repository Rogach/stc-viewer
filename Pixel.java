public class Pixel {
    public double depth;
    public Triangle tri;
    public double normalCos;

    public Pixel(double depth, Triangle tri, double normalCos) {
        this.depth = depth;
        this.tri = tri;
        this.normalCos = normalCos;
    }
}
