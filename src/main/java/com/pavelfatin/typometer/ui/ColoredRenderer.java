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
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class ColoredRenderer extends DefaultTableCellRenderer {
    private static Optional<Field> ourBackground = createBackgroundField();

    private Map<Integer, Color> myColors = new HashMap<>();

    // Required becase DefaultTableCellRenderer.unselectedBackground is not declared as "protected" yet.
    private static Optional<Field> createBackgroundField() {
        try {
            Field field = Component.class.getDeclaredField("background");
            field.setAccessible(true);
            return Optional.of(field);
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    void setColors(Map<Integer, Color> colors) {
        myColors = colors;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (isSelected) {
            Optional.ofNullable(myColors.get(row)).ifPresent(this::setBackground0);
        }

        return component;
    }

    private void setBackground0(Color color) {
        ourBackground.ifPresent(field -> {
            try {
                field.set(this, color);
            } catch (IllegalAccessException e) {
                // do nothing
            }
        });
    }
}
