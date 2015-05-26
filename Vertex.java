import java.util.*;

public class Vertex {
    public Point3d p;
    public double value;
    public Set<Vertex> neighbours = new HashSet<>();

    public Vertex(Point3d p, double value) {
        this.p = p;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("V(%.3f,%.3f,%.3f)", p.x, p.y, p.z);
    }
}
