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
