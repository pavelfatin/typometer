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

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class Parameters {
    private final int myCount;
    private final int myDelay;
    private final boolean myPausesEnabled;
    private final int myPausePeriod;
    private final int myPauseLength;

    public Parameters(int count, int delay, boolean pausesEnabled, int pausePeriod, int pauseLength) {
        myCount = count;
        myDelay = delay;
        myPausesEnabled = pausesEnabled;
        myPausePeriod = pausePeriod;
        myPauseLength = pauseLength;
    }

    public int getCount() {
        return myCount;
    }

    public int getDelay() {
        return myDelay;
    }

    public boolean isPausesEnabled() {
        return myPausesEnabled;
    }

    public int getPausePeriod() {
        return myPausePeriod;
    }

    public int getPauseLength() {
        return myPauseLength;
    }

    public List<Integer> getDelays() {
        return IntStream.rangeClosed(1, myCount).mapToObj(i -> delayAfter(i)).collect(toList());
    }

    private Integer delayAfter(int index) {
        return myPausesEnabled && index % myPausePeriod == 0 ? myPauseLength : myDelay;
    }

}
