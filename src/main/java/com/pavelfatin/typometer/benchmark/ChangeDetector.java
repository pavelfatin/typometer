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
