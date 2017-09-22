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
