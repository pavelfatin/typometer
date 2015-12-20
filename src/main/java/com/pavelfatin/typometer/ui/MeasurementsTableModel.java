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

package com.pavelfatin.typometer.ui;

import com.pavelfatin.typometer.data.Measurement;
import com.pavelfatin.typometer.statistics.Statistics;
import com.pavelfatin.typometer.statistics.SummaryStatistics;

import javax.swing.table.DefaultTableModel;
import java.util.Collection;
import java.util.Vector;

import static java.util.stream.Collectors.joining;

class MeasurementsTableModel extends DefaultTableModel {
    public static final String[] COLUMN_NAMES = new String[]{"#", "Title", "Min, ms", "Max, ms", "Avg, ms", "SD, ms"};

    MeasurementsTableModel() {
        super(COLUMN_NAMES, 0);
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Void.class;
            case 1:
                return String.class;
            default:
                return Double.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 1;
    }

    void addRow(Measurement measurement, Statistics statistics) {
        addRow(createRow(measurement, statistics));
    }

    void addRows(Collection<Measurement> measurements) {
        measurements.forEach(measurement -> addRow(createRow(measurement)));
    }

    void clear() {
        int count = getRowCount();

        if (count > 0) {
            dataVector = new Vector();
            fireTableRowsDeleted(0, count - 1);
        }
    }

    private static Object[][] createData(Collection<Measurement> measurements) {
        return measurements.stream().map(MeasurementsTableModel::createRow).toArray(Object[][]::new);
    }

    private static Object[] createRow(Measurement measurement) {
        return createRow(measurement, SummaryStatistics.analyze(measurement.getDelays()));
    }

    private static Object[] createRow(Measurement measurement, Statistics stats) {
        return new Object[]{"#", measurement.getTitle(), stats.getMin(), stats.getMax(), stats.getMean(), stats.getStandardDeviation()};
    }

    String format(Collection<Integer> indices) {
        return format(getDataVector(), indices);
    }

    private static String format(Vector data, Collection<Integer> indices) {
        String delimiter = System.getProperty("line.separator");

        String header = "# Title\tMin\tMax\tAvg\tSD";

        String body = indices.stream()
                .map(index -> format(index, (Vector) data.get(index)))
                .collect(joining(delimiter));

        return header + delimiter + body;
    }

    private static String format(int index, Vector row) {
        return String.format("%d %s\t%.1f\t%.1f\t%.1f\t%.1f", index + 1, (String) row.get(1),
                (Double) row.get(2), (Double) row.get(3), (Double) row.get(4), (Double) row.get(5));
    }

    String format(int index) {
        Vector row = (Vector) getDataVector().get(index);
        return String.format("%s — min: %.1f, max: %.1f, avg: %.1f, SD: %.1f", (String) row.get(1),
                (Double) row.get(2), (Double) row.get(3), (Double) row.get(4), (Double) row.get(5));
    }
}
