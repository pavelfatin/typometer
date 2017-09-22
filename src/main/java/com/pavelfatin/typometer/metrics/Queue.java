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
