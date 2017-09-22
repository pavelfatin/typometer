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
import java.util.Optional;

import static java.lang.Math.round;

@RunWith(Parallelized.class)
public class MetricsTest {
    private static final Collection<Object[]> TESTS = Arrays.asList(
            // Lubuntu 14.04.3
            test("lubuntu-14.04.3/atom-1.0.19"),
            test("lubuntu-14.04.3/eclipse-4.5.1"),
            test("lubuntu-14.04.3/emacs-24.3.1", true),
            test("lubuntu-14.04.3/gvim-7.4.52"),
            test("lubuntu-14.04.3/idea-14.1.5/dark"),
            test("lubuntu-14.04.3/idea-14.1.5/light"),
            test("lubuntu-14.04.3/leafpad-0.8.18.1"),
            test("lubuntu-14.04.3/lxterminal-0.1.11", true),
            test("lubuntu-14.04.3/mc-4.8.11", true),
            test("lubuntu-14.04.3/nano-2.2.6", true),
            test("lubuntu-14.04.3/netbeans-8.0.2"),
            test("lubuntu-14.04.3/sublime-3083"),
            test("lubuntu-14.04.3/vim-7.4.52", true),

            // Windows 7 SP1 (cleartype)
            test("windows-7-sp1/cleartype/atom-1.0.19"),
            test("windows-7-sp1/cleartype/cmd-6.1.7601"),
            test("windows-7-sp1/cleartype/eclipse-4.5.1"),
            test("windows-7-sp1/cleartype/emacs-24.5.1", true),
            test("windows-7-sp1/cleartype/far-3.0.4242"),
            test("windows-7-sp1/cleartype/gvim-7.4.280"),
            test("windows-7-sp1/cleartype/idea-14.1.5/dark"),
            test("windows-7-sp1/cleartype/idea-14.1.5/light"),
            test("windows-7-sp1/cleartype/netbeans-8.0.2"),
            test("windows-7-sp1/cleartype/notepad-6.1.7601"),
            test("windows-7-sp1/cleartype/notepad-plus-6.8.4"),
            test("windows-7-sp1/cleartype/sublime-3083"),

            // Windows 7 SP1 (plain)
            test("windows-7-sp1/plain/atom-1.0.19"),
            test("windows-7-sp1/plain/cmd-6.1.7601"),
            test("windows-7-sp1/plain/eclipse-4.5.1"),
            test("windows-7-sp1/plain/emacs-24.5.1", true),
            test("windows-7-sp1/plain/far-3.0.4242"),
            test("windows-7-sp1/plain/gvim-7.4.280"),
            test("windows-7-sp1/plain/idea-14.1.5/dark"),
            test("windows-7-sp1/plain/idea-14.1.5/light"),
            test("windows-7-sp1/plain/netbeans-8.0.2"),
            test("windows-7-sp1/plain/notepad-6.1.7601"),
            test("windows-7-sp1/plain/notepad-plus-6.8.4"),
            test("windows-7-sp1/plain/sublime-3083")
    );

    private static final int PATTERN_LENGTH = 5;
    private static final int SEQUENCE_LENGTH = 70;
    private static final int MIN_STEP_LENGTH = 3;

    private String myPath;
    private boolean myBlockCursor;

    public MetricsTest(String path, boolean blockCursor) {
        myPath = path;
        myBlockCursor = blockCursor;
    }

    @Test
    public void detection() throws IOException {
        doTest(myPath, myBlockCursor);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getTests() {
        return TESTS;
    }

    private void doTest(String path, boolean blockCursor) throws IOException {
        BufferedImage image1 = loadImage("/metrics/" + path + "/1.png");
        BufferedImage image2 = loadImage("/metrics/" + path + "/2.png");
        BufferedImage image3 = loadImage("/metrics/" + path + "/3.png");

        Optional<Metrics> optionalMetrics = Metrics.detect(image1, image2, PATTERN_LENGTH);

        Assert.assertTrue("Metrics must be detected", optionalMetrics.isPresent());

        Metrics metrics = optionalMetrics.get();

        Assert.assertEquals("Cursor type must be detected correctly", blockCursor, metrics.isBlockCursor());

        Assert.assertTrue("Step must be greater than " + MIN_STEP_LENGTH + ": " + metrics.getStep(),
                metrics.getStep() > MIN_STEP_LENGTH);

        Assert.assertTrue("Line length must be greater than " + SEQUENCE_LENGTH + ": " + metrics.getLineLength(),
                metrics.getLineLength() > SEQUENCE_LENGTH);

        if (blockCursor) {
            assertPointsPresent(true, image1, metrics, 0, 1);
        }

        int cursorLength = metrics.getCursorLength();

        assertPointsPresent(true, image2, metrics, 0, PATTERN_LENGTH + cursorLength);
        assertPointsPresent(false, image2, metrics, PATTERN_LENGTH + cursorLength, SEQUENCE_LENGTH);

        assertPointsPresent(true, image3, metrics, 0, SEQUENCE_LENGTH + cursorLength);
    }

    private static BufferedImage loadImage(String path) throws IOException {
        BufferedImage image = ImageIO.read(MetricsTest.class.getResource(path));
        Assert.assertNotNull("Image not found: " + path, image);
        return image;
    }

    private static void assertPointsPresent(boolean expectation, BufferedImage image, Metrics metrics, int begin, int end) {
        Point p = metrics.getStartingPoint();

        double x = p.x + metrics.getStep() * begin;

        for (int i = begin; i < end; i++) {
            int ix = (int) round(x);

            Color color = new Color(image.getRGB(ix, p.y));

            if (expectation) {
                String message = String.format("Point %d must be present at (%d, %d)", i + 1, ix, p.y);
                Assert.assertNotEquals(message, metrics.getBackground(), color);
            } else {
                String message = String.format("Point %d must not be present at (%d, %d)", i + 1, ix, p.y);
                Assert.assertEquals(message, metrics.getBackground(), color);
            }

            x += metrics.getStep();
        }
    }

    private static Object[] test(String path) {
        return new Object[]{path, false};
    }

    private static Object[] test(String path, boolean blockCursor) {
        return new Object[]{path, blockCursor};
    }
}
