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
