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

import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class HeapTest {
    @Test
    public void allocation() throws IOException {
        GreyscaleImage image = loadImage("/metrics/windows-7-sp1/cleartype/idea-14.1.5/dark/1.png");
        Queue queue = new Queue(8);

        MetricsDetector.areasIn(image, false, queue);
        MetricsDetector.areasIn(image, true, queue);

        Assert.assertEquals(256, queue.heapSpace());
    }

    private static GreyscaleImage loadImage(String path) throws IOException {
        BufferedImage image = ImageIO.read(HeapTest.class.getResource(path));
        Assert.assertNotNull("Image not found: " + path, image);
        return GreyscaleImage.createFrom(image);
    }
}
