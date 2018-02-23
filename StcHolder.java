import java.io.*;
import java.lang.ref.SoftReference;

public class StcHolder {
    public File file;

    private SoftReference<Stc> stcRef = new SoftReference<Stc>(null);
    private SoftReference<Surface> surfRef = new SoftReference<Surface>(null);

    public StcHolder(File file) {
        this.file = file;
    }

    public Stc getStc() throws Exception {
        Stc cachedStc = stcRef.get();
        if (cachedStc == null) {
            Stc stc = Stc.load(file.getPath());
            stcRef = new SoftReference<Stc>(stc);
            return stc;
        } else {
            return cachedStc;
        }
    }

    public String getHemisphere() {
        if (file.getPath().toLowerCase().endsWith("lh.stc")) {
            return "lh";
        } else {
            return "rh";
        }
    }

    public Surface getSurface() throws Exception {
        Surface cachedSurface = surfRef.get();
        if (cachedSurface == null) {
            Surface surface = Surface.load(file.getParentFile().getPath() + "/" + getHemisphere());
            surfRef = new SoftReference<Surface>(surface);
            return surface;
        } else {
            return cachedSurface;
        }
    }

    @Override public String toString() {
        return file.getName();
    }
}
