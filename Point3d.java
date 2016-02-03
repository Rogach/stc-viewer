public class Point3d {
    public float x;
    public float y;
    public float z;

    public Point3d(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
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

    public float dist(Point3d other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /* Distance to origin */
    public float length() {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public Point3d norm() {
        float l = length();
        return new Point3d(x / l, y / l, z / l);
    }

    public float angleCos(Point3d other) {
        // cos = dot(a, b) / (||a|| * ||b||)
        float dot = x * other.x + y * other.y + z * other.z;
        return dot / (this.length() * other.length());
    }

    public float angle(Point3d other) {
        return (float) Math.acos(angleCos(other));
    }

    @Override
    public String toString() {
        return String.format("Point3d(%s, %s, %s)", x, y, z);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (int) (Float.floatToIntBits(this.x) ^ (Float.floatToIntBits(this.x) >>> 32));
        hash = 23 * hash + (int) (Float.floatToIntBits(this.y) ^ (Float.floatToIntBits(this.y) >>> 32));
        hash = 23 * hash + (int) (Float.floatToIntBits(this.z) ^ (Float.floatToIntBits(this.z) >>> 32));
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
        if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) {
            return false;
        }
        if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y)) {
            return false;
        }
        if (Float.floatToIntBits(this.z) != Float.floatToIntBits(other.z)) {
            return false;
        }
        return true;
    }


}
