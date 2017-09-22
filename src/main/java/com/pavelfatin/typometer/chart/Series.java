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
