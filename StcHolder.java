import java.io.*;

public class StcHolder {
    public File file;

    private Stc stc;
    private Surface surf;

    public StcHolder(File file) {
        this.file = file;
    }

    public Stc getStc() throws Exception {
        if (stc == null) {
            stc = Stc.load(file.getPath());
        }
        return stc;
    }

    public String getHemisphere() {
        if (file.getPath().toLowerCase().endsWith("lh.stc")) {
            return "lh";
        } else {
            return "rh";
        }
    }

    public Surface getSurface() throws Exception {
        if (surf == null) {
            surf = Surface.load(file.getParentFile().getPath() + "/" + getHemisphere() + ".inflated");
        }
        return surf;
    }

    @Override public String toString() {
        return file.getName();
    }
}
