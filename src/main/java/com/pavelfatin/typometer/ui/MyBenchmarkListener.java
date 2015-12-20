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
