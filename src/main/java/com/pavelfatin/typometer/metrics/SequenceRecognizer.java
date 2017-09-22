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

package com.pavelfatin.typometer.metrics;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.round;

class SequenceRecognizer {
    private SequenceRecognizer() {
    }

    static List<List<Rectangle>> findSequencesIn(Collection<Rectangle> rectangles, int length, int minStep,
                                                 double maxSizeDeviation, double maxDistanceDeviation) {

        Collection<List<Rectangle>> groups = rectangles.stream().collect(Collectors.groupingBy(r -> r.y)).values();

        return groups.stream()
                .filter(rs -> rs.size() == length)
                .filter(rs -> stepOf(rs) >= minStep)
                .filter(rs -> maxDeviationIn(widthsOf(rs)) < maxSizeDeviation + 0.01D)
                .filter(rs -> maxDeviationIn(heightsOf(rs)) < maxSizeDeviation + 0.01D)
                .filter(rs -> maxDeviationIn(distancesBetween(rs)) < maxDistanceDeviation + 0.01D)
                .collect(Collectors.toList());
    }

    private static int stepOf(List<Rectangle> rectangles) {
        Rectangle first = rectangles.get(0);
        Rectangle last = rectangles.get(rectangles.size() - 1);
        return (int) round((double) (last.x - first.x) / (rectangles.size() - 1));
    }

    private static Collection<Integer> widthsOf(Collection<Rectangle> rectangles) {
        return rectangles.stream().map(r -> r.width).collect(Collectors.toList());
    }

    private static Collection<Integer> heightsOf(Collection<Rectangle> rectangles) {
        return rectangles.stream().map(r -> r.height).collect(Collectors.toList());
    }

    private static Collection<Integer> distancesBetween(Collection<Rectangle> rectangles) {
        List<Integer> xs = rectangles.stream().map(r -> r.x).sorted().collect(Collectors.toList());
        return distancesBetween(xs);
    }

    private static Collection<Integer> distancesBetween(List<Integer> xs) {
        if (xs.size() < 2) return Collections.emptyList();

        Collection<Integer> result = new ArrayList<>();

        int previous = xs.get(0);

        for (int x : xs.subList(1, xs.size())) {
            result.add(x - previous);
            previous = x;
        }

        return result;
    }

    private static double maxDeviationIn(Collection<Integer> values) {
        double average = values.stream().collect(Collectors.averagingInt(it -> it));

        return values.stream().map(it -> abs(it - average)).reduce(0.0D, Math::max);
    }
}
