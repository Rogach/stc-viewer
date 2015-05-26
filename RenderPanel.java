import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;

public class RenderPanel extends JPanel {
    public static Color BACKGROUND_COLOR = Color.BLACK;
    public static Color POSITIVE_COLOR = Color.YELLOW;
    public static Color NEGATIVE_COLOR = Color.CYAN;
    public static Color SURFACE_COLOR = new Color(100, 100, 100);
    public static int SMOOTH_STEPS = 5;

    public Surface surf;
    public Stc stc;

    public RenderParams params = null;

    public RenderPanel() {
        try {
            surf = Surface.load("data/lh.inflated");
            centerVertices(surf.vertices);
            stc = Stc.load("data/pas_45_kanizsa-lh.stc");
            double[] values = stc.data[11];

            double[] smoothValues = new double[surf.vertices.size()];
            int[] smoothCount = new int[surf.vertices.size()];

            for (int q = 0; q < stc.vertexIndices.length; q++) {
                double baseValue = values[q];
                Set<Vertex> visitedVertices = new HashSet<>();
                Set<Vertex> currentGeneration = new HashSet<>();
                currentGeneration.add(surf.vertices.get(stc.vertexIndices[q]));
                for (int i = 0; i < SMOOTH_STEPS; i++) {
                    Set<Vertex> nextGeneration = new HashSet<>();
                    for (Vertex v : currentGeneration) {
                        if (!visitedVertices.contains(v)) {
                            visitedVertices.add(v);
                            smoothValues[v.index] += baseValue;
                            smoothCount[v.index]++;
                            nextGeneration.addAll(v.neighbours);
                        }
                    }
                    currentGeneration = nextGeneration;
                }
            }

            for (int q = 0; q < surf.vertices.size(); q++) {
                int div = smoothCount[q];
                if (div == 0) div = 1;
                surf.vertices.get(q).value = smoothValues[q] / div;
            }

            double lowThreshold = 0.01 * 1e-10;
            double highThreshold = 0.03 * 1e-10;
            double spread = highThreshold - lowThreshold;
            for (Vertex vertex : surf.vertices) {
                double v = vertex.value;
                if (v >= 0) {
                    if (v < lowThreshold) {
                        v = 0;
                    } else {
                        v = (v - lowThreshold) / spread;
                    }
                } else {
                    if (v > -lowThreshold) {
                        v = 0;
                    } else {
                        v = (v + lowThreshold) / spread;
                    }
                }
                vertex.value = v;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setParams(RenderParams params) {
        this.params = params;
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g1) {
        super.paintComponent(g1);
        Graphics2D g = (Graphics2D) g1;
        if (params != null) {
            params.width = this.getWidth();
            params.height = this.getHeight();
            g.drawImage(render(params), 0, 0, null);
        }
    }

    BufferedImage render(RenderParams params) {
        BufferedImage img = new BufferedImage(params.width, params.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        long stt = System.currentTimeMillis();

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, params.width, params.height);

        List<Triangle> tris = surf.faces;
        params.scale = calculateScaleFactor(tris, Math.min(params.width, params.height));
        renderPipeline(params, img, tris);

        long end = System.currentTimeMillis();

        g.setColor(Color.WHITE);
        g.drawString(String.format("Tris: %d", tris.size()), 3, 14 * 1);
        g.drawString(String.format("%d ms", end - stt), 3, 14 * 2);

        g.dispose();
        return img;
    }

    void centerVertices(List<Vertex> vertices) {
        double avgX = 0;
        double avgY = 0;
        double avgZ = 0;
        for (Vertex v : vertices) {
            avgX += v.p.x;
            avgY += v.p.y;
            avgZ += v.p.z;
        }
        avgX /= vertices.size();
        avgY /= vertices.size();
        avgZ /= vertices.size();
        for (Vertex v : vertices) {
            v.p.x -= avgX;
            v.p.y -= avgY;
            v.p.z -= avgZ;
        }
    }

    double calculateScaleFactor(List<Triangle> tris, double imgSize) {
        double maxDist = 0;
        for (Triangle t : tris) {
            double d = Math.max(t.v1.p.length(), Math.max(t.v2.p.length(), t.v3.p.length()));
            if (d > maxDist) maxDist = d;
        }
        return imgSize / 2 / maxDist * 0.9;
    }

    void renderPipeline(RenderParams params, BufferedImage output, List<Triangle> tris) {
        Pixel[][] zBuffer = new Pixel[params.width][params.height];
        int i = 0;
        for (Triangle t : tris) {
            List<Pixel> pixels = rasterizeTriangle(t, params);
            for (Pixel p : pixels) {
                if (zBuffer[p.x][p.y] == null || zBuffer[p.x][p.y].depth < p.depth) {
                    zBuffer[p.x][p.y] = p;
                }
            }
        }
        for (int x = 0; x < params.width; x++) {
            for (int y = 0; y < params.height; y++) {
                Pixel p = zBuffer[x][y];
                if (p != null) {
                    Point3d invLightDir = new Point3d(0, 0, 1);
                    double angleCos = Math.abs(p.normal.angleCos(invLightDir));
                    double val =
                        p.tri.v1.value * p.bar1 +
                        p.tri.v2.value * p.bar2 +
                        p.tri.v3.value * p.bar3;

                    Color c = shadeColor(interpolateColors((val >= 0) ? POSITIVE_COLOR : NEGATIVE_COLOR, SURFACE_COLOR, Math.abs(val)), angleCos);
                    output.setRGB(x, y, getPixel(c));
                }
            }
        }
    }

    List<Pixel> rasterizeTriangle(Triangle t, RenderParams params) {
        // move to camera space
        Point3d v1p = cameraTransform(t.v1.p, params);
        Point3d v2p = cameraTransform(t.v2.p, params);
        Point3d v3p = cameraTransform(t.v3.p, params);
        Point3d normal = cameraTransform(t.normal, params);
        // project points on screen
        Point2d p1 = screenProjection(v1p, params);
        Point2d p2 = screenProjection(v2p, params);
        Point2d p3 = screenProjection(v3p, params);

        int minX = (int) Math.ceil(Math.min(p1.x, Math.min(p2.x, p3.x)));
        int maxX = (int) Math.ceil(Math.max(p1.x, Math.max(p2.x, p3.x)));
        int minY = (int) Math.ceil(Math.min(p1.y, Math.min(p2.y, p3.y)));
        int maxY = (int) Math.ceil(Math.max(p1.y, Math.max(p2.y, p3.y)));

        double triangleArea = (p1.y - p3.y) * (p2.x - p3.x) + (p2.y - p3.y) * (p3.x - p1.x);

        List<Pixel> pixels = new ArrayList<>();
         for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                double b1 = ((y - p3.y) * (p2.x - p3.x) + (p2.y - p3.y) * (p3.x - x)) / triangleArea;
                double b2 = ((y - p1.y) * (p3.x - p1.x) + (p3.y - p1.y) * (p1.x - x)) / triangleArea;
                double b3 = ((y - p2.y) * (p1.x - p2.x) + (p1.y - p2.y) * (p2.x - x)) / triangleArea;
                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                    double depth = v1p.z * b1 + v2p.z * b2 + v3p.z * b3;
                    pixels.add(new Pixel(x, y, b1, b2, b3, depth, t, normal));
                }
            }
        }
        return pixels;
    }

    Point3d cameraTransform(Point3d pt, RenderParams params) {
        Matrix3 headingTransform = new Matrix3(new double[] {
                Math.cos(params.heading), 0, Math.sin(params.heading),
                0, 1, 0,
                -Math.sin(params.heading), 0, Math.cos(params.heading)
            });
        Matrix3 pitchTransform = new Matrix3(new double[] {
                1, 0, 0,
                0, Math.cos(params.pitch), -Math.sin(params.pitch),
                0, Math.sin(params.pitch), Math.cos(params.pitch)
            });
        Matrix3 transform = pitchTransform.multiply(headingTransform);
        return transform.multiply(pt);
    }

    Point2d screenProjection(Point3d pt, RenderParams params) {
        return new Point2d(pt.x * params.scale + params.width / 2,
                           pt.y * params.scale + params.height / 2);
    }

    int getPixel(Color c) {
        return (c.getRed() << 16) + (c.getGreen() << 8) + c.getBlue();
    }

    Color interpolateColors(Color a, Color b, double t) {
        t = Math.max(0d, Math.min(1d, t));
        return new Color((int) (a.getRed() * t + b.getRed() * (1 - t)),
                         (int) (a.getGreen() * t + b.getGreen() * (1 - t)),
                         (int) (a.getBlue() * t + b.getBlue() * (1 - t)));
    }

    Color shadeColor(Color a, double shade) {
        int sh = (int) Math.round(Math.log10(shade) * 20);
        return new Color(Math.max(0, a.getRed() + sh),
                         Math.max(0, a.getGreen() + sh),
                         Math.max(0, a.getBlue() + sh));
    }
}
