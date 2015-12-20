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

package com.pavelfatin.typometer.data;

import java.util.Collection;
import java.util.Iterator;

import static java.util.stream.Collectors.toList;

class Column {
    private String myTitle;
    private Collection<Double> myValues;

    Column(String title, Collection<Double> values) {
        myTitle = title;
        myValues = values;
    }

    String getTitle() {
        return myTitle;
    }

    Collection<Double> getValues() {
        return myValues;
    }

    static Collection<Column> zip(Collection<String> titles, Collection<Collection<Double>> columns) {
        Iterator<Collection<Double>> columnsIterator = columns.iterator();
        return titles.stream().map(title -> new Column(title, columnsIterator.next())).collect(toList());
    }
}
