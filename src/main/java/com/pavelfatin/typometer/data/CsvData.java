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
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

class CsvData {
    private static final String QUOTE = "\"";
    private static final String FIELD_SEPARATOR = ",";
    private static final String LINE_SEPARATOR = "\r\n"; // RFC 4180
    private static final DecimalFormat FIELD_FORMAT = createDecimalFormat(Locale.US, 2);

    private CsvData() {
    }

    private static DecimalFormat createDecimalFormat(Locale locale, int fractionDigits) {
        DecimalFormat result = new DecimalFormat();
        result.setDecimalFormatSymbols(new DecimalFormatSymbols(locale));
        result.setMinimumFractionDigits(fractionDigits);
        result.setMaximumFractionDigits(fractionDigits);
        result.setGroupingUsed(false);
        return result;
    }

    static void write(BufferedWriter writer, Collection<Column> columns) throws IOException {
        Collection<String> titles = columns.stream().map(Column::getTitle).collect(toList());
        Collection<Collection<Double>> values = columns.stream().map(Column::getValues).collect(toList());

        writeTitles(writer, titles);
        writeRecords(writer, values);
    }

    private static void writeTitles(BufferedWriter writer, Collection<String> titles) throws IOException {
        Collection<String> quotedTitles = titles.stream().map(CsvData::quote).collect(toList());

        writer.write(join(FIELD_SEPARATOR, quotedTitles));
        writer.write(LINE_SEPARATOR);
    }

    private static String quote(String s) {
        return s.chars().allMatch(Character::isLetterOrDigit) ? s : QUOTE + s + QUOTE;
    }

    private static void writeRecords(BufferedWriter writer, Collection<Collection<Double>> columns) throws IOException {
        Collection<Iterator<Double>> columnIterators = columns.stream().map(Collection::iterator).collect(toList());

        while (true) {
            Collection<String> record = columnIterators.stream().map(CsvData::nextAsString).collect(toList());

            if (record.stream().allMatch(String::isEmpty)) {
                break;
            }

            writer.write(join(FIELD_SEPARATOR, record));
            writer.write(LINE_SEPARATOR);
        }
    }

    private static String nextAsString(Iterator<Double> it) {
        return it.hasNext() ? FIELD_FORMAT.format((double) it.next()) : "";
    }

    static Collection<Column> read(BufferedReader reader) throws IOException {
        Collection<String> titles = readTitles(reader);
        Iterator<Collection<Double>> columnIterator = readRecords(reader, titles.size()).iterator();

        return titles.stream().map(title -> new Column(title, columnIterator.next())).collect(toList());
    }

    private static Collection<String> readTitles(BufferedReader reader) throws IOException {
        String line = reader.readLine();

        if (line == null) {
            return Collections.emptyList();
        }

        String[] row = line.split(FIELD_SEPARATOR);

        return stream(row).map(CsvData::unquote).collect(toList());
    }

    private static String unquote(String s) {
        return s.startsWith(QUOTE) && s.endsWith(QUOTE) ? s.substring(1, s.length() - 1) : s;
    }

    private static Collection<Collection<Double>> readRecords(BufferedReader reader, int columnCount) throws IOException {
        List<Collection<Double>> columns = Stream.generate(() -> new ArrayList<Double>()).limit(columnCount).collect(toList());

        while (true) {
            String line = reader.readLine();

            if (line == null) {
                break;
            }

            Collection<Optional<Double>> record = stream(line.split(FIELD_SEPARATOR)).map(s -> parseField(s)).collect(toList());

            int index = 0;
            for (Optional<Double> field : record) {
                field.ifPresent(columns.get(index)::add);
                index++;
            }
        }

        return columns;
    }

    private static Optional<Double> parseField(String s) {
        try {
            return s.isEmpty() ? Optional.empty() : Optional.of(FIELD_FORMAT.parse(s).doubleValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
