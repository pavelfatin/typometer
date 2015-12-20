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

package com.pavelfatin.typometer.chart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

class Link extends JLabel {
    private static final Color COLOR = new Color(26, 13, 171);

    private Font myOriginalFont = getFont();
    private Font myUnderlinedFont = underlined(myOriginalFont);

    Link(String text, String toolTipText, ActionListener listener) {
        super(text);

        setForeground(COLOR);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(toolTipText);
        setOpaque(true);
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(0, 3, 3, 0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setFont(myUnderlinedFont);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setFont(myOriginalFont);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                setFont(myOriginalFont);

                listener.actionPerformed(new ActionEvent(this, 0, "pressed"));
            }
        });
    }

    private static Font underlined(Font font) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        return font.deriveFont(attributes);
    }
}
