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

package com.pavelfatin.typometer.ui;

import com.pavelfatin.typometer.benchmark.BenchmarkListener;
import com.pavelfatin.typometer.benchmark.Parameters;
import com.pavelfatin.typometer.statistics.Statistics;
import com.pavelfatin.typometer.statistics.SummaryStatistics;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

class MyBenchmarkListener implements BenchmarkListener {
    private static final String STATS_FORMAT = "min: %.1f | max: %.1f | avg: %.1f | SD: %.1f";

    private static final int STATS_REFRESH_PERIOD = 10;

    private MainFrame myFrame;

    private Collection<Double> myResults;
    private SummaryStatistics myStats;
    private ProgressDialog myProgressDialog;

    MyBenchmarkListener(MainFrame frame, ProgressDialog dialog) {
        myFrame = frame;
        myProgressDialog = dialog;
    }

    @Override
    public void onStart(Parameters parameters) {
        myResults = new ArrayList<>();
        myStats = new SummaryStatistics();

        myProgressDialog.setMaximum(parameters.getCount());
        myProgressDialog.setValue(0);
    }

    @Override
    public void onPhase(String title) {
        myProgressDialog.setHeader(title);
    }

    @Override
    public void onResult(double value) {
        myResults.add(value);
        myStats.accept(value);

        if (myStats.getCount() % STATS_REFRESH_PERIOD == 0) {
            myProgressDialog.setFooter(format(myStats));
        }

        myProgressDialog.setValue(myResults.size());
    }

    private static String format(Statistics stats) {
        return String.format(STATS_FORMAT, stats.getMin(), stats.getMax(),
                stats.getMean(), stats.getStandardDeviation());
    }

    @Override
    public void onError(String message) {
        myProgressDialog.setHeader(message, Color.RED);
        myProgressDialog.setButtonText("Close");
        myProgressDialog.requestFocus();
    }

    @Override
    public void onFinish() {
        myProgressDialog.dispose();

        myFrame.onResults(myResults, myStats);
    }
}
