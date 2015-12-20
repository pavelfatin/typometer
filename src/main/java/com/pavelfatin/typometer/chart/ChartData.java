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

import java.util.Collection;

public class ChartData {
    private Collection<String> myCategories;

    private Collection<Series> mySeries;

    public ChartData(Collection<String> categories, Collection<Series> series) {
        myCategories = categories;
        mySeries = series;
    }

    public Collection<String> getCategories() {
        return myCategories;
    }

    public int getCategoryCount() {
        return myCategories.size();
    }

    public Collection<Series> getSeries() {
        return mySeries;
    }

    public int getMaxLength() {
        return mySeries.stream().mapToInt(Series::getLength).reduce(0, Math::max);
    }

    public double getMaxValue() {
        return mySeries.stream().mapToDouble(Series::getMaxValue).reduce(0.0D, Math::max);
    }
}
