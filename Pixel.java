public class Pixel {
    public double depth;
    public Triangle tri;
    public Point3d normal;

    public Pixel(double depth, Triangle tri, Point3d normal) {
        this.depth = depth;
        this.tri = tri;
        this.normal = normal;
    }
}
