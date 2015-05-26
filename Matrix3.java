public class Matrix3 {
    
    public double[] data;

    public Matrix3(double[] data) {
        assert data.length == 9;
        this.data = data;
    }
    
    public Matrix3 multiply(Matrix3 matrix) {
        double[] result = new double[9];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int i = 0; i < 3; i++) {
                    result[y * 3 + x] += data[y * 3 + i] * matrix.data[i * 3 + x];
                }
            }
        }
        return new Matrix3(result);
    }

    public Point3d multiply(Point3d p) {
        double[] input = new double[] { p.x, p.y, p.z };
        double[] result = new double[3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                result[y] += data[y * 3 + x] * input[x];
            }
        }
        return new Point3d(result[0], result[1], result[2]);
    }
    
    public static Matrix3 ID = new Matrix3(new double[] {
        1, 0, 0,
        0, 1, 0,
        0, 0, 1
    });

    @Override
    public String toString() {
        return String.format("%6.3f %6.3f %6.3f\n%6.3f %6.3f %6.3f\n%6.3f %6.3f %6.3f",
                             data[0], data[1], data[2],
                             data[3], data[4], data[5],
                             data[6], data[7], data[8]);
    }
}
