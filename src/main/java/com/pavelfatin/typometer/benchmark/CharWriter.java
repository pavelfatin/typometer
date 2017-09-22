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

package com.pavelfatin.typometer.benchmark;

import java.awt.*;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.sleep;

class CharWriter implements Runnable {
    private final Robot myRobot;
    private final int myCharacter;
    private final Collection<Integer> myDelays;
    private final BlockingQueue<CharEvent> myQueue;

    CharWriter(Robot robot, int character, Collection<Integer> delays, BlockingQueue<CharEvent> queue) {
        myRobot = robot;
        myCharacter = character;
        myDelays = delays;
        myQueue = queue;
    }

    @Override
    public void run() {
        for (Integer delay : myDelays) {
            try {
                myRobot.keyPress(myCharacter);
                myQueue.put(CharEvent.TYPED);
                myRobot.keyRelease(myCharacter);

                sleep(delay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
