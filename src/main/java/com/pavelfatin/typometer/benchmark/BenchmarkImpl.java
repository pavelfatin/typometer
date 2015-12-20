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

import com.pavelfatin.typometer.ExceptionHandler;
import com.pavelfatin.typometer.metrics.Metrics;
import com.pavelfatin.typometer.screen.ScreenAccessor;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.pavelfatin.typometer.benchmark.ChangeDetector.waitForChange;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.lang.Thread.sleep;

class BenchmarkImpl implements Benchmark {
    private static final int PATTERN_LENGTH = 5;
    private static final int MIN_LINE_LENGTH = 15;

    private static final int PATTERN_INSERTION_DELAY = 300;
    private static final int DELETION_DELAY = 200;

    private static final int CHARACTER = KeyEvent.VK_PERIOD;

    private static final Robot ourRobot;

    private ExecutorService myExecutor = Executors.newFixedThreadPool(2);

    static {
        try {
            ourRobot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(Parameters parameters, boolean async, ScreenAccessor accessor, BenchmarkListener listener) {
        try {
            benchmark(myExecutor, ourRobot, accessor, parameters, async, listener);
        } catch (BenchmarkException e) {
            listener.onError(e.getMessage());
        } catch (InterruptedException e) {
            listener.onError("Process canceled by user.");
        }
    }

    @Override
    public void dispose() {
        myExecutor.shutdown();

        try {
            myExecutor.awaitTermination(7, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void benchmark(ExecutorService executor, Robot robot, ScreenAccessor accessor,
                           Parameters parameters, boolean async, BenchmarkListener listener)
            throws BenchmarkException, InterruptedException {

        listener.onStart(parameters);

        Metrics metrics = detectScreenMetrics(robot, accessor, listener::onPhase);

        List<Integer> delays = parameters.getDelays();

        int total = parameters.getCount();

        int offset = 0;

        while (true) {
            int count = min(total - offset, metrics.getLineLength());

            List<Integer> lineDelays = delays.subList(offset, offset + count);

            PrimitiveIterator.OfInt indices = IntStream.range(offset, offset + count).iterator();

            Consumer<Double> consumer = result -> {
                listener.onPhase(String.format("Character %d / %d...", indices.next() + 1, total));
                listener.onResult(result);
            };

            if (async) {
                typeAsync(executor, robot, accessor, metrics, lineDelays, consumer);
            } else {
                typeSync(executor, robot, accessor, metrics, lineDelays, consumer);
            }

            delete(robot, accessor, metrics.getEffectiveStartingPoint(), count);

            offset += count;

            if (offset == total) {
                break;
            }
        }

        listener.onPhase("Done.");

        listener.onFinish();
    }

    private static Metrics detectScreenMetrics(Robot robot, ScreenAccessor accessor, Consumer<String> listener)
            throws BenchmarkException, InterruptedException {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        listener.accept("Inserting a reference pattern...");
        BufferedImage imageBefore = createScreenCapture(robot, screenSize);
        type(robot, CHARACTER, PATTERN_LENGTH);
        sleep(PATTERN_INSERTION_DELAY);

        listener.accept("Detecting screen metrics...");
        BufferedImage imageAfter = createScreenCapture(robot, screenSize);
        Optional<Metrics> metricsOption = Metrics.detect(imageBefore, imageAfter, PATTERN_LENGTH);

        if (!metricsOption.isPresent()) {
            type(robot, KeyEvent.VK_BACK_SPACE, PATTERN_LENGTH);
            throw new BenchmarkException("Cannot detect the reference pattern.");
        }

        Metrics metrics = metricsOption.get();

        if (metrics.getLineLength() < MIN_LINE_LENGTH) {
            type(robot, KeyEvent.VK_BACK_SPACE, PATTERN_LENGTH);
            throw new BenchmarkException("Available line length is too short.");
        }

        listener.accept("Deleting the pattern...");
        delete(robot, accessor, metrics.getEffectiveStartingPoint(), PATTERN_LENGTH);

        return metrics;
    }

    private static void delete(Robot robot, ScreenAccessor accessor, Point point, int count)
            throws BenchmarkException, InterruptedException {

        Color previousColor = robot.getPixelColor(point.x, point.y);

        type(robot, KeyEvent.VK_BACK_SPACE, count);
        waitForChange(accessor, point.x, point.y, previousColor);

        sleep(DELETION_DELAY);
    }

    private static void typeSync(ExecutorService executor, Robot robot, ScreenAccessor accessor,
                                 Metrics metrics, List<Integer> delays, Consumer<Double> consumer)
            throws BenchmarkException, InterruptedException {

        Point point = metrics.getEffectiveStartingPoint();

        double x = point.x;
        int y = point.y;

        for (int delay : delays) {
            if (!accessor.getPixelColor((int) round(x), y).equals(metrics.getBackground())) {
                throw new BenchmarkException("Previously undetected block cursor found.");
            }

            robot.keyPress(CHARACTER);
            long before = System.nanoTime();

            Future keyReleaseTask = executor.submit(() -> robot.keyRelease(CHARACTER));

            ChangeDetector.waitForChange(accessor, (int) round(x), y, metrics.getBackground());
            consumer.accept((System.nanoTime() - before) / 1000_000.0D);

            try {
                keyReleaseTask.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            sleep(delay);

            x += metrics.getStep();
        }
    }

    private static void typeAsync(ExecutorService executor, Robot robot, ScreenAccessor accessor, Metrics metrics,
                                  List<Integer> delays, Consumer<Double> consumer)
            throws BenchmarkException, InterruptedException {

        int count = delays.size();

        BlockingQueue<CharEvent> events = new ArrayBlockingQueue<>(2 * count);
        Queue<Long> typeMoments = new ArrayDeque<>(count);

        CharReader charReader = new CharReader(accessor, metrics, count, events);
        CharWriter charWriter = new CharWriter(robot, CHARACTER, delays, events);

        Future readerTask = executor.submit(ExceptionHandler.wrap(charReader));
        Future writerTask = executor.submit(ExceptionHandler.wrap(charWriter));

        boolean ignoreNextTypingEvent = false;

        try {
            for (int i = 0; i < 2 * count; i++) {
                switch (events.take()) {
                    case TYPED:
                        if (ignoreNextTypingEvent) {
                            ignoreNextTypingEvent = false;
                        } else {
                            typeMoments.add(System.nanoTime());
                        }
                        break;
                    case RECOGNIZED:
                        // Such a case is possible, because we receive TYPED events _after_ corresponding key presses.
                        if (typeMoments.isEmpty()) {
                            ignoreNextTypingEvent = true;
                            consumer.accept(0.0D);
                        } else {
                            double result = (System.nanoTime() - typeMoments.poll()) / 1000_000.0D;
                            consumer.accept(result);
                        }
                        break;
                    case TIMEOUT:
                        writerTask.cancel(true);
                        throw new BenchmarkException("Cannot detect typed char, timeout expired.");
                }
            }
        } finally {
            writerTask.cancel(true);
            readerTask.cancel(true);
        }
    }

    private static BufferedImage createScreenCapture(Robot robot, Dimension screenSize) {
        return robot.createScreenCapture(new Rectangle(screenSize));
    }

    private static void type(Robot robot, int character, int count) {
        IntStream.range(0, count).forEach(i -> type(robot, character));
    }

    private static void type(Robot robot, int character) {
        robot.keyPress(character);
        robot.keyRelease(character);
    }
}
