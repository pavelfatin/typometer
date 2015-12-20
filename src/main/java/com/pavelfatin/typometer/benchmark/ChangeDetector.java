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

package com.pavelfatin.typometer.benchmark;

import com.pavelfatin.typometer.screen.ScreenAccessor;

import java.awt.*;

class ChangeDetector {
    private static final long TIMEOUT = 3000;
    private static final int CHECK_PERIOD = 10000;

    static void waitForChange(ScreenAccessor accessor, int x, int y, Color previousColor) throws BenchmarkException, InterruptedException {
        long before = System.currentTimeMillis();

        do {
            for (int i = 0; i < CHECK_PERIOD; i++) {
                Color color = accessor.getPixelColor(x, y);
                if (!previousColor.equals(color)) {
                    return;
                }
            }
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        } while ((System.currentTimeMillis() - before) < TIMEOUT);

        throw new BenchmarkException("Timeout expired.");
    }
}
