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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Data {
    private final List<Measurement> myMeasurements;

    public Data() {
        this(Collections.emptyList());
    }

    private Data(Collection<Measurement> measurements) {
        myMeasurements = new ArrayList<>(measurements);
    }

    public List<Measurement> getMeasurements() {
        return Collections.unmodifiableList(myMeasurements);
    }

    public Measurement get(int index) {
        return myMeasurements.get(index);
    }

    public void add(Measurement measurement) {
        myMeasurements.add(measurement);
    }

    public void add(Data data) {
        myMeasurements.addAll(data.getMeasurements());
    }

    public void remove(int index) {
        myMeasurements.remove(index);
    }

    public void clear() {
        myMeasurements.clear();
    }

    public void moveUp(int index) {
        Measurement measurement = myMeasurements.get(index);
        myMeasurements.remove(index);
        myMeasurements.add(index - 1, measurement);
    }

    public void moveDown(int index) {
        Measurement measurement = myMeasurements.get(index);
        myMeasurements.remove(index);
        myMeasurements.add(index + 1, measurement);
    }

    public void setTitle(int index, String title) {
        myMeasurements.set(index, new Measurement(title, myMeasurements.get(index).getDelays()));
    }

    public void save(File file) throws IOException {
        FileStorage.write(file, myMeasurements);
    }

    public static Data load(File file) throws IOException {
        Collection<Measurement> measurements = FileStorage.read(file);
        return new Data(measurements);
    }
}
