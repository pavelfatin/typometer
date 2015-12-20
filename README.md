# Typometer README

Typometer is a tool to measure and analyze visual latency of text / code editors.

Editor latency is delay between an input event and a corresponding screen update, in particular case – delay between keystroke and character appearance. While there are many kinds of delays (caret movement, line editing, etc.), typing latency is a major predictor of editor usability.

Check my article [Typing with pleasure](https://pavelfatin.com/typing-with-pleasure) to learn more about editor latency and its effects on typing performance.

Download: [typometer-1.0-bin.zip](https://github.com/pavelfatin/typometer/releases/download/v1.0.0/typometer-1.0-bin.zip) (0.5 MB)

Java 8 or latter is required to run the program. You can [download Java](https://java.com/download) from the official site.

## Features

* Cross-platform (Windows, Mac, Linux).
* Native API calls for faster screen access.
* Synchronous / asynchronous modes.
* Import / export of CSV data.
* Summary statistics, frequency distribution.
* Line / bar charts (including comparative ones).
* Chart image export (with legend).

## Principle

The program generates OS input events (key presses) and uses screen capture to fully automate the test process.

At first, a predefined pattern ("``.....``") is inserted in editor window in order to detect screen metrics (start position, step, background, etc.).

After that, the program types a predefined number of "``.``" characters into the editor (with given periodicity), measuring delays between key presses and corresponding character drawings.

To achieve high accuracy of measurement, only a single pixel is queried for each symbol. Moreover, the program can use fast native API ([WinAPI](https://en.wikipedia.org/wiki/Windows_API), [XLib](https://en.wikipedia.org/wiki/Xlib)) calls on supported platforms, offering [AWT robot](http://docs.oracle.com/javase/8/docs/api/java/awt/Robot.html) as a fallback option.

There are two modes of testing available:

* **Synchronous** – the program always waits for typed character to appear before making a pause and typing the next character. It's the most accurate method (because there's no threading overhead).
* **Asynchronous** – the program types and recognizes characters independently. This method is slightly less accurate, but it's useful for testing rapid typing, when editor drawing might easily lag by multiple characters.

## Usage

To register only essential editor latency, text must be rendered directly to [framebuffer](https://en.wikipedia.org/wiki/Framebuffer), without intermediate image processing that might introduce additional delay. Prefer [stacking window managers](https://en.wikipedia.org/wiki/Stacking_window_manager) to [compositing window managers](https://en.wikipedia.org/wiki/Compositing_window_manager) for the testing purposes, particularly:

* Switch to Classic theme in Windows. [Windows Aero](https://en.wikipedia.org/wiki/Windows_Aero) enforces internal [vertical synchronization](https://en.wikipedia.org/wiki/Analog_television#Vertical_synchronization), which leads to minimum 1 frame lag (about 17 ms for 60 Hz monitor refresh rate) and delay discretization.
* Use Linux distributive with lightweight [window manager](https://en.wikipedia.org/wiki/Window_manager), like [Lubuntu](http://lubuntu.net/) ([Openbox](https://en.wikipedia.org/wiki/Openbox)). Complex, 3D-based windows managers might substantially increase system rendering latency, for example, on my hardware, Ubuntu's [Compiz](https://en.wikipedia.org/wiki/Compiz), adds ~10 ms unavoidable lag.

You may consider switching your machine in a particular hardware mode (power scheme, integrated / discrete graphics, etc.). In power save mode (and on battery), for example, editor responsiveness is usually much lower, so it's possible to detect significant performance glitches which are less frequently observable otherwise.

Before you start benchmarking, make sure that other applications are not placing noticeable load on your system. It's up to you whether to "warm up" [VM](https://en.wikipedia.org/wiki/Virtual_machine#Process_virtual_machines)-based editors, so they can pre-compile performance-critical parts of their code before proceeding.

If possible, enable non-block caret (i. e. underline / vertical bar instead of rectangle) in editor. This might increase measurement accuracy.

Typical action sequence is the following:

1. Specify a measurement title, like "HTML in Vim" *(optional, can be set later)*.
2. Configure test parameters *(optional)*.
3. Launch an editor, maximize its window.
4. Open some data in the editor, for instance, a large HTML file *(optional)*.
5. Place editor caret in desired context (like comment, etc.), at the end of short / empty line.
6. Start benchmarking process in the program.
7. After a corresponding prompt, transfer focus to the editor window.
8. Wait for test completion, don't interfere with the process.

You can always interrupt the testing process simply by transferring focus back to the program window.

After test result is acquired, you may either analyze the singular data by itself or perform additional tests (different editors / conditions) to do comparative analysis.

Both source- and aggregate data is easily accessible, you can:

* copy table content  as text,
* save chart to [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) file (with legend and summary stats),
* export raw data in [CSV](https://en.wikipedia.org/wiki/Comma-separated_values) format (for [Calc](https://en.wikipedia.org/wiki/LibreOffice_Calc) or [R](https://www.r-project.org/), if you fancy).

It's possible to merge results either by inserting data from an existing CSV file, or by appending data to a CSV file on saving.

## Troubleshooting

To make benchmarking possible, correct screen metrics must be detected at the initial step. The program attempts to recognize a custom pattern (5 new dots) in order to determine the following parameters:

* starting position,
* horizontal step,
* background color,
* line length,
* caret type.

Because there are many editors (and multiple versions of each editor), which looks different on different platforms, and there are many possible color schemes and fonts, the metrics recognition algorithm has to be very flexible. While the program sources contain a great deal of test cases, some glitches are still probable.

Here's a list of typical problems and corresponding solutions:

* Editor background is non-uniform (gradient, picture) – set solid color background.
* Characters are too low-contrast and obscure – use a crisp color scheme.
* Dot characters merge with the caret – increase font size.
* Editor replaces multiple dots with ellipsis – disable that auto-correction.
* Spaces between dots are uneven – use [monospaced font](https://en.wikipedia.org/wiki/Monospaced_font) in the editor.
* Editor has a left panel that melds with the text area – hide the panel.

Feel free to contribute by creating additional test case images (check `/src/test/resources` directory for examples).

Pavel Fatin, [https://pavelfatin.com](https://pavelfatin.com/)
