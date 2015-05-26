public class Pixel {
    // pixel coordinates
    public int x;
    public int y;

    // barycentric coordinates
    public double bar1;
    public double bar2;
    public double bar3;

    public double depth;
    public Triangle tri;
    public Point3d normal;

    public Pixel(int x, int y, double bar1, double bar2, double bar3, double depth, Triangle tri, Point3d normal) {
        this.x = x;
        this.y = y;
        this.bar1 = bar1;
        this.bar2 = bar2;
        this.bar3 = bar3;
        this.depth = depth;
        this.tri = tri;
        this.normal = normal;
    }
}
