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
