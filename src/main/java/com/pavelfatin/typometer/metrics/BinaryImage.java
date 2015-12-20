/*
 * Copyright (C) 2015 Pavel Fatin <https://pavelfatin.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pavelfatin.typometer.metrics;

class BinaryImage {
    private int myWidth;
    private int myHeight;
    private boolean[][] myData;

    private BinaryImage(int width, int height) {
        myWidth = width;
        myHeight = height;
        myData = new boolean[height][width];
    }

    int getWidth() {
        return myWidth;
    }

    int getHeight() {
        return myHeight;
    }

    boolean get(int x, int y) {
        return myData[y][x];
    }

    void set(int x, int y, boolean b) {
        myData[y][x] = b;
    }

    static BinaryImage createFrom(GreyscaleImage image, int k, int threshold, boolean invert) {
        GreyscaleImage base = invert ? image.erode(k) : image.dilate(k);

        BinaryImage result = new BinaryImage(image.getWidth(), image.getHeight());

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int value = image.get(x, y);
                int background = base.get(x, y);

                int delta = invert ? value - background : background - value;

                if (delta > threshold) {
                    result.set(x, y, true);
                }
            }
        }

        return result;
    }
}
