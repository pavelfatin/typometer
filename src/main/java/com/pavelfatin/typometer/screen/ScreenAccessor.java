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

package com.pavelfatin.typometer.screen;

import com.sun.jna.Platform;

import java.awt.*;

public interface ScreenAccessor {
    Color getPixelColor(int x, int y);

    void dispose();

    static boolean isNativeApiSupported() {
        return Platform.isWindows() || Platform.isLinux();
    }

    static ScreenAccessor create(boolean isNative) {
        if (isNative) {
            if (Platform.isWindows()) {
                return new WindowsScreenAccessor();
            }
            if (Platform.isLinux()) {
                return new LinuxScreenAccessor();
            }
        }
        return new AwtRobotScreenAccessor();
    }
}
