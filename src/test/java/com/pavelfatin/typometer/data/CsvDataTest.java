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

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class CsvDataTest {
    private static final List<Column> DATA = asList(
            new Column("a", asList(1.1D, 2.1D)),
            new Column("b c", asList(1.2D, 2.2D)));

    // RFC 4180
    private static final String CONTENT =
            "a,\"b c\"\r\n" +
            "1.10,1.20\r\n" +
            "2.10,2.20\r\n";

    private static final double EPSILON = 0.001D;

    @Test
    public void read() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(CONTENT));

        Collection<Column> columns = CsvData.read(reader);

        assertEquals(DATA, new ArrayList<>(columns));
    }

    @Test
    public void write() throws IOException {
        StringWriter writer = new StringWriter();

        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        CsvData.write(bufferedWriter, DATA);
        bufferedWriter.flush();

        Assert.assertEquals(CONTENT, writer.toString());
    }

    private static void assertEquals(List<Column> cs1, List<Column> cs2) {
        Assert.assertEquals(cs1.size(), cs2.size());

        for (int i = 0; i < cs1.size(); i++) {
            assertEquals(cs1.get(i), cs2.get(i));
        }
    }

    private static void assertEquals(Column c1, Column c2) {
        Assert.assertEquals(c1.getTitle(), c2.getTitle());

        List<Double> xs1 = new ArrayList<>(c1.getValues());
        List<Double> xs2 = new ArrayList<>(c2.getValues());

        Assert.assertEquals(xs1.size(), xs2.size());

        for (int i = 0; i < xs1.size(); i++) {
            assertTrue(Math.abs(xs1.get(i) - xs2.get(i)) < EPSILON);
        }
    }
}
