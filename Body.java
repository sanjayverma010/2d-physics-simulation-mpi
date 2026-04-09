import java.io.Serializable;

public class Body implements Serializable {
    public double x, y;
    public double vx, vy;
    public double radius;

    public Body(double x, double y, double vx, double vy, double radius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
    }
}
