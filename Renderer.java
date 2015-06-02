import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;

public class Renderer {

    public static Color POSITIVE_COLOR = Color.YELLOW;
    public static Color NEGATIVE_COLOR = Color.CYAN;
    public static Color SURFACE_COLOR = new Color(100, 100, 100);
    public static int SMOOTH_STEPS = 3;

    private RenderParams params;
    private Renderer oldRenderer;
    private RenderParams oldParams;

    private Surface surf;
    private Stc stc;
    private double screenScale;
    private int[] stcMapping;
    private Pixel[][] zBuffer;
    private BufferedImage result;

    public Renderer(RenderParams params, Renderer oldRenderer) {
        this.params = params;
        this.surf = params.surf;
        this.stc = params.stc;
        this.oldRenderer = oldRenderer;
        if (oldRenderer != null) {
            this.oldParams = oldRenderer.params;
        }
    }

    BufferedImage render() throws Exception {
        if (oldParams != null &&
            oldParams.surf == params.surf &&
            oldParams.width == params.width && oldParams.height == params.height) {
            screenScale = oldRenderer.screenScale;
        } else {
            screenScale = calculateScreenScale(surf.faces, Math.min(params.width, params.height));
        }

        if (oldParams != null &&
            oldParams.surf == params.surf &&
            oldParams.stc == params.stc
            ) {
            stcMapping = oldRenderer.stcMapping;
        } else {
            calculateStcMapping();
        }

        if (oldParams != null &&
            oldParams.surf == params.surf &&
            oldParams.stc == params.stc &&
            oldParams.time == params.time
            ) {
            // don't apply data
        } else {
            applyStcData();
        }

        renderPipeline(surf.faces);

        return result;
    }

    double calculateScreenScale(List<Triangle> tris, double imgSize) {
        double maxDist = 0;
        for (Triangle t : tris) {
            double d = Math.max(t.v1.p.length(), Math.max(t.v2.p.length(), t.v3.p.length()));
            if (d > maxDist) maxDist = d;
        }
        return imgSize / 2 / maxDist * 0.9;
    }

    void calculateStcMapping() {
        stcMapping = new int[surf.vertices.size()];
        for (int stcIdx = 0; stcIdx < stc.vertexIndices.length; stcIdx++) {
            Set<Vertex> currentGeneration = new HashSet<>();
            currentGeneration.add(surf.vertices.get(stc.vertexIndices[stcIdx]));
            for (int i = 0; i < SMOOTH_STEPS - 1; i++) {
                Set<Vertex> nextGeneration = new HashSet<>();
                for (Vertex v : currentGeneration) {
                    stcMapping[v.index] = stcIdx;
                    nextGeneration.addAll(v.neighbours);
                }
                currentGeneration = nextGeneration;
            }
            for (Vertex v : currentGeneration) {
                stcMapping[v.index] = stcIdx;
            }
        }
    }

    void applyStcData() {
        double[] values = stc.data[params.time];
        for (int q = 0; q < surf.vertices.size(); q++) {
            surf.vertices.get(q).value = values[stcMapping[q]];
        }
    }

    void renderPipeline(List<Triangle> tris) {
        if (oldParams != null &&
            oldParams.surf == params.surf &&
            oldParams.stc == params.stc &&
            oldParams.heading == params.heading &&
            oldParams.pitch == params.pitch &&
            oldParams.width == params.width &&
            oldParams.height == params.height
            ) {
            zBuffer = oldRenderer.zBuffer;
        } else {
            calculateCameraTransform();

            zBuffer = new Pixel[params.width][params.height];

            for (Triangle t : tris) {
                rasterizeTriangle(t);
            }
        }

        if (oldParams != null &&
            oldParams.surf == params.surf &&
            oldParams.stc == params.stc &&
            oldParams.heading == params.heading &&
            oldParams.pitch == params.pitch &&
            oldParams.width == params.width &&
            oldParams.height == params.height &&
            oldParams.time == params.time &&
            oldParams.lowThreshold == params.lowThreshold &&
            oldParams.highThreshold == params.highThreshold
            ) {
            result = oldRenderer.result;
        } else {
            result = new BufferedImage(params.width, params.height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < params.width; x++) {
                for (int y = 0; y < params.height; y++) {
                    Pixel p = zBuffer[x][y];
                    if (p != null) {
                        Point3d invLightDir = new Point3d(0, 0, 1);
                        double angleCos = Math.abs(p.normal.angleCos(invLightDir));
                        double val = (p.tri.v1.value + p.tri.v2.value + p.tri.v3.value) / 3;
                        Color c = shadeColor(interpolateColors((val >= 0) ? POSITIVE_COLOR : NEGATIVE_COLOR, SURFACE_COLOR, scaleValue(val)), angleCos);
                        result.setRGB(x, y, getPixel(c));
                    }
                }
            }
        }
    }

    double scaleValue(double val) {
        return Math.min(1, Math.max(0, Math.abs(val) - params.lowThreshold) / (params.highThreshold - params.lowThreshold));
    }

    void rasterizeTriangle(Triangle t) {
        // move to camera space
        Point3d v1p = cameraTransform(t.v1.p);
        Point3d v2p = cameraTransform(t.v2.p);
        Point3d v3p = cameraTransform(t.v3.p);
        Point3d normal = cameraTransform(t.normal);
        // project points on screen
        Point2d p1 = screenProjection(v1p);
        Point2d p2 = screenProjection(v2p);
        Point2d p3 = screenProjection(v3p);

        // our triangles are always very small
        // so we can skip doing barycentrics
        // and just average three vertices
        double depth = (v1p.z + v2p.z + v3p.z) / 3;

        int minX = (int) Math.ceil(Math.min(p1.x, Math.min(p2.x, p3.x)));
        int maxX = (int) Math.floor(Math.max(p1.x, Math.max(p2.x, p3.x)));
        int minY = (int) Math.ceil(Math.min(p1.y, Math.min(p2.y, p3.y)));
        int maxY = (int) Math.floor(Math.max(p1.y, Math.max(p2.y, p3.y)));

        double triangleArea = (p1.y - p3.y) * (p2.x - p3.x) + (p2.y - p3.y) * (p3.x - p1.x);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                double b1 = ((y - p3.y) * (p2.x - p3.x) + (p2.y - p3.y) * (p3.x - x)) / triangleArea;
                double b2 = ((y - p1.y) * (p3.x - p1.x) + (p3.y - p1.y) * (p1.x - x)) / triangleArea;
                double b3 = ((y - p2.y) * (p1.x - p2.x) + (p1.y - p2.y) * (p2.x - x)) / triangleArea;
                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                    Pixel prev = zBuffer[x][y];
                    if (prev == null || prev.depth < depth) {
                        zBuffer[x][y] = new Pixel(x, y, depth, t, normal);
                    }
                }
            }
        }
    }

    private Matrix3 cameraTransformMatrix;
    void calculateCameraTransform() {
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
        cameraTransformMatrix = pitchTransform.multiply(headingTransform);
    }

    Point3d cameraTransform(Point3d pt) {
        return cameraTransformMatrix.multiply(pt);
    }

    Point2d screenProjection(Point3d pt) {
        return new Point2d(pt.x * screenScale + params.width / 2,
                           pt.y * screenScale + params.height / 2);
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
