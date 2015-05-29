public class Pixel {
    // pixel coordinates
    public int x;
    public int y;

    public double value;
    public double depth;
    public Triangle tri;
    public Point3d normal;

    public Pixel(int x, int y, double value, double depth, Triangle tri, Point3d normal) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.depth = depth;
        this.tri = tri;
        this.normal = normal;
    }
}
