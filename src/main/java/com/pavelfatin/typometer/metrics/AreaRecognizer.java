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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

class AreaRecognizer {
    private AreaRecognizer() {
    }

    static Collection<Rectangle> areasIn(BinaryImage image, double threshold, Queue queue) {
        Collection<Rectangle> result = new ArrayList<>();

        Dimension size = new Dimension(image.getWidth(), image.getHeight());

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.get(x, y)) {
                    areaFrom(image, size, x, y, threshold, queue).ifPresent(result::add);
                }
            }
        }

        return result;
    }

    private static Optional<Rectangle> areaFrom(BinaryImage image, Dimension size, int x0, int y0,
                                                double threshold, Queue queue) {
        int count = 0;
        Rectangle result = new Rectangle(x0, y0, 0, 0);

        queue.add(x0);
        queue.add(y0);

        while (!queue.isEmpty()) {
            int x = queue.remove();
            int y = queue.remove();

            count++;
            result.add(new Rectangle(x, y, 1, 1));

            if (x > 0) {
                if (y > 0 && image.get(x - 1, y - 1)) {
                    queue.add(x - 1);
                    queue.add(y - 1);
                    image.set(x - 1, y - 1, false);
                }

                if (image.get(x - 1, y)) {
                    queue.add(x - 1);
                    queue.add(y);
                    image.set(x - 1, y, false);
                }

                if (y < size.height - 1 && image.get(x - 1, y + 1)) {
                    queue.add(x - 1);
                    queue.add(y + 1);
                    image.set(x - 1, y + 1, false);
                }
            }

            if (x < size.width - 1) {
                if (y > 0 && image.get(x + 1, y - 1)) {
                    queue.add(x + 1);
                    queue.add(y - 1);
                    image.set(x + 1, y - 1, false);
                }

                if (image.get(x + 1, y)) {
                    queue.add(x + 1);
                    queue.add(y);
                    image.set(x + 1, y, false);
                }

                if (y < size.height - 1 && image.get(x + 1, y + 1)) {
                    queue.add(x + 1);
                    queue.add(y + 1);
                    image.set(x + 1, y + 1, false);
                }
            }

            if (y > 0 && image.get(x, y - 1)) {
                queue.add(x);
                queue.add(y - 1);
                image.set(x, y - 1, false);
            }

            if (y < size.height - 1 && image.get(x, y + 1)) {
                queue.add(x);
                queue.add(y + 1);
                image.set(x, y + 1, false);
            }
        }

        return count > threshold * (result.width * result.height) ? Optional.of(result) : Optional.<Rectangle>empty();
    }
}
