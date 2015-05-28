public class RenderParams {
    public int width;
    public int height;
    public double heading;
    public double pitch;
    public Surface surf;
    public Stc stc;
    public int time;
    public double lowThreshold;
    public double highThreshold;

    public RenderParams copy() {
        RenderParams copy = new RenderParams();
        copy.width = this.width;
        copy.height = this.height;
        copy.heading = this.heading;
        copy.pitch = this.pitch;
        copy.surf = this.surf;
        copy.stc = this.stc;
        copy.time = this.time;
        copy.lowThreshold = this.lowThreshold;
        copy.highThreshold = this.highThreshold;
        return copy;
    }
}
