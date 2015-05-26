import javax.swing.*;
import javax.swing.event.*;
import java.awt.Color;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

        MainFrame frame = new MainFrame();
        frame.setVisible(true);
    }

    public static List<Triangle> generateTetrahedron() {
        List<Triangle> tris = new ArrayList<>();

        Vertex v1 = new Vertex(new Point3d(1, 1, 1), 0);
        Vertex v2 = new Vertex(new Point3d(1, -1, -1), 0);
        Vertex v3 = new Vertex(new Point3d(-1, 1, -1), 0);
        Vertex v4 = new Vertex(new Point3d(-1, -1, 1), 0);

        tris.add(new Triangle(v1, v2, v3));
        tris.add(new Triangle(v1, v2, v4));
        tris.add(new Triangle(v1, v3, v4));
        tris.add(new Triangle(v2, v3, v4));

        return tris;
    }
}
