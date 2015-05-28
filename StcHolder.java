import java.io.*;

public class StcHolder {
    public File file;

    public StcHolder(File file) {
        this.file = file;
    }

    public Stc getStc() throws Exception {
        return Stc.load(file.getPath());
    }

    public String getHemisphere() {
        if (file.getPath().toLowerCase().endsWith("lh.stc")) {
            return "lh";
        } else {
            return "rh";
        }
    }

    public Surface getSurface() throws Exception {
        return Surface.load(file.getParentFile().getPath() + "/" + getHemisphere() + ".inflated");
    }

    @Override public String toString() {
        return file.getName();
    }
}
