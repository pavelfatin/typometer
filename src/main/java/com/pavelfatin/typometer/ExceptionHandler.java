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

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    public static final ExceptionHandler INSTANCE = new ExceptionHandler();

    private ExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();

        SwingUtilities.invokeLater(() -> showErrorDialog(e));
    }

    private void showErrorDialog(Throwable e) {
        Window parent = getTopWindow().orElse(null);

        JDialog dialog = createErrorDialog(parent, getStacktraceText(e));

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static JDialog createErrorDialog(Window parent, String text) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setColumns(60);
        area.setRows(20);
        area.setText(text);
        area.setCaretPosition(0);

        JDialog dialog = new JDialog(parent, "Error");
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);
        dialog.getContentPane().add(new JScrollPane(area));

        return dialog;
    }

    private static Optional<Window> getTopWindow() {
        Window[] windows = Frame.getOwnerlessWindows();
        return windows.length > 0 ? Optional.of(windows[0]) : Optional.empty();
    }

    private static String getStacktraceText(Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();

    }

    public static Runnable wrap(Runnable delegate) {
        return () -> {
            try {
                delegate.run();
            } catch (Throwable e) {
                INSTANCE.uncaughtException(Thread.currentThread(), e);
                throw e;
            }
        };
    }
}
