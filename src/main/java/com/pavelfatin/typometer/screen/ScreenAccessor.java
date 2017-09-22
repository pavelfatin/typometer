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
