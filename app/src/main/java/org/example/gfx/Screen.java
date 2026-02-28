package org.example.gfx;

import java.util.Random;

public class Screen extends Bitmap {
    public Random r = new Random();
    public Bitmap test;
    public Bitmap3D perspectiveVision;

    public Screen(int width, int height) {
        super(width, height);
        test = new Bitmap(50, 50);
        for (int i = 0; i < test.pixels.length; i++) {
            test.pixels[i] = r.nextInt();
        }
        perspectiveVision = new Bitmap3D(width, height);
    }

    int t;
    public void render() {
        t++;
        int ox = (int) (Math.sin(t / 5000.0) * 10);
        int oy = (int) (Math.cos(t / 5000.0) * 10);
        clear();
        perspectiveVision.render();
        render(perspectiveVision, 0, 0);
    }

    public void update() {

    }

    public void clear() {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0;
        }
    }
}
