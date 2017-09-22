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
