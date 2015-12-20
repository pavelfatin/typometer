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
