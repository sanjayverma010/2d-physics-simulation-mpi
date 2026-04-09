import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import mpi.*;

public class ParallelPhysics {

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

    // ================= PANEL =================
    static class Panel extends JPanel {

        Body[] bodies;
        int collisions;
        int step;

        Panel(Body[] b) {
            bodies = b;
        }

        public void update(Body[] b, int c, int s) {
            bodies = b;
            collisions = c;
            step = s;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int w = getWidth();
            int h = getHeight();

            // boundary box
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, w - 1, h - 1);

            for (Body b : bodies) {
                int x = (int) (b.x / 100.0 * w);
                int y = (int) (b.y / 100.0 * h);

                if (b.colliding)
                    g.setColor(Color.RED);
                else
                    g.setColor(Color.BLUE);

                g.fillOval(x, y, 8, 8);
                b.colliding = false;
            }

            g.setColor(Color.BLACK);
            g.drawString("Collisions: " + collisions, 20, 20);
            g.drawString("Step: " + step, 20, 40);
        }
    }

    // ================= MAIN =================
    public static void main(String[] args) throws Exception {

        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int totalBodies = 800; // slightly larger
        Body[] bodies = initBodies(totalBodies);

        int chunk = totalBodies / size;
        int start = rank * chunk;
        int end = (rank == size - 1) ? totalBodies : start + chunk;

        Panel panel = null;
        JFrame frame = null;

        if (rank == 0) {
            frame = new JFrame("Optimized Parallel Simulation");
            panel = new Panel(bodies);

            frame.add(panel);
            frame.setSize(600, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // when we close framing loop can terminat
            frame.setVisible(true);
        }

        int totalCollisions = 0;
        int step = 0;

        double startTime = MPI.Wtime();

        // ================= LOOP =================
        while (true) {

            int stepCollisions = 0;

            // ---- COMPUTE ----
            for (int i = start; i < end; i++) {

                bodies[i].vy -= gravity * dt;
                bodies[i].x += bodies[i].vx * dt;
                bodies[i].y += bodies[i].vy * dt;

                // boundary fix
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

            // ---- REDUCED COMMUNICATION ----
            if (step % 5 == 0) {
                MPI.COMM_WORLD.Allgather(
                        bodies, start, chunk, MPI.OBJECT,
                        bodies, 0, chunk, MPI.OBJECT);
            }

            // ---- COLLISION ----
            for (int i = start; i < end; i++) {
                for (int j = i + 1; j < totalBodies; j++) {

                    double dx = bodies[i].x - bodies[j].x;
                    double dy = bodies[i].y - bodies[j].y;

                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5)
                        continue;

                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist < bodies[i].radius + bodies[j].radius) {

                        stepCollisions++;

                        bodies[i].colliding = true;
                        bodies[j].colliding = true;

                        double temp = bodies[i].vx;
                        bodies[i].vx = bodies[j].vx;
                        bodies[j].vx = temp;
                    }
                }
            }

            // ---- REDUCE ----
            int[] global = new int[1];

            MPI.COMM_WORLD.Reduce(
                    new int[] { stepCollisions }, 0,
                    global, 0, 1, MPI.INT, MPI.SUM, 0);

            // ---- GUI UPDATE ----
            if (rank == 0) {

                totalCollisions += global[0];

                panel.update(bodies, totalCollisions, step);
                panel.repaint();

                frame.setTitle("Collisions: " + totalCollisions);

                if (step % 100 == 0) {
                    double t = MPI.Wtime() - startTime;
                    System.out.printf("Time: %.2f sec | Step: %d%n", t, step);
                }
            }

            try {
                Thread.sleep(10);
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