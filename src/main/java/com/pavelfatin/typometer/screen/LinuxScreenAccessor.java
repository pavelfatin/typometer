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
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

class LinuxScreenAccessor implements ScreenAccessor {
    private static final int XYPixmap = 1;

    private final Pointer myDisplay;
    private final int myWindow;
    private final int myScreen;
    private final int myColormap;
    private final NativeLong myAllPlanes;
    private final XColor.ByReference myColor;

    LinuxScreenAccessor() {
        myDisplay = XLIB.XOpenDisplay(null);
        myScreen = XLIB.XDefaultScreen(myDisplay);
        myWindow = XLIB.XRootWindow(myDisplay, myScreen);
        myColormap = XLIB.XDefaultColormap(myDisplay, myScreen);
        myAllPlanes = XLIB.XAllPlanes();
        myColor = new XColor.ByReference();
    }

    @Override
    public void dispose() {
        XLIB.XCloseDisplay(myDisplay);
    }

    @Override
    public Color getPixelColor(int x, int y) {
        Pointer image = XLIB.XGetImage(myDisplay, myWindow, x, y, 1, 1, myAllPlanes, XYPixmap);

        try {
            myColor.pixel = XLIB.XGetPixel(image, 0, 0);

            XLIB.XQueryColor(myDisplay, myColormap, myColor);

            return new Color((myColor.red & 0xFFFF) >> 8, (myColor.green & 0xFFFF) >> 8, (myColor.blue & 0xFFFF) >> 8);
        } finally {
            XLIB.XFree(image);
        }
    }

    private static class XLIB {
        static {
            Native.register("X11");
        }

        static native Pointer XOpenDisplay(String display_name);

        static native void XCloseDisplay(Pointer display);

        static native int XDefaultScreen(Pointer display);

        static native int XRootWindow(Pointer display, int screen_number);

        static native NativeLong XAllPlanes();

        static native int XDefaultColormap(Pointer display, int screen_number);

        static native Pointer XGetImage(Pointer display, int d, int x, int y, int width, int height, NativeLong plane_mask, int format);

        static native NativeLong XGetPixel(Pointer ximage, int x, int y);

        static native void XQueryColor(Pointer display, int colormap, Structure color);

        static native void XFree(Pointer data);
    }

    public static class XColor extends Structure {
        static class ByReference extends XColor implements Structure.ByReference {};

        public NativeLong pixel;

        public short red, green, blue;

        public byte flags;

        public byte pad;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("pixel", "red", "green", "blue", "flags", "pad");
        }
    }
}
