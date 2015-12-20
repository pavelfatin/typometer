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
import java.util.Collections;

public class Measurement {
    private final String myTitle;
    private final Collection<Double> myDelays;

    public Measurement(String title, Collection<Double> delays) {

        myTitle = title;
        myDelays = delays;
    }

    public String getTitle() {
        return myTitle;
    }

    public Collection<Double> getDelays() {
        return Collections.unmodifiableCollection(myDelays);
    }

    public int getLength() {
        return myDelays.size();
    }
}
