public class Triangle {
    public Vertex v1;
    public Vertex v2;
    public Vertex v3;

    public Point3d normal;

    public Triangle(Vertex v1, Vertex v2, Vertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        normal = (v2.p.sub(v1.p)).cross(v3.p.sub(v1.p)).norm();
    }

    @Override
    public String toString() {
        return String.format("Triangle(%s,%s,%s)", v1, v2, v3);
    }
}
