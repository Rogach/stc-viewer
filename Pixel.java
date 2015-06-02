public class Pixel {
    // pixel coordinates
    public int x;
    public int y;

    public double depth;
    public Triangle tri;
    public Point3d normal;

    public Pixel(int x, int y, double depth, Triangle tri, Point3d normal) {
        this.x = x;
        this.y = y;
        this.depth = depth;
        this.tri = tri;
        this.normal = normal;
    }
}
