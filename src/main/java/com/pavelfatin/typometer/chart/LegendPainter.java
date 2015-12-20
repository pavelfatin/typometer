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
