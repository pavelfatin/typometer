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

package com.pavelfatin.typometer;

import com.pavelfatin.typometer.benchmark.Parameters;
import com.pavelfatin.typometer.ui.MainFrame;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class Application {
    private static final String DEFAULT_TITLE = "Untitled";
    private static final Parameters DEFAULT_PARAMETERS = new Parameters(200, 150, false, 50, 1000);

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.INSTANCE);
        SwingUtilities.invokeLater(() -> openMainForm(DEFAULT_TITLE, DEFAULT_PARAMETERS));
    }

    private static void openMainForm(String title, Parameters parameters) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        MainFrame form = new MainFrame(title, parameters);
        form.pack();
        form.setLocationRelativeTo(null);
        form.setVisible(true);
    }
}

