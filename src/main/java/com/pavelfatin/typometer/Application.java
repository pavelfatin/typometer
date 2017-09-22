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

