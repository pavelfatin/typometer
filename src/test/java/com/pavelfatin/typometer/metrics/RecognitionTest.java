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

import com.pavelfatin.typometer.Parallelized;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parallelized.class)
public class RecognitionTest {
    private static final Collection<Object[]> TESTS = Arrays.asList(
            // General
            test("underline"),

            // Size (Sublime Text 3083)
            test("size/08pt"),
            test("size/10pt"),
            test("size/12pt"),
            test("size/14pt"),
            test("size/16pt"),
            test("size/18pt"),

            // Smoothing (Sublime Text 3083)
            test("color/smoothing/all-hallows-eve"),
            test("color/smoothing/amy"),
            test("color/smoothing/blackboard"),
            test("color/smoothing/cobalt"),
            test("color/smoothing/dawn"),
            test("color/smoothing/eiffel"),
            test("color/smoothing/espresso-libre"),
            test("color/smoothing/idle"),
            test("color/smoothing/iplastic"),
            test("color/smoothing/lazy"),
            test("color/smoothing/mac-classic"),
            test("color/smoothing/monokai"),
            test("color/smoothing/monokai-bright"),
            test("color/smoothing/pastels-on-dark"),
            test("color/smoothing/slush-and-poppies"),
            test("color/smoothing/solarized-dark"),
            test("color/smoothing/solarized-light"),
            test("color/smoothing/spacecadet"),
            test("color/smoothing/sunburst"),
            test("color/smoothing/twilight"),
            test("color/smoothing/zenburnesque"),

            // Plain (Sublime Text 3083)
            test("color/plain/all-hallows-eve"),
            test("color/plain/amy"),
            test("color/plain/blackboard"),
            test("color/plain/cobalt"),
            test("color/plain/dawn"),
            test("color/plain/eiffel"),
            test("color/plain/espresso-libre"),
            test("color/plain/idle"),
            test("color/plain/iplastic"),
            test("color/plain/lazy"),
            test("color/plain/mac-classic"),
            test("color/plain/monokai"),
            test("color/plain/monokai-bright"),
            test("color/plain/pastels-on-dark"),
            test("color/plain/slush-and-poppies"),
            test("color/plain/solarized-dark"),
            test("color/plain/solarized-light"),
            test("color/plain/spacecadet"),
            test("color/plain/sunburst"),
            test("color/plain/twilight"),
            test("color/plain/zenburnesque")
    );

    private static final int PATTERN_LENGTH = 5;

    private String myPath;

    public RecognitionTest(String path) {
        myPath = path;
    }

    @Test
    public void detection() throws IOException {
        doTest(myPath);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getTests() {
        return TESTS;
    }

    private void doTest(String path) throws IOException {
        BufferedImage image = loadImage("/recognition/" + path + ".png");

        boolean invert = GreyscaleImage.isInverted(image);

        List<List<Rectangle>> sequences = sequencesIn(image, PATTERN_LENGTH, invert);

        if (sequences.isEmpty()) {
            sequences = sequencesIn(image, PATTERN_LENGTH, !invert);
        }

        Assert.assertEquals("Single sequence must be detected", 1, sequences.size());
    }

    private static List<List<Rectangle>> sequencesIn(BufferedImage image, int count, boolean invert) {
        GreyscaleImage greyscaleImage = GreyscaleImage.createFrom(image);
        Collection<Rectangle> areas = MetricsDetector.areasIn(greyscaleImage, invert, new Queue(32));
        return MetricsDetector.sequencesIn(areas, count);
    }

    private static BufferedImage loadImage(String path) throws IOException {
        BufferedImage image = ImageIO.read(RecognitionTest.class.getResource(path));
        Assert.assertNotNull("Image not found: " + path, image);
        return image;
    }

    private static Object[] test(String path) {
        return new Object[]{path};
    }
}
