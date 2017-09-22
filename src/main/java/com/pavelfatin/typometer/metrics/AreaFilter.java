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

import java.awt.*;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

class AreaFilter {
    private static final double EPSILON = 0.01D;

    private AreaFilter() {
    }

    static Collection<Rectangle> filter(Collection<Rectangle> rectangles, int minArea, int maxArea, double maxAspectRatio) {
        return rectangles.stream()
                .filter(r -> isWithin(minArea, areaOf(r), maxArea))
                .filter(r -> isWithin(1.0D / maxAspectRatio, aspectRatioOf(r), maxAspectRatio))
                .collect(toList());
    }

    private static boolean isWithin(int min, int i, int max) {
        return min <= i && i <= max;
    }

    private static boolean isWithin(double min, double i, double max) {
        return (min - EPSILON) < i && i <= (max + 0.01D);
    }

    private static int areaOf(Rectangle rectangle) {
        return rectangle.width * rectangle.height;
    }

    private static double aspectRatioOf(Rectangle r) {
        return (double) r.width / (double) r.height;
    }
}
