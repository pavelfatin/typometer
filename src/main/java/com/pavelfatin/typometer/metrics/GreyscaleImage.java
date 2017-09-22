/*
 * Copyright 2017 Pavel Fatin, https://pavelfatin.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pavelfatin.typometer.metrics;

import java.awt.image.BufferedImage;

class GreyscaleImage {
    static final int RANGE = 127;

    // Recommended by CIE
    private static final double K_RED = 0.5D * 0.2126D;
    private static final double K_GREEN = 0.5D * 0.7152D;
    private static final double K_BLUE = 0.5D * 0.0722D;

    private int myWidth;
    private int myHeight;
    private byte[][] myData;

    private GreyscaleImage(int width, int height) {
        myWidth = width;
        myHeight = height;
        myData = new byte[height][width];
        ;
    }

    int getWidth() {
        return myWidth;
    }

    int getHeight() {
        return myHeight;
    }

    int get(int x, int y) {
        return myData[y][x];
    }

    void set(int x, int y, int v) {
        myData[y][x] = (byte) v;
    }

    static GreyscaleImage createFrom(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        GreyscaleImage result = new GreyscaleImage(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int luminescence = luminescenceOf(image.getRGB(x, y));
                result.set(x, y, luminescence);
            }
        }

        return result;
    }

    private static byte luminescenceOf(int color) {
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = (color) & 0xFF;
        return (byte) ((K_RED * red + K_GREEN * green + K_BLUE * blue));
    }

    static boolean isInverted(BufferedImage image) {
        return luminescenceOf(image) < RANGE / 2;
    }

    private static byte luminescenceOf(BufferedImage image) {
        int count = 10;

        int dx = image.getWidth() / count;
        int dy = image.getHeight() / count;

        int x = 0;
        int y = 0;

        int sum = 0;

        for (int i = 0; i < count; i++) {
            sum += luminescenceOf(image.getRGB(x, y));
            x += dx;
            y += dy;
        }

        return (byte) (sum / count);
    }

    GreyscaleImage erode(int k) {
        GreyscaleImage result = new GreyscaleImage(myWidth, myHeight);

        for (int y = k; y < myHeight - k; y++) {
            for (int x = k; x < myWidth - k; x++) {
                int v = Integer.MAX_VALUE;
                for (int dy = -k; dy <= k; dy++) {
                    for (int dx = -k; dx <= k; dx++) {
                        v = Math.min(v, myData[y + dy][x + dx]);
                    }
                }
                result.set(x, y, v);
            }
        }

        return result;
    }

    GreyscaleImage dilate(int k) {
        GreyscaleImage result = new GreyscaleImage(myWidth, myHeight);

        for (int y = k; y < myHeight - k; y++) {
            for (int x = k; x < myWidth - k; x++) {
                int v = Integer.MIN_VALUE;
                for (int dy = -k; dy <= k; dy++) {
                    for (int dx = -k; dx <= k; dx++) {
                        v = Math.max(v, myData[y + dy][x + dx]);
                    }
                }
                result.set(x, y, v);
            }
        }

        return result;
    }
}
