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
