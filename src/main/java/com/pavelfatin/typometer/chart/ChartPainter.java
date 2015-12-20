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
import java.util.stream.IntStream;

import static java.lang.Math.min;
import static java.lang.Math.round;

class ChartPainter {
    private static final double POROSITY = 0.5D;
    private static final double MAX_LINE_HEIGHT = 0.95D;

    private static final int MAX_VERTICAL_SPAN_COUNT = 7;

    private static final int LINE_WIDTH = 2;
    private static final int LEDGE_LENGTH = 4;
    private static final int VERTICAL_AXIS_MARGIN = 3;
    private static final int HORIZONTAL_AXIS_MARGIN = 2;

    private static final Insets INSETS = new Insets(15, 9, 9, 15);

    private static final Color GRID_COLOR = new Color(134, 134, 134);

    private static final Font AXIS_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    private ChartPainter() {
    }

    static void paint(Graphics g, Rectangle r, ChartType type, ChartData data) {
        double maxValue = type == ChartType.Line ? (1.0 / MAX_LINE_HEIGHT) * data.getMaxValue() : data.getMaxValue();

        boolean centerCategories = type == ChartType.Bar;

        int horizontalSpanCount = centerCategories ? data.getCategoryCount() : data.getCategoryCount() - 1;
        int verticalSpanCount = min(MAX_VERTICAL_SPAN_COUNT, (int) round(maxValue));

        Rectangle r2 = createInnerRectangle(g, r, maxValue);

        g.setColor(GRID_COLOR);
        drawGrid(g, r2, horizontalSpanCount, verticalSpanCount);

        g.setColor(Color.BLACK);
        g.setFont(AXIS_FONT);
        drawValueAxisCaptions(g, r2, verticalSpanCount, (int) round(maxValue));
        drawCategoryAxisCaptions(g, r2, data.getCategories(), centerCategories);

        if (type == ChartType.Line) {
            ((Graphics2D) g).setStroke(new BasicStroke(LINE_WIDTH));
            drawLines(g, r2, data.getSeries(), data.getMaxLength(), maxValue);
        } else {
            drawBars(g, r2, data.getSeries(), data.getMaxLength(), maxValue);
        }
    }

    private static Rectangle createInnerRectangle(Graphics g, Rectangle r, double range) {
        FontMetrics metrics = g.getFontMetrics(AXIS_FONT);

        int maxValueWidth = metrics.stringWidth(Integer.toString((int) round(range)));
        int leftMargin = INSETS.left + maxValueWidth + VERTICAL_AXIS_MARGIN + LEDGE_LENGTH;

        int valueHeight = metrics.getAscent();
        int bottomMargin = INSETS.bottom + valueHeight + HORIZONTAL_AXIS_MARGIN + LEDGE_LENGTH;

        return new Rectangle(r.x + leftMargin, r.y + INSETS.top,
                r.width - leftMargin - INSETS.right, r.height - INSETS.top - bottomMargin);
    }

    private static void drawGrid(Graphics g, Rectangle r, int hSpanCount, int vSpanCount) {
        g.drawLine(r.x, r.y, r.x, r.y + r.height);

        double hStep = (double) r.width / hSpanCount;

        IntStream.range(0, hSpanCount + 1)
                .map(i -> (int) round(i * hStep))
                .forEach(x -> g.drawLine(r.x + x, r.y + r.height, r.x + x, r.y + r.height + LEDGE_LENGTH));

        double vStep = (double) r.height / vSpanCount;

        IntStream.range(0, vSpanCount + 1)
                .map(i -> (int) round(i * vStep))
                .forEach(y -> g.drawLine(r.x - LEDGE_LENGTH, r.y + y, r.x + r.width, r.y + y));
    }

    private static void drawValueAxisCaptions(Graphics g, Rectangle r, int spanCount, int range) {
        FontMetrics metrics = g.getFontMetrics();

        double step = (double) r.height / spanCount;
        double valueStep = (double) range / spanCount;

        IntStream.range(0, spanCount + 1).forEach(i -> {
            String s = Integer.toString(range - (int) round(valueStep * i));
            int x = r.x - metrics.stringWidth(s) - LEDGE_LENGTH - VERTICAL_AXIS_MARGIN;
            int y = r.y + (int) round(step * i + 0.5D * metrics.getHeight()) - 4;
            g.drawString(s, x, y);
        });
    }

    private static void drawCategoryAxisCaptions(Graphics g, Rectangle r, Collection<String> categories, boolean center) {
        FontMetrics metrics = g.getFontMetrics();

        int count = center ? categories.size() : categories.size() - 1;
        double step = (double) r.width / count;
        double offset = center ? step * (1 - POROSITY) : 0;

        int i = 0;
        for (String categorie : categories) {
            int x = r.x + (int) round(step * i + offset - 0.5D * metrics.stringWidth(categorie));
            int y = r.y + r.height + metrics.getAscent() + LEDGE_LENGTH + HORIZONTAL_AXIS_MARGIN;
            g.drawString(categorie, x, y);
            i++;
        }
    }

    private static void drawLines(Graphics g, Rectangle r, Collection<Series> series, int count, double maxValue) {
        double step = (double) r.width / count;

        for (Series each : series) {
            g.setColor(each.getColor());

            double x0 = r.x + step;
            int y0 = -1;

            for (Double v : each.getValues()) {
                int y1 = r.y + r.height - (int) ((double) r.height * v / maxValue);
                if (y0 >= 0) {
                    g.drawLine((int) round(x0 - step), y0, (int) round(x0), y1);
                }
                y0 = y1;
                x0 += step;
            }
        }
    }

    private static void drawBars(Graphics g, Rectangle r, Collection<Series> series, int count, double maxValue) {
        double step = (double) r.width / count;
        double width = step / (series.size() + 2.0 * POROSITY);

        double offset = r.x + POROSITY * width;

        for (Series each : series) {
            g.setColor(each.getColor());

            double x = offset;

            for (Double v : each.getValues()) {
                int value = (int) ((double) r.height * v / maxValue);
                g.fillRect((int) round(x), r.y + r.height - value, (int) round(width), value);
                x += step;
            }

            offset += width;
        }
    }
}
