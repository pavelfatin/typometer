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

package com.pavelfatin.typometer.statistics;

import java.util.ArrayList;
import java.util.Collection;

public class Range {
    private double myMin;
    private double myMax;

    Range(double min, double max) {
        myMin = min;
        myMax = max;
    }

    public double getMin() {
        return myMin;
    }

    public double getMax() {
        return myMax;
    }

    public double getCenter() {
        return 0.5D * (myMin + myMax);
    }

    public double getLength() {
        return myMax - myMin;
    }

    Collection<Range> split(int count) {
        Collection<Range> result = new ArrayList<>();

        double step = getLength() / count;

        double min = myMin;
        for (int i = 0; i < count; i++) {
            result.add(new Range(min, min + step));
            min += step;
        }

        return result;
    }

    static Range of(Collection<Double> values) {
        double min = values.stream().reduce(Double.MAX_VALUE, Math::min);
        double max = values.stream().reduce(Double.MIN_VALUE, Math::max);
        return new Range(min, max);
    }

    Range union(Range other) {
        double min = Math.min(myMin, other.getMin());
        double max = Math.max(myMax, other.getMax());
        return new Range(min, max);
    }

    static Range union(Collection<Range> ranges) {
        return ranges.stream().reduce((a, b) -> a.union(b))
                .orElseThrow(() -> new IllegalArgumentException());
    }

    boolean contains(Double value) {
        return myMin <= value && value < myMax;
    }
}
