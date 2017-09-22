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
