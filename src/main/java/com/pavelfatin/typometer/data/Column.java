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
