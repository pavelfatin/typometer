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
