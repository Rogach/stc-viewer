public class Point3d {
    public double x;
    public double y;
    public double z;

    public Point3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Point3d add(Point3d other) {
        return new Point3d(x + other.getX(), y + other.getY(), z + other.getZ());
    }

    public Point3d sub(Point3d other) {
        return new Point3d(x - other.getX(), y - other.getY(), z - other.getZ());
    }

    public Point3d dot(Point3d other) {
        return new Point3d(x * other.x, y * other.y, z * other.z);
    }

    public Point3d cross(Point3d other) {
        return new Point3d(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x);
    }

    public double dist(Point3d other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /* Distance to origin */
    public double length() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    public Point3d norm() {
        double l = length();
        return new Point3d(x / l, y / l, z / l);
    }

    public double angleCos(Point3d other) {
        // cos = dot(a, b) / (||a|| * ||b||)
        double dot = x * other.x + y * other.y + z * other.z;
        return dot / (this.length() * other.length());
    }

    public double angle(Point3d other) {
        return Math.acos(angleCos(other));
    }

    @Override
    public String toString() {
        return String.format("Point3d(%s, %s, %s)", x, y, z);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Point3d other = (Point3d) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        return true;
    }


}
