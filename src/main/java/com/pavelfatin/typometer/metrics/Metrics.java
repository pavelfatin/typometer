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
