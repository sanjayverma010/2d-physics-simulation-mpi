import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SequentialPhysics {

    static final double dt = 0.01;
    static final double gravity = 9.8;

    // ================= BODY =================
    static class Body implements Serializable {
        double x, y, vx, vy, radius;
        boolean colliding = false;

        Body(double x, double y, double vx, double vy, double r) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = r;
        }
    }

    // ================= GUI PANEL =================
    static class SimulationPanel extends JPanel {

        Body[] bodies;
        int totalCollisions = 0;
        int step = 0;

        SimulationPanel(Body[] bodies) {
            this.bodies = bodies;
        }

        public void update(Body[] bodies, int collisions, int step) {
            this.bodies = bodies;
            this.totalCollisions = collisions;
            this.step = step;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int width = getWidth();
            int height = getHeight();

            // Draw boundary box
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, width - 1, height - 1);

            // Draw particles
            for (Body b : bodies) {

                int x = (int) (b.x / 100.0 * width);
                int y = (int) (b.y / 100.0 * height);

                if (b.colliding)
                    g.setColor(Color.RED);
                else
                    g.setColor(Color.BLUE);

                g.fillOval(x, y, 8, 8);

                // reset collision flag
                b.colliding = false;
            }

            // Draw stats
            g.setColor(Color.BLACK);
            g.drawString("Collisions: " + totalCollisions, 20, 20);
            g.drawString("Step: " + step, 20, 40);
        }
    }

    // ================= MAIN =================
    public static void main(String[] args) {

        int totalBodies = 800; // keep moderate for smooth GUI
        Body[] bodies = initBodies(totalBodies);

        JFrame frame = new JFrame("Sequential Physics Simulation");
        SimulationPanel panel = new SimulationPanel(bodies);

        frame.add(panel);
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        int totalCollisions = 0;
        int step = 0;

        long startTime = System.nanoTime();

        // ================= INFINITE LOOP =================
        while (true) {

            // ---- COMPUTATION ----
            for (int i = 0; i < totalBodies; i++) {

                bodies[i].vy -= gravity * dt;
                bodies[i].x += bodies[i].vx * dt;
                bodies[i].y += bodies[i].vy * dt;

                // FIXED BOUNDARY (IMPORTANT)
                if (bodies[i].x < 0) {
                    bodies[i].x = 0;
                    bodies[i].vx *= -1;
                }
                if (bodies[i].x > 100) {
                    bodies[i].x = 100;
                    bodies[i].vx *= -1;
                }

                if (bodies[i].y < 0) {
                    bodies[i].y = 0;
                    bodies[i].vy *= -1;
                }
                if (bodies[i].y > 100) {
                    bodies[i].y = 100;
                    bodies[i].vy *= -1;
                }
            }

            // ---- COLLISION ----
            for (int i = 0; i < totalBodies; i++) {
                for (int j = i + 1; j < totalBodies; j++) {

                    double dx = bodies[i].x - bodies[j].x;
                    double dy = bodies[i].y - bodies[j].y;

                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist < bodies[i].radius + bodies[j].radius) {

                        totalCollisions++;

                        bodies[i].colliding = true;
                        bodies[j].colliding = true;

                        // velocity swap
                        double temp = bodies[i].vx;
                        bodies[i].vx = bodies[j].vx;
                        bodies[j].vx = temp;
                    }
                }
            }

            // ---- GUI UPDATE ----
            panel.update(bodies, totalCollisions, step);
            panel.repaint();

            frame.setTitle("Sequential | Collisions: " + totalCollisions);

            // print time occasionally
            if (step % 100 == 0) {
                long currentTime = System.nanoTime();
                double elapsed = (currentTime - startTime) / 1e9;
                System.out.printf("Time: %.2f sec | Step: %d%n", elapsed, step);
            }

            try {
                Thread.sleep(10); // control speed
            } catch (Exception e) {
            }

            step++;
        }
    }

    // ================= INIT =================
    static Body[] initBodies(int n) {

        Random rand = new Random();
        Body[] arr = new Body[n];

        for (int i = 0; i < n; i++) {
            arr[i] = new Body(
                    rand.nextDouble() * 100,
                    rand.nextDouble() * 100,
                    rand.nextDouble() * 5,
                    rand.nextDouble() * 5,
                    1);
        }

        return arr;
    }
}