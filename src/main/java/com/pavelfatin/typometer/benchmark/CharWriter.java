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
