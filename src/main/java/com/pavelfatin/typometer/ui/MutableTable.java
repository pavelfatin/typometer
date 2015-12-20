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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Math.min;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

class MutableTable extends JTable {
    void editSelectedRow(int column) {
        if (editCellAt(getSelectedRow(), column)) {
            getEditorComponent().requestFocus();
        }
    }

    void deleteSelectedRows(Consumer<Integer> listener) {
        int selectedIndex = getSelectedRow();

        if (selectedIndex >= 0) {
            Collection<Integer> indices = reverse(getSelectedRowIndices());

            indices.forEach(index -> {
                getMutableModel().removeRow(index);
                listener.accept(index);
            });

            int rowIndexToSelect = min(selectedIndex, getRowCount() - 1);

            if (rowIndexToSelect >= 0) {
                setSelectedRowIndex(rowIndexToSelect);
            }
        }
    }

    boolean canMoveSelectedRowsUp() {
        return !indicesToMoveUp(getSelectedRowIndices()).isEmpty();
    }

    boolean canMoveSelectedRowsDown() {
        return !indicesToMoveDown(getSelectedRowIndices(), getRowCount()).isEmpty();
    }

    void moveSelectedRowsUp(Consumer<Integer> listener) {
        Collection<Integer> selection = getSelectedRowIndices();
        Collection<Integer> indices = indicesToMoveUp(selection);
        moveRows(indices, -1, listener);
        setSelectedRowIndices(combineIndices(selection, indices, -1));
        scrollToRowVisible(getSelectionModel().getMinSelectionIndex());
    }

    void moveSelectedRowsDown(Consumer<Integer> listener) {
        Collection<Integer> selection = getSelectedRowIndices();
        Collection<Integer> indices = indicesToMoveDown(selection, getRowCount());
        moveRows(indices, +1, listener);
        setSelectedRowIndices(combineIndices(selection, indices, +1));
        scrollToRowVisible(getSelectionModel().getMaxSelectionIndex());
    }

    private static Set<Integer> combineIndices(Collection<Integer> original, Collection<Integer> shifted, int delta) {
        HashSet<Integer> result = new HashSet<>(original);
        result.removeAll(shifted);
        shifted.stream().mapToInt(x -> x + delta).forEach(result::add);
        return result;
    }

    private void moveRows(Collection<Integer> indices, int delta, Consumer<Integer> listener) {
        DefaultTableModel model = getMutableModel();

        for (int index : indices) {
            Vector row = (Vector) model.getDataVector().get(index);
            model.removeRow(index);
            model.insertRow(index + delta, row);
            listener.accept(index);
        }
    }

    private static Collection<Integer> indicesToMoveUp(Collection<Integer> indices) {
        Collection<Integer> result = new ArrayList<>();
        int previousIndex = -1;
        for (int index : indices) {
            if (previousIndex < index - 1) {
                result.add(index);
                previousIndex = index - 1;
            } else {
                previousIndex = index;
            }
        }
        return result;
    }

    private static Collection<Integer> indicesToMoveDown(Collection<Integer> indices, int count) {
        List<Integer> reversedIndices = reverse(indices);

        Collection<Integer> result = new ArrayList<>();
        int nextIndex = count;
        for (int index : reversedIndices) {
            if (nextIndex > index + 1) {
                result.add(index);
                nextIndex = index + 1;
            } else {
                nextIndex = index;
            }
        }
        return result;
    }

    private static List<Integer> reverse(Collection<Integer> indices) {
        List<Integer> result = new ArrayList<>(indices);
        Collections.reverse(result);
        return result;
    }

    Collection<Integer> getSelectedRowIndices() {
        return stream(getSelectedRows()).mapToObj(it -> it).collect(toList());
    }

    private void setSelectedRowIndices(Collection<Integer> indices) {
        ListSelectionModel model = getSelectionModel();
        model.setValueIsAdjusting(true);
        model.clearSelection();
        for (int index : indices) {
            model.addSelectionInterval(index, index);
        }
        model.setValueIsAdjusting(false);
    }

    void selectFirstRow(boolean scroll) {
        if (getRowCount() > 0) {
            setSelectedRowIndex(0);

            if (scroll) {
                scrollToRowVisible(0);
            }
        }
    }

    void selectLastRow(boolean scroll) {
        if (getRowCount() > 0) {
            int index = getRowCount() - 1;

            setSelectedRowIndex(index);

            if (scroll) {
                scrollToRowVisible(index);
            }
        }
    }

    void setSelectedRowIndex(int index) {
        getSelectionModel().setSelectionInterval(index, index);
    }

    void scrollToRowVisible(int index) {
        int height = getRowHeight();
        Rectangle rectangle = new Rectangle(0, height * index, getWidth(), height);
        scrollRectToVisible(rectangle);
    }

    Direction getSelectionDirection() {
        ListSelectionModel model = getSelectionModel();
        int leadIndex = model.getLeadSelectionIndex();
        int anchorIndex = model.getAnchorSelectionIndex();
        return leadIndex == anchorIndex ? Direction.INDEFINITE :
                leadIndex > anchorIndex ? Direction.DOWN : Direction.UP;
    }

    private DefaultTableModel getMutableModel() {
        return (DefaultTableModel) getModel();
    }
}
