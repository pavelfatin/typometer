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
import java.awt.image.BufferedImage;
import java.util.Optional;

import static java.lang.Math.round;

public class Metrics {
    private final Point myStartingPoint;
    private final double myStep;
    private final int myLineLength;
    private final Color myBackground;
    private final boolean myBlockCursor;

    Metrics(Point startingPoint, double step, int lineLength, Color background, boolean blockCursor) {
        myStartingPoint = startingPoint;
        myStep = step;
        myLineLength = lineLength;
        myBackground = background;
        myBlockCursor = blockCursor;
    }

    public Point getStartingPoint() {
        return myStartingPoint;
    }

    public double getStep() {
        return myStep;
    }

    public int getLineLength() {
        return myLineLength;
    }

    public Color getBackground() {
        return myBackground;
    }

    public boolean isBlockCursor() {
        return myBlockCursor;
    }

    public int getCursorLength() {
        return myBlockCursor ? 1 : 0;
    }

    public Point getEffectiveStartingPoint() {
        return myBlockCursor
                ? new Point((int) round(myStartingPoint.x + myStep), myStartingPoint.y)
                : myStartingPoint;
    }

    public static Optional<Metrics> detect(BufferedImage image1, BufferedImage image2, int count) {
        return MetricsDetector.detect(image1, image2, count);
    }
}
