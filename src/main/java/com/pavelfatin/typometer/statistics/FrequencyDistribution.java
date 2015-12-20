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

import java.util.Arrays;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class FrequencyDistribution {
    private Collection<Range> myRanges;
    private Collection<Collection<Double>> myFrequencies;

    FrequencyDistribution(Collection<Range> ranges, Collection<Collection<Double>> frequencies) {
        myRanges = ranges;
        myFrequencies = frequencies;
    }

    public Collection<Range> getRanges() {
        return myRanges;
    }

    public Collection<Collection<Double>> getFrequencies() {
        return myFrequencies;
    }

    public static FrequencyDistribution compute(Collection<Collection<Double>> series, int count) {
        Range range = Range.union(series.stream().map(Range::of).collect(toList()));

        Collection<Range> ranges = range.split(count);

        Collection<Collection<Double>> frequencies = series.stream()
                .map(values -> relative(frequencies(values, ranges))).collect(toList());

        return new FrequencyDistribution(ranges, frequencies);
    }

    private static Collection<Integer> frequencies(Collection<Double> values, Collection<Range> ranges) {
        int[] frequencies = new int[ranges.size()];

        for (Double value : values) {
            int i = 0;
            for (Range range : ranges) {
                if (range.contains(value)) {
                    frequencies[i]++;
                    break;
                }
                i++;
            }
        }

        return Arrays.stream(frequencies).mapToObj(Integer::valueOf).collect(toList());
    }

    private static Collection<Double> relative(Collection<Integer> frequencies) {
        double total = frequencies.stream().mapToInt(Integer::valueOf).sum();
        return frequencies.stream().map(it -> 100.0D * it / total).collect(toList());
    }
}
