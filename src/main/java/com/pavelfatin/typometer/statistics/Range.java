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
