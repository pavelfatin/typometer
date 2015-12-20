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

package com.pavelfatin.typometer.metrics;

import java.util.NoSuchElementException;

class Queue {
    private short[] myElements;
    private int myHead;
    private int myTail;

    Queue(int size) {
        myElements = new short[size];
    }

    boolean isEmpty() {
        return myHead == myTail;
    }

    void add(int element) {
        myElements[myTail] = (short) element;

        myTail++;

        if (myTail == myElements.length) {
            myTail = 0;
        }

        if (myTail == myHead) {
            short[] newElements = new short[myElements.length * 2];
            System.arraycopy(myElements, myHead, newElements, 0, myElements.length - myHead);
            System.arraycopy(myElements, 0, newElements, myElements.length - myHead, myHead);
            myHead = 0;
            myTail = myElements.length;
            myElements = newElements;
        }
    }

    int remove() {
        int element = myElements[myHead];

        if (myHead == myTail) {
            throw new NoSuchElementException();
        }

        myHead++;

        if (myHead == myElements.length) {
            myHead = 0;
        }

        return element;
    }

    int heapSpace() {
        return myElements.length * 2;
    }
}
