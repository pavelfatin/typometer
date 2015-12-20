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

import com.pavelfatin.typometer.metrics.Metrics;
import com.pavelfatin.typometer.screen.ScreenAccessor;

import java.awt.*;
import java.util.concurrent.BlockingQueue;

import static java.lang.Math.round;

class CharReader implements Runnable {
    private final ScreenAccessor myAccessor;
    private final Metrics myMetrics;
    private final int myCount;
    private final BlockingQueue<CharEvent> myQueue;

    CharReader(ScreenAccessor accessor, Metrics metrics, int count, BlockingQueue<CharEvent> queue) {
        myAccessor = accessor;
        myMetrics = metrics;
        myCount = count;
        myQueue = queue;
    }

    @Override
    public void run() {
        Point point = myMetrics.getEffectiveStartingPoint();

        double x = point.x;
        int y = point.y;

        for (int i = 0; i < myCount; i++) {
            try {
                try {
                    ChangeDetector.waitForChange(myAccessor, (int) round(x), y, myMetrics.getBackground());
                    myQueue.put(CharEvent.RECOGNIZED);
                } catch (InterruptedException e) {
                    return;
                }
            } catch (BenchmarkException e) {
                try {
                    myQueue.put(CharEvent.TIMEOUT);
                    return;
                } catch (InterruptedException e1) {
                    return;
                }
            }

            x += myMetrics.getStep();
        }
    }
}
