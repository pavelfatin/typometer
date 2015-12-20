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

package com.pavelfatin.typometer.ui;

import com.pavelfatin.typometer.ExceptionHandler;
import com.pavelfatin.typometer.benchmark.Benchmark;
import com.pavelfatin.typometer.benchmark.BenchmarkListener;
import com.pavelfatin.typometer.benchmark.Parameters;
import com.pavelfatin.typometer.chart.ChartData;
import com.pavelfatin.typometer.chart.ChartPanel;
import com.pavelfatin.typometer.chart.ChartType;
import com.pavelfatin.typometer.chart.Series;
import com.pavelfatin.typometer.data.Data;
import com.pavelfatin.typometer.data.Measurement;
import com.pavelfatin.typometer.screen.ScreenAccessor;
import com.pavelfatin.typometer.statistics.FrequencyDistribution;
import com.pavelfatin.typometer.statistics.Statistics;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class MainFrame extends JFrame {
    private static final Collection<Color> PALETTE_COLORS = asList(
            brighter(UIManager.getColor("textHighlight")),
            brighter(new Color(105, 138, 57)),
            brighter(new Color(138, 57, 105)));

    private static final Color DEFAULT_COLOR = Color.GRAY;

    private static final int INIT_DELAY = 500;

    private static final int CHART_MAX_SPAN_COUNT = 10;

    private static final String APPEND = "Append";
    private static final String REPLACE = "Replace";
    private static final String OVERWRITE = "Overwrite";
    private static final String CANCEL = "Cancel";

    public static final String FILE_EXTENSION = "csv";
    public static final String FILE_DESCRIPTION = "CSV files";

    private static final FileNameExtensionFilter CSV_FILE_FILTER =
            new FileNameExtensionFilter(FILE_DESCRIPTION, FILE_EXTENSION);

    private static final Dimension PREFERRED_SIZE = new Dimension(460, 570);

    private static Optional<File> myCurrentDirectory = Optional.empty();

    static {
        // Workaround for https://bugs.openjdk.java.net/browse/JDK-8134828
        UIManager.put("ScrollBar.minimumThumbSize", new Dimension(32, 32));
    }

    private MutableTable myTable;
    private JButton myBenchmarkButton;
    private JSpinner myCount;
    private JSpinner myPauseLength;
    private JSpinner myDelay;
    private JCheckBox myPausesEnabled;
    private JSpinner myPausePeriod;
    private JPanel myComponent;
    private JTextField myTitle;
    private ChartPanel myChartPanel;
    private JButton myResetButton;
    private JPanel myCentralPanel;
    private JToolBar myToolBar;
    private JCheckBox myAsync;
    private JCheckBox myNative;
    private JRadioButton myDistributionButton;
    private JRadioButton mySeriesButton;
    private ButtonGroup myButtonGroup = new ButtonGroup();

    private String myDefaultTitle;
    private Parameters myDefaultParameters;

    private ExecutorService myExecutor = Executors.newSingleThreadExecutor();
    private Benchmark myBenchmark = Benchmark.create();
    private Optional<Future> myTask = Optional.empty();

    private Actions myActions = new Actions(this);

    private Data myData = new Data();
    private MeasurementsTableModel myTableModel = new MeasurementsTableModel();

    private ColoredRenderer myOrdinalRenderer = new OrdinalRenderer();
    private TableCellRenderer myValueRenderer = new DoubleValueRenderer("%.1f");

    public MainFrame(String title, Parameters parameters) {
        super("Typometer – Editor latency analyzer");

        myDefaultParameters = parameters;
        myDefaultTitle = title;

        setAlwaysOnTop(true);
        setPreferredSize(PREFERRED_SIZE);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        initComponents();
        initParameterBounds();

        myNative.setEnabled(ScreenAccessor.isNativeApiSupported());
        onReset();

        myButtonGroup.add(mySeriesButton);
        myButtonGroup.add(myDistributionButton);

        updateActionStates(Collections.emptyList());
        initListeners();

        getContentPane().add(myComponent);
        getRootPane().setDefaultButton(myBenchmarkButton);
    }

    private void initComponents() {
        myActions.populate(myToolBar);
        myActions.register(getRootPane(), myTable);

        JLabel notice = new JLabel("select multiple items to compare");
        notice.setForeground(Color.DARK_GRAY);

        myToolBar.add(Box.createHorizontalGlue());
        myToolBar.add(notice);
        myToolBar.add(Box.createHorizontalStrut(4));

        myTable.setModel(myTableModel);
        myTable.setDefaultRenderer(Void.class, myOrdinalRenderer);
        myTable.setDefaultRenderer(Double.class, myValueRenderer);
        myTable.putClientProperty("JTable.autoStartsEdit", false);
        myTable.putClientProperty("terminateEditOnFocusLost", true);

        TableColumnModel columnModel = myTable.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(columnWidthFor("##") + 1);
        columnModel.getColumn(2).setPreferredWidth(40);
        columnModel.getColumn(3).setPreferredWidth(40);
        columnModel.getColumn(4).setPreferredWidth(40);
        columnModel.getColumn(5).setPreferredWidth(40);

        myChartPanel.setBorder(UIManager.getBorder("ScrollPane.border"));

        myTable.setModel(myTableModel);
    }

    private static int columnWidthFor(String s) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setText(s);
        return renderer.getPreferredSize().width;
    }

    private void initParameterBounds() {
        myCount.setModel(new SpinnerNumberModel(100, 100, 9900, 100));
        myDelay.setModel(new SpinnerNumberModel(0, 0, 9990, 10));
        myPausesEnabled.setSelected(false);
        myPausePeriod.setModel(new SpinnerNumberModel(10, 10, 9900, 10));
        myPauseLength.setModel(new SpinnerNumberModel(0, 0, 9900, 100));
    }

    private void setDefaultParameters() {
        myTitle.setText(myDefaultTitle);

        myCount.setValue(myDefaultParameters.getCount());
        myDelay.setValue(myDefaultParameters.getDelay());
        myPausesEnabled.setSelected(myDefaultParameters.isPausesEnabled());
        myPausePeriod.setValue(myDefaultParameters.getPausePeriod());
        myPauseLength.setValue(myDefaultParameters.getPauseLength());

        myNative.setSelected(ScreenAccessor.isNativeApiSupported());
        myAsync.setSelected(false);
    }

    private void initListeners() {
        myResetButton.addActionListener(e -> onReset());

        myBenchmarkButton.addActionListener(e -> onBenchmark());

        myTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 1) {
                onTitleEdited(e.getFirstRow(), e.getColumn());
            }
        });

        myTable.getSelectionModel().addListSelectionListener(e -> onSelectionChanged(myTable.getSelectedRowIndices()));

        mySeriesButton.addItemListener(e -> updateChart(myTable.getSelectedRowIndices()));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                onWindowClosed();
            }
        });
    }

    private static Collection<Color> createColorSequence(int count, Direction direction) {
        Collection<Color> result = new ArrayList<>();

        int paletteSize = PALETTE_COLORS.size();
        int paletteColorCount = min(count, paletteSize);

        Collection<Color> defaultColors = IntStream.range(0, count - paletteColorCount)
                .mapToObj(i -> DEFAULT_COLOR).collect(toList());

        List<Color> colorSequence = new ArrayList<>(PALETTE_COLORS);

        if (direction == Direction.UP) {
            Collections.reverse(colorSequence);
            result.addAll(defaultColors);
            result.addAll(colorSequence.subList(paletteSize - paletteColorCount, paletteSize));
        } else {
            result.addAll(colorSequence.subList(0, paletteColorCount));
            result.addAll(defaultColors);
        }

        return result;
    }

    private static Color brighter(Color color) {
        double k = 1.3D;
        return new Color((int) (color.getRed() * k), (int) (color.getGreen() * k), (int) (color.getBlue() * k));
    }

    private void onReset() {
        setDefaultParameters();
    }

    void onNew() {
        if (myTable.getRowCount() > 0) {
            int choice = JOptionPane.showConfirmDialog(this, "Clean existing data in the table?",
                    "New", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                myTableModel.clear();
                myData.clear();
            }
        }
    }

    void onImport() {
        JFileChooser chooser = createFileChooser(JFileChooser.OPEN_DIALOG, "Import data");

        if (chooser.showDialog(this, null) == JFileChooser.APPROVE_OPTION) {
            if (myTable.getRowCount() > 0) {
                Object choice = showInputDialog("Import data", "Existing data is already present in the table.",
                        new String[]{APPEND, REPLACE, CANCEL});

                if (!APPEND.equals(choice) && !REPLACE.equals(choice)) {
                    return;
                }

                if (REPLACE.equals(choice)) {
                    myTableModel.clear();
                    myData.clear();
                }
            }

            try {
                doImport(Data.load(chooser.getSelectedFile()));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot read file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Object showInputDialog(String title, String message, String[] options) {
        JOptionPane pane = new JOptionPane(message + "\n\nPlease choose an action:");
        pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        pane.setOptionType(JOptionPane.YES_NO_CANCEL_OPTION);
        pane.setOptions(options);
        JDialog dialog = pane.createDialog(this, title);
        dialog.pack();
        dialog.setVisible(true);
        return pane.getValue();
    }

    private void doImport(Data data) {
        int previousCount = myTable.getRowCount();

        myData.add(data);
        myTableModel.addRows(data.getMeasurements());

        int count = myTable.getRowCount();

        if (count > 0) {
            int index = previousCount > 0 && count > 0 ? previousCount : 0;
            myTable.setSelectedRowIndex(index);
            myTable.scrollToRowVisible(index);
        }

        myTable.requestFocusInWindow();
    }

    void onExport() {
        JFileChooser chooser = createFileChooser(JFileChooser.SAVE_DIALOG, "Export data");
        chooser.setSelectedFile(new File("results"));

        if (chooser.showDialog(this, null) == JFileChooser.APPROVE_OPTION) {
            File file = withExtension(chooser.getSelectedFile(), FILE_EXTENSION);

            Data dataToExport = myData;

            if (file.exists()) {
                Object choice = showInputDialog("Export data", String.format("The file '%s' already exists.", file.getName()),
                        new String[]{APPEND, OVERWRITE, CANCEL});

                if (choice == null || CANCEL.equals(choice)) {
                    return;
                }

                if (APPEND.equals(choice)) {
                    try {
                        dataToExport = Data.load(file);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot read file", JOptionPane.ERROR_MESSAGE);
                    }

                    dataToExport.add(myData);
                }
            }

            try {
                dataToExport.save(file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot write file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void onCopy() {
        String text = myTableModel.format(myTable.getSelectedRowIndices());

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    void onUp() {
        myTable.moveSelectedRowsUp(myData::moveUp);
    }

    void onDown() {
        myTable.moveSelectedRowsDown(myData::moveDown);
    }

    void onEdit() {
        myTable.editSelectedRow(1);
    }

    void onDelete() {
        myTable.deleteSelectedRows(myData::remove);
    }

    private void onBenchmark() {
        ProgressDialog dialog = new ProgressDialog(this, "Benchmarking...");

        dialog.addWindowFocusListener(new WindowFocusListener() {
            private boolean myStarted = false;

            @Override
            public void windowGainedFocus(WindowEvent e) {
                cancelBenchmarkTask();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                if (dialog.isVisible() && !myStarted) {
                    dialog.setHeader("Starting the benchmark...");
                    sleep(INIT_DELAY);
                    doBenchmark(dialog);
                    myStarted = true;
                }
            }
        });

        dialog.setHeader("Please make the editor active. Waiting...");

        dialog.pack();
        dialog.setLocationRelativeTo(getCentralPanel());
        dialog.setVisible(true);

        cancelBenchmarkTask();
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    private void doBenchmark(ProgressDialog dialog) {
        final Parameters parameters = getParameters();
        final ScreenAccessor accessor = ScreenAccessor.create(myNative.isSelected());
        final boolean async = myAsync.isSelected();
        final BenchmarkListener listener = new BenchmarkListenerDecorator(new MyBenchmarkListener(this, dialog));

        Future task = myExecutor.submit(ExceptionHandler.wrap(() -> {
            try {
                myBenchmark.run(parameters, async, accessor, listener);
            } finally {
                SwingUtilities.invokeLater(accessor::dispose);
            }
        }));

        myTask = Optional.of(task);
    }

    void cancelBenchmarkTask() {
        myTask.ifPresent(it -> it.cancel(true));
    }

    private void onTitleEdited(int row, int column) {
        String title = (String) myTableModel.getValueAt(row, column);
        myData.setTitle(row, title);
    }

    private void onSelectionChanged(Collection<Integer> indices) {
        if (indices.size() > 1) {
            Map<Integer, Color> colorMap = zip(indices, createColorSequence(indices.size(), myTable.getSelectionDirection()));
            myOrdinalRenderer.setColors(colorMap);
            myTable.repaint();
        } else {
            myOrdinalRenderer.setColors(Collections.emptyMap());
        }

        updateActionStates(indices);

        updateChart(indices);
    }

    private void updateChart(Collection<Integer> indices) {
        if (!indices.isEmpty()) {
            Collection<Measurement> measurements = indices.stream().limit(PALETTE_COLORS.size())
                    .map(myData::get).collect(toList());

            Collection<Color> colors = createColorSequence(measurements.size(), myTable.getSelectionDirection());

            Collection<String> titles = indices.stream().limit(PALETTE_COLORS.size())
                    .map(myTableModel::format).collect(toList());

            ChartType chartType = getChartType();

            ChartData data = chartType == ChartType.Line
                    ? createLineChartData(measurements, titles, colors)
                    : createBarChartData(measurements, titles, colors);

            myChartPanel.setData(chartType, Optional.of(data));
        } else {
            myChartPanel.setData(getChartType(), Optional.empty());
        }
    }

    private ChartType getChartType() {
        return mySeriesButton.isSelected() ? ChartType.Line : ChartType.Bar;
    }

    private void updateActionStates(Collection<Integer> indices) {
        myActions.setNewEnabled(myTable.getRowCount() > 0);

        myActions.setCopyEnabled(!indices.isEmpty());

        myActions.setMoveUpEnabled(myTable.canMoveSelectedRowsUp());
        myActions.setMoveDownEnabled(myTable.canMoveSelectedRowsDown());

        myActions.setEditEnabled(indices.size() == 1);
        myActions.setDeleteEnabled(!indices.isEmpty());
    }

    private static Map<Integer, Color> zip(Collection<Integer> indices, Collection<Color> colors) {
        Iterator<Color> colorIterator = colors.iterator();

        Map<Integer, Color> map = new HashMap<>();
        for (int index : indices) {
            Color color = colorIterator.next();
            map.put(index, color);
        }

        return map;
    }

    private static ChartData createLineChartData(Collection<Measurement> measurements, Collection<String> titles, Collection<Color> colors) {
        int maxLength = measurements.stream().mapToInt(Measurement::getLength).reduce(0, Math::max);

        int spanCount = min(CHART_MAX_SPAN_COUNT, maxLength);

        double step = (double) maxLength / spanCount;
        Collection<String> categories = DoubleStream.iterate(0.0D, v -> v + step).limit(spanCount + 1)
                .mapToObj(v -> Integer.toString((int) Math.round(v))).collect(toList());

        Iterator<String> titleIterator = titles.iterator();
        Iterator<Color> colorIterator = colors.iterator();

        Collection<Series> series = measurements.stream()
                .map(it -> new Series(titleIterator.next(), colorIterator.next(), it.getDelays())).collect(toList());

        return new ChartData(categories, series);
    }

    private static ChartData createBarChartData(Collection<Measurement> measurements, Collection<String> titles, Collection<Color> colors) {
        Collection<Collection<Double>> delays = measurements.stream().map(Measurement::getDelays).collect(toList());

        FrequencyDistribution distribution = FrequencyDistribution.compute(delays, CHART_MAX_SPAN_COUNT);

        Collection<String> categories = distribution.getRanges().stream()
                .map(range -> String.format("%.1f", range.getCenter())).collect(toList());

        Iterator<String> titleIterator = titles.iterator();
        Iterator<Color> colorIterator = colors.iterator();

        Collection<Series> series = distribution.getFrequencies().stream()
                .map(values -> new Series(titleIterator.next(), colorIterator.next(), values)).collect(toList());

        return new ChartData(categories, series);
    }

    private void onWindowClosed() {
        myExecutor.shutdown();

        try {
            myExecutor.awaitTermination(7, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        myBenchmark.dispose();
    }

    private static File withExtension(File file, String extension) {
        String suffix = "." + extension;
        String path = file.getPath();
        return path.endsWith(suffix) ? file : new File(path + suffix);
    }

    private static JFileChooser createFileChooser(int type, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(type);
        chooser.setDialogTitle(title);
        chooser.addChoosableFileFilter(CSV_FILE_FILTER);
        chooser.setFileFilter(CSV_FILE_FILTER);
        chooser.addActionListener(action -> {
            if (JFileChooser.APPROVE_SELECTION.equals(action.getActionCommand())) {
                myCurrentDirectory = Optional.of(chooser.getCurrentDirectory());
            }
        });
        myCurrentDirectory.ifPresent(chooser::setCurrentDirectory);
        return chooser;
    }

    public void onResults(Collection<Double> delays, Statistics stats) {
        Measurement measurement = new Measurement(myTitle.getText(), delays);
        myData.add(measurement);
        myTableModel.addRow(measurement, stats);
        myTable.selectLastRow(true);
        requestFocus();
    }

    JPanel getCentralPanel() {
        return myCentralPanel;
    }

    private Parameters getParameters() {
        return new Parameters(
                (Integer) myCount.getValue(),
                (Integer) myDelay.getValue(),
                myPausesEnabled.isSelected(),
                (Integer) myPausePeriod.getValue(),
                (Integer) myPauseLength.getValue());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        myComponent = new JPanel();
        myComponent.setLayout(new BorderLayout(0, 10));
        myComponent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 10));
        myComponent.add(panel1, BorderLayout.NORTH);
        myCentralPanel = new JPanel();
        myCentralPanel.setLayout(new BorderLayout(0, 0));
        panel1.add(myCentralPanel, BorderLayout.SOUTH);
        myBenchmarkButton = new JButton();
        myBenchmarkButton.setText("Benchmark");
        myBenchmarkButton.setMnemonic('B');
        myBenchmarkButton.setDisplayedMnemonicIndex(0);
        myBenchmarkButton.setToolTipText("Start benchmarking process");
        myCentralPanel.add(myBenchmarkButton, BorderLayout.WEST);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        panel1.add(panel2, BorderLayout.CENTER);
        final JLabel label1 = new JLabel();
        label1.setText("Chars:");
        label1.setDisplayedMnemonic('C');
        label1.setDisplayedMnemonicIndex(0);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel2.add(label1, gbc);
        myCount = new JSpinner();
        myCount.setToolTipText("Total number of chars to type");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 22);
        panel2.add(myCount, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Delay, ms:");
        label2.setDisplayedMnemonic('D');
        label2.setDisplayedMnemonicIndex(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel2.add(label2, gbc);
        myDelay = new JSpinner();
        myDelay.setToolTipText("Delay between key presses");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel2.add(myDelay, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Period:");
        label3.setDisplayedMnemonic('P');
        label3.setDisplayedMnemonicIndex(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel2.add(label3, gbc);
        myPausePeriod = new JSpinner();
        myPausePeriod.setToolTipText("Number of chars in each batch");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 22);
        panel2.add(myPausePeriod, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Length, ms:");
        label4.setDisplayedMnemonic('L');
        label4.setDisplayedMnemonicIndex(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel2.add(label4, gbc);
        myPauseLength = new JSpinner();
        myPauseLength.setToolTipText("Delay between batches");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(myPauseLength, gbc);
        myPausesEnabled = new JCheckBox();
        myPausesEnabled.setText("Intermediate  pauses");
        myPausesEnabled.setMnemonic('S');
        myPausesEnabled.setDisplayedMnemonicIndex(17);
        myPausesEnabled.setToolTipText("Periodical pauses between batches");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel2.add(myPausesEnabled, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Title:");
        label5.setDisplayedMnemonic('T');
        label5.setDisplayedMnemonicIndex(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel2.add(label5, gbc);
        myTitle = new JTextField();
        myTitle.setColumns(10);
        myTitle.setToolTipText("Title of measurement");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel2.add(myTitle, gbc);
        myResetButton = new JButton();
        myResetButton.setText("Reset");
        myResetButton.setMnemonic('R');
        myResetButton.setDisplayedMnemonicIndex(0);
        myResetButton.setToolTipText("Reset parameters to default values");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        panel2.add(myResetButton, gbc);
        final JSeparator separator1 = new JSeparator();
        separator1.setOrientation(1);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 8, 0, 8);
        panel2.add(separator1, gbc);
        myAsync = new JCheckBox();
        myAsync.setText("Async");
        myAsync.setMnemonic('A');
        myAsync.setDisplayedMnemonicIndex(0);
        myAsync.setToolTipText("Don't wait for char to appear before pausing and typing a next one");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel2.add(myAsync, gbc);
        myNative = new JCheckBox();
        myNative.setText("Native API");
        myNative.setMnemonic('N');
        myNative.setDisplayedMnemonicIndex(0);
        myNative.setToolTipText("Access screen via direct platform API calls (faster)");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel2.add(myNative, gbc);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setOrientation(0);
        splitPane1.setResizeWeight(0.3);
        myComponent.add(splitPane1, BorderLayout.CENTER);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        splitPane1.setLeftComponent(panel3);
        myToolBar = new JToolBar();
        myToolBar.setFloatable(false);
        panel3.add(myToolBar, BorderLayout.NORTH);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, BorderLayout.CENTER);
        myTable = new MutableTable();
        scrollPane1.setViewportView(myTable);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        splitPane1.setRightComponent(panel4);
        myChartPanel = new ChartPanel();
        panel4.add(myChartPanel, BorderLayout.CENTER);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel4.add(panel5, BorderLayout.NORTH);
        final JLabel label6 = new JLabel();
        label6.setText("Chart:");
        panel5.add(label6);
        mySeriesButton = new JRadioButton();
        mySeriesButton.setSelected(true);
        mySeriesButton.setText("Series (ms / n)");
        mySeriesButton.setMnemonic('E');
        mySeriesButton.setDisplayedMnemonicIndex(1);
        panel5.add(mySeriesButton);
        myDistributionButton = new JRadioButton();
        myDistributionButton.setSelected(false);
        myDistributionButton.setText("Distribution (% / ms)");
        myDistributionButton.setMnemonic('I');
        myDistributionButton.setDisplayedMnemonicIndex(1);
        panel5.add(myDistributionButton);
        label1.setLabelFor(myCount);
        label2.setLabelFor(myDelay);
        label3.setLabelFor(myPausePeriod);
        label4.setLabelFor(myPauseLength);
        label5.setLabelFor(myTitle);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return myComponent;
    }
}
