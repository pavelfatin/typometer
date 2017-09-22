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
