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

import static javax.swing.SwingUtilities.invokeLater;

class BenchmarkListenerDecorator implements BenchmarkListener {
    private final BenchmarkListener myDelegate;

    BenchmarkListenerDecorator(BenchmarkListener delegate) {
        myDelegate = delegate;
    }

    @Override
    public void onStart(Parameters parameters) {
        invokeLater(() -> myDelegate.onStart(parameters));
    }

    @Override
    public void onPhase(String title) {
        invokeLater(() -> myDelegate.onPhase(title));
    }

    @Override
    public void onResult(double value) {
        invokeLater(() -> myDelegate.onResult(value));
    }

    @Override
    public void onError(String message) {
        invokeLater(() -> myDelegate.onError(message));
    }

    @Override
    public void onFinish() {
        invokeLater(myDelegate::onFinish);
    }
}
