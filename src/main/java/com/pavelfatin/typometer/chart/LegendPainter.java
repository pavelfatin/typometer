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

import java.awt.*;
import java.util.Collection;

class LegendPainter {
    private static final Insets INSETS = new Insets(3, 12, 15, 0);
    private static final Dimension POINT_SIZE = new Dimension(10, 10);
    private static final int VERTICAL_GAP = 12;
    private static final int TEXT_MARGIN = 5;
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    static int heightOf(int count) {
        return INSETS.top + POINT_SIZE.height * count + VERTICAL_GAP * (count - 1) + INSETS.bottom;
    }

    static void paint(Graphics g, Point point, Collection<Series> series) {
        g.setFont(FONT);

        FontMetrics metrics = g.getFontMetrics();

        int maxWidth = 0;
        for (Series it : series) {
            maxWidth = Math.max(metrics.stringWidth(it.getTitle()), maxWidth);
        }

        int y = point.y + INSETS.top;

        for (Series it : series) {
            g.setColor(it.getColor());
            g.fillRect(point.x + INSETS.left, y, POINT_SIZE.width, POINT_SIZE.height);

            g.setColor(Color.BLACK);
            g.drawString(it.getTitle(), point.x + INSETS.left + POINT_SIZE.width + TEXT_MARGIN, y + POINT_SIZE.height);

            y += POINT_SIZE.height + VERTICAL_GAP;
        }
    }
}
