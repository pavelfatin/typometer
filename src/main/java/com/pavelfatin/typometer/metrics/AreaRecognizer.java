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
