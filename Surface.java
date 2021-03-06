import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Surface {
    public List<Vertex> vertices;
    public List<Triangle> faces;

    public Surface(List<Vertex> vertices, List<Triangle> faces) {
        this.vertices = vertices;
        this.faces = faces;
    }

    private static Map<String, Surface> surfaceCache = new HashMap<>();

    public static Surface load(String filenameTemplate) throws Exception {
        Surface fromCache = surfaceCache.get(filenameTemplate);
        if (fromCache != null) return fromCache;

        long startTime = System.currentTimeMillis();

        File curvFile = new File(filenameTemplate + ".curv");
        boolean[] isSulcus = null;
        boolean hasSulcusInfo = curvFile.exists();
        if (hasSulcusInfo) {
            try (FileInputStream fis = new FileInputStream(curvFile);
                 FileChannel fch = fis.getChannel()
                 ) {
                ByteBuffer buffer = fch.map(FileChannel.MapMode.READ_ONLY, 0, curvFile.length());
                buffer.order(ByteOrder.BIG_ENDIAN);

                // read signature
                byte sig1 = buffer.get();
                byte sig2 = buffer.get();
                byte sig3 = buffer.get();
                if (sig1 != (byte) 0xff || sig2 != (byte) 0xff || sig3 != (byte) 0xff) {
                    throw new Exception("input file is not in freesurfer sulc format");
                }

                int nvert = buffer.getInt();
                isSulcus = new boolean[nvert];

                buffer.getInt(); // fnum?
                buffer.getInt(); // vals_per_vertex == 1?
                for (int i = 0; i < nvert; i++) {
                    isSulcus[i] = buffer.getFloat() > 0;
                }
            }
        }

        File surfaceFile = new File(filenameTemplate + ".inflated");
        boolean isLeft = filenameTemplate.endsWith("lh");
        try (FileInputStream fis = new FileInputStream(surfaceFile);
             FileChannel fch = fis.getChannel()) {
                ByteBuffer buffer = fch.map(FileChannel.MapMode.READ_ONLY, 0, surfaceFile.length());
                buffer.order(ByteOrder.BIG_ENDIAN);

                // read signature
                byte sig1 = buffer.get();
                byte sig2 = buffer.get();
                byte sig3 = buffer.get();
                if (sig1 != (byte) 0xff || sig2 != (byte) 0xff || sig3 != (byte) 0xfe) {
                    throw new Exception("input file is not in freesurfer triangle-face surface format");
                }

                // skip two comment strings
                int b;
                while ((b = buffer.get()) != '\n');
                while ((b = buffer.get()) != '\n');

                int nvert = buffer.getInt();
                int nface = buffer.getInt();

                // read vertex coordinates (in mm)
                List<Vertex> vertices = new ArrayList<>(nvert);
                for (int q = 0; q < nvert; q++) {
                    float x = buffer.getFloat();
                    float y = buffer.getFloat();
                    float z = buffer.getFloat();
                    if (isLeft) {
                        vertices.add(new Vertex(new Point3d(y, -z, x), 0, q));
                    } else {
                        vertices.add(new Vertex(new Point3d(-y, -z, -x), 0, q));
                    }
                }
                centerVertices(vertices);

                List<Triangle> faces = new ArrayList<>(nface);
                for (int q = 0; q < nface; q++) {
                    int i1 = buffer.getInt();
                    int i2 = buffer.getInt();
                    int i3 = buffer.getInt();
                    vertices.get(i1).neighbours.add(vertices.get(i2));
                    vertices.get(i1).neighbours.add(vertices.get(i3));
                    vertices.get(i2).neighbours.add(vertices.get(i1));
                    vertices.get(i2).neighbours.add(vertices.get(i3));
                    vertices.get(i3).neighbours.add(vertices.get(i1));
                    vertices.get(i3).neighbours.add(vertices.get(i2));
                    boolean inSulcus = hasSulcusInfo && isSulcus[i1] && isSulcus[i2] && isSulcus[i3];
                    faces.add(new Triangle(vertices.get(i1), vertices.get(i2), vertices.get(i3), inSulcus));
                }
                System.out.printf("loaded surface with %d vertices and %d faces\n",
                                  vertices.size(),
                                  faces.size());
                Surface surf = new Surface(vertices, faces);
                surfaceCache.put(filenameTemplate, surf);

                System.out.printf("loading surface took %d ms\n", System.currentTimeMillis() - startTime);
                return surf;
            }
    }

    @Override
    public String toString() {
        return String.format("Freesurface surface data, with %d vertices and %d faces",
                             vertices.size(), faces.size());
    }

    static void centerVertices(List<Vertex> vertices) {
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
}
