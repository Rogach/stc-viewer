import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.ref.WeakReference;

public class Stc {
    public double tmin;
    public double tstep;
    public int[] vertexIndices;
    public double[][] data;
    public boolean noActivity;

    public Stc(double tmin, double tstep, int[] vertexIndices, double[][] data) {
        this.tmin = tmin;
        this.tstep = tstep;
        this.vertexIndices = vertexIndices;
        this.data = data;

        this.noActivity = true;
        activitySearch:
        for (int q = 0; q < data.length; q++) {
            for (int w = 0; w < data[0].length; w++) {
                if (data[q][w] != 0) {
                    this.noActivity = false;
                    break activitySearch;
                }
            }
        }
    }

    private static Map<String, WeakReference<Stc>> stcCache = new HashMap<>();

    public static Stc load(String filename) throws Exception {
        WeakReference<Stc> referenceFromCache = stcCache.get(filename);
        if (referenceFromCache != null && referenceFromCache.get() != null) {
            return referenceFromCache.get();
        }

        long startTime = System.currentTimeMillis();
        File file = new File(filename);
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fch = fis.getChannel()) {
                ByteBuffer buffer = fch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
                buffer.order(ByteOrder.BIG_ENDIAN);

                // read time data in ms
                double tmin = buffer.getFloat();
                double tstep = buffer.getFloat();

                int nvert = buffer.getInt();
                int[] vertexIndices = new int[nvert];
                for (int q = 0; q < nvert; q++) {
                    vertexIndices[q] = buffer.getInt();
                }

                int ntimes = buffer.getInt();
                double[][] data = new double[ntimes][nvert];
                for (int t = 0; t < ntimes; t++) {
                    for (int q = 0; q < nvert; q++) {
                        data[t][q] = buffer.getFloat();
                    }
                }

                System.out.printf("loaded stc with %d vertices and %d time points\n",
                                  nvert,
                                  ntimes);
                Stc stc = new Stc(tmin, tstep, vertexIndices, data);
                stcCache.put(filename, new WeakReference(stc));

                System.out.printf("loading stc took %d ms\n", System.currentTimeMillis() - startTime);
                return stc;
            }
    }

    @Override
    public String toString() {
        return String.format("Stc file with tmin = %.3f, tstep = %.3f, %d vertices and %d times",
                             tmin, tstep, vertexIndices.length, data.length);
    }
}
