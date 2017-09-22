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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

class FileStorage {
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private FileStorage() {
    }

    static void write(File file, Collection<Measurement> measurements) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), CHARSET)) {
            write(writer, measurements);
        }
    }

    private static void write(BufferedWriter writer, Collection<Measurement> measurements) throws IOException {
        if (measurements.isEmpty()) return;

        Collection<String> titles = measurements.stream().map(Measurement::getTitle).collect(toList());
        Collection<Collection<Double>> columns = measurements.stream().map(Measurement::getDelays).collect(toList());

        CsvData.write(writer, Column.zip(titles, columns));
    }

    static Collection<Measurement> read(File file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            return read(reader);
        }
    }

    private static Collection<Measurement> read(BufferedReader reader) throws IOException {
        Collection<Column> columns = CsvData.read(reader);

        return columns.stream().map(FileStorage::createMeasurement).collect(toList());
    }

    private static Measurement createMeasurement(Column column) {
        return new Measurement(column.getTitle(), column.getValues());
    }
}
