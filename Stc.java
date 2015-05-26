import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Stc {
    public double tmin;
    public double tstep;
    public int[] vertexIndices;
    public double[][] data;

    public Stc(double tmin, double tstep, int[] vertexIndices, double[][] data) {
        this.tmin = tmin;
        this.tstep = tstep;
        this.vertexIndices = vertexIndices;
        this.data = data;
    }

    public static Stc load(String filename) throws Exception {
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

                return new Stc(tmin, tstep, vertexIndices, data);
            }
    }

    @Override
    public String toString() {
        return String.format("Stc file with tmin = %.3f, tstep = %.3f, %d vertices and %d times",
                             tmin, tstep, vertexIndices.length, data.length);
    }
}
