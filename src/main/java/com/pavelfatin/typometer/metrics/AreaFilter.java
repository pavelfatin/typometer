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
