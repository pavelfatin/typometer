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

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.awt.*;

class WindowsScreenAccessor implements ScreenAccessor {
    private final Pointer myDc;

    WindowsScreenAccessor() {
        myDc = GDI32.CreateDCA("DISPLAY", null, null, Pointer.NULL);
    }

    @Override
    public void dispose() {
        GDI32.DeleteDC(myDc);
    }

    @Override
    public Color getPixelColor(int x, int y) {
        int value = GDI32.GetPixel(myDc, x, y);
        return new Color(value & 0xFF, value >> 8 & 0xFF, value >> 16 & 0xFF);
    }

    private static class GDI32 {
        static {
            Native.register("gdi32");
        }

        static native Pointer CreateDCA(String lpszDriver, String lpszDevice, String lpszOutput, Pointer lpInitData);

        static native boolean DeleteDC(Pointer hDC);

        static native int GetPixel(Pointer hdc, int nXPos, int nYPos);
    }
}
