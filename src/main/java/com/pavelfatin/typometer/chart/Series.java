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

public class Series {
    private String myTitle;
    private Color myColor;

    private Collection<Double> myValues;

    public Series(String title, Color color, Collection<Double> values) {
        myTitle = title;
        myColor = color;
        myValues = values;
    }

    public String getTitle() {
        return myTitle;
    }

    public Color getColor() {
        return myColor;
    }

    public Collection<Double> getValues() {
        return myValues;
    }

    public int getLength() {
        return myValues.size();
    }

    public double getMaxValue() {
        return myValues.stream().reduce(0.0D, Math::max);
    }
}
