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
