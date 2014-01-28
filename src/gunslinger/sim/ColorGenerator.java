package gunslinger.sim;

import java.util.*;
import java.awt.Color;

class ColorGenerator
{
    public static Color getColor(int i) {
        return new Color(getRGB(i+1));
    }


    static int getRGB(int index) {
        int[] p = getPattern(index);
        return getElement(p[0]) << 16 | getElement(p[1]) << 8 | getElement(p[2]);
    }

    static int getElement(int index) {
        int value = index - 1;
        int v = 0;
        for (int i = 0; i < 8; i++) {
            v = v | (value & 1);
            v <<= 1;
            value >>= 1;
        }
        v >>= 1;
        return v & 0xFF;
    }

    static int[] getPattern(int index) {
        int n = (int)Math.cbrt(index);
        index -= (n*n*n);
        int[] p = new int[3];
        Arrays.fill(p,n);
        if (index == 0) {
            return p;
        }
        index--;
        int v = index % 3;
        index = index / 3;
        if (index < n) {
            p[v] = index % n;
            return p;
        }
        index -= n;
        p[v      ] = index / n;
        p[++v % 3] = index % n;
        return p;
    }

    // private static Color getColor(int id) {
    //     float hue = id * 40;
    //        float saturation = 90 + gen.nextFloat() * 10;
    //        float lightness = 50 + gen.nextFloat() * 10;
    //     return new Color(Color.HSBtoRGB(hue, saturation, lightness));
    // }

}
