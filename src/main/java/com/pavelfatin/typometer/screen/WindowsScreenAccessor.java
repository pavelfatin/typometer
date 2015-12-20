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
