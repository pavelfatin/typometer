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
