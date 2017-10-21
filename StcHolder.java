import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;

public class StcHolder {
    public File file;

    private WeakReference<Stc> stcRef = new WeakReference(null);
    private WeakReference<Surface> surfRef = new WeakReference(null);

    public StcHolder(File file) {
        this.file = file;
    }

    public Stc getStc() throws Exception {
        Stc cachedStc = stcRef.get();
        if (cachedStc == null) {
            Stc stc = Stc.load(file.getPath());
            stcRef = new WeakReference(stc);
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
            Surface surface = Surface.load(file.getParentFile().getPath() + "/" + getHemisphere() + ".inflated");
            surfRef = new WeakReference(surface);
            return surface;
        } else {
            return cachedSurface;
        }
    }

    @Override public String toString() {
        return file.getName();
    }
}
