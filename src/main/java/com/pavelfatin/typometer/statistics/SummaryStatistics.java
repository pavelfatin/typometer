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

import java.util.Collection;
import java.util.function.DoubleConsumer;

import static java.lang.Math.*;

public class SummaryStatistics implements Statistics, DoubleConsumer {
    private int myCount = 0;
    private double myMin = Double.MAX_VALUE;
    private double myMax = 0.0D;
    private double myMean = 0.0D;
    private double myS = 0.0D;

    public void accept(double value) {
        myCount++;

        myMin = min(myMin, value);

        myMax = max(myMax, value);

        if (myCount == 1) {
            myMean = value;
        } else {
            double previousMean = myMean;
            myMean += (value - myMean) / myCount;
            myS += (value - previousMean) * (value - myMean);
        }
    }

    public int getCount() {
        return myCount;
    }

    public double getMin() {
        return myCount == 0 ? 0.0D : myMin;
    }

    public double getMax() {
        return myMax;
    }

    public double getRange() {
        return myMin - myMax;
    }

    public double getMean() {
        return myMean;
    }

    public double getStandardDeviation() {
        return sqrt(myS / (myCount - 1));
    }

    public double getRelativeStandardDeviation() {
        return 100.0D * getStandardDeviation() / myMean;
    }

    public static Statistics analyze(Collection<Double> values) {
        SummaryStatistics statistics = new SummaryStatistics();
        values.stream().forEach(statistics::accept);
        return statistics;
    }
}
