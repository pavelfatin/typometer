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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.stream.Collectors;

class Actions {
    private MainFrame myFrame;

    Actions(MainFrame frame) {
        myFrame = frame;
    }

    private Action myNewAction = createAction("New", "Clear data", "/new.gif",
            InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_N, e -> myFrame.onNew());

    private Action myImportAction = createAction("Import", "Import data from CSV file", "/import.gif",
            InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_O, e -> myFrame.onImport());

    private Action myExportAction = createAction("Export", "Export data to CSV file", "/export.gif",
            InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_S, e -> myFrame.onExport());

    private Action myCopyAction = createAction("Copy", "Copy selected row(s) to clipboard", "/copy.png",
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), KeyEvent.VK_C, e -> myFrame.onCopy());

    private Action myEditAction = createAction("Edit", "Edit title of selected row", "/edit.gif",
            0, KeyEvent.VK_F2, e -> myFrame.onEdit());

    private Action myMoveUpAction = createAction("Up", "Move selected row(s) up", "/up.png",
            InputEvent.ALT_DOWN_MASK, KeyEvent.VK_UP, e -> myFrame.onUp());

    private Action myMoveDownAction = createAction("Down", "Move selected row(s) down", "/down.png",
            InputEvent.ALT_DOWN_MASK, KeyEvent.VK_DOWN, e -> myFrame.onDown());

    private Action myDeleteAction = createAction("Delete", "Delete selected row(s)", "/remove.gif",
            0, KeyEvent.VK_DELETE, e -> myFrame.onDelete());

    void populate(JToolBar toolBar) {
        toolBar.add(myNewAction);
        toolBar.add(myImportAction);
        toolBar.add(myExportAction);
        toolBar.addSeparator();
        toolBar.add(myCopyAction);
        toolBar.addSeparator();
        toolBar.add(myMoveDownAction);
        toolBar.add(myMoveUpAction);
        toolBar.addSeparator();
        toolBar.add(myEditAction);
        toolBar.add(myDeleteAction);
    }

    void register(JRootPane rootPane, JTable table) {
        register(rootPane, myNewAction);
        register(rootPane, myImportAction);
        register(rootPane, myExportAction);

        register(table, myCopyAction);

        removeRecursively(table.getActionMap(), TransferHandler.getCopyAction().getValue(Action.NAME));

        KeyStroke alternativeCopyKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK);
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(alternativeCopyKeyStroke, myCopyAction.getValue(Action.ACTION_COMMAND_KEY));

        register(table, myMoveUpAction);
        register(table, myMoveDownAction);

        register(table, myEditAction);
        register(table, myDeleteAction);
    }

    private static void removeRecursively(ActionMap map, Object key) {
        map.remove(key);
        ActionMap parent = map.getParent();
        if (parent != null) {
            removeRecursively(parent, key);
        }
    }

    void setNewEnabled(boolean b) {
        myNewAction.setEnabled(b);
    }

    void setCopyEnabled(boolean b) {
        myCopyAction.setEnabled(b);
    }

    void setMoveUpEnabled(boolean b) {
        myMoveUpAction.setEnabled(b);
    }

    void setMoveDownEnabled(boolean b) {
        myMoveDownAction.setEnabled(b);
    }

    void setEditEnabled(boolean b) {
        myEditAction.setEnabled(b);
    }

    void setDeleteEnabled(boolean b) {
        myDeleteAction.setEnabled(b);
    }

    private AbstractAction createAction(String name, String description, String icon, int modifiers, int keyCode, ActionListener listener) {
        ImageIcon imageIcon = new ImageIcon(getClass().getResource(icon));
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        AbstractAction action = new AbstractAction(name, imageIcon) {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }
        };
        action.putValue(Action.ACTION_COMMAND_KEY, String.format("ACTION_%s", name.toUpperCase()));
        action.putValue(Action.ACCELERATOR_KEY, keyStroke);
        String accelerator = toCamelCase(keyStroke.toString().replace(" pressed", " +").replace("pressed ", ""));
        action.putValue(Action.SHORT_DESCRIPTION, String.format("%s (%s)", description, accelerator));
        return action;
    }

    private void register(JComponent component, Action action) {
        Object command = action.getValue(Action.ACTION_COMMAND_KEY);

        component.getActionMap().put(command, action);

        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put((KeyStroke) action.getValue(Action.ACCELERATOR_KEY), command);
    }

    private static String toCamelCase(String string) {
        return Arrays.stream(string.toLowerCase().split(" "))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining(" "));
    }
}
