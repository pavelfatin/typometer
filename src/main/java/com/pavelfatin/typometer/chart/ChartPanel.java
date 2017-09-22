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

package com.pavelfatin.typometer.chart;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.round;

public class ChartPanel extends JComponent {
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    private static final String EMPTY_LABEL = "No chart data";
    private static final Color EMPTY_LABEL_COLOR = Color.GRAY;

    private static final String FILE_EXTENSION = "png";
    private static final String FILE_DESCRIPTION = "PNG files";

    private static final FileNameExtensionFilter PNG_FILE_FILTER =
            new FileNameExtensionFilter(FILE_DESCRIPTION, FILE_EXTENSION);

    private static Optional<File> myCurrentDirectory = Optional.empty();

    private Link mySaveLink = new Link("Save", "Save the chart to PNG file (with legend)", e -> onSave());

    private ChartType myType = ChartType.Line;
    private Optional<ChartData> myData = Optional.empty();

    public ChartPanel() {
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        mySaveLink.setVisible(false);
        add(mySaveLink);
    }

    @Override
    public void doLayout() {
        super.doLayout();

        Dimension linkSize = mySaveLink.getPreferredSize();

        mySaveLink.setSize(linkSize);
        mySaveLink.setLocation(getParent().getWidth() - linkSize.width - 14, 10);
    }

    public void setData(ChartType type, Optional<ChartData> data) {
        mySaveLink.setVisible(data.isPresent());
        myType = type;
        myData = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        paintComponent(g, createRectangle(getSize(), getInsets()));
    }

    private void paintComponent(Graphics g, Rectangle r) {
        g.setColor(getBackground());
        g.fillRect(r.x, r.y, r.width, r.height);

        if (myData.isPresent()) {
            ChartPainter.paint(g, r, myType, myData.get());
        } else {
            g.setColor(EMPTY_LABEL_COLOR);
            drawString(g, r, EMPTY_LABEL);
        }
    }

    private static void drawString(Graphics g, Rectangle r, String s) {
        FontMetrics metrics = g.getFontMetrics();
        int x = r.x + (int) round(0.5D * (r.width - metrics.stringWidth(s)));
        int y = r.y + (int) round(0.5D * (r.height + metrics.getAscent()));
        g.drawString(s, x, y);
    }

    private static Rectangle createRectangle(Dimension size, Insets insets) {
        return new Rectangle(insets.left, insets.top,
                size.width - insets.left - insets.right,
                size.height - insets.top - insets.bottom);
    }

    private void onSave() {
        ChartData data = myData.get();

        JFileChooser chooser = createFileChooser(JFileChooser.SAVE_DIALOG, "Save chart");
        chooser.setSelectedFile(new File(titleOf(data)));

        if (chooser.showDialog(getTopLevelAncestor(), null) == JFileChooser.APPROVE_OPTION) {
            File file = withExtension(chooser.getSelectedFile(), FILE_EXTENSION);

            if (file.exists()) {
                String message = String.format("The file '%s' already exists. Overwrite?", file.getName());

                int choice = JOptionPane.showConfirmDialog(getTopLevelAncestor(), message, "Save chart", JOptionPane.YES_NO_OPTION);

                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            BufferedImage image = createChartImage(myType, data, getSize(), getInsets());

            try {
                ImageIO.write(image, "PNG", file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot write file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String titleOf(ChartData data) {
        return data.getSeries().stream().map(it -> trim(it.getTitle())).collect(Collectors.joining(" VS "));
    }

    private static String trim(String title) {
        int i = title.indexOf(" — ");
        return i > 0 ? title.substring(0, i) : title;
    }

    private static BufferedImage createChartImage(ChartType type, ChartData data, Dimension size, Insets insets) {
        int legendHeight = LegendPainter.heightOf(data.getSeries().size());

        BufferedImage image = new BufferedImage(size.width, legendHeight + size.height, BufferedImage.TYPE_INT_RGB);

        Graphics graphics = image.getGraphics();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, size.width, legendHeight + size.height);

        LegendPainter.paint(graphics, new Point(0, size.height), data.getSeries());

        Rectangle dataRectangle = createRectangle(size, insets);
        ChartPainter.paint(graphics, dataRectangle, type, data);

        graphics.dispose();

        return image;
    }

    private static JFileChooser createFileChooser(int type, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(type);
        chooser.setDialogTitle(title);
        chooser.addChoosableFileFilter(PNG_FILE_FILTER);
        chooser.setFileFilter(PNG_FILE_FILTER);
        chooser.addActionListener(action -> {
            if (JFileChooser.APPROVE_SELECTION.equals(action.getActionCommand())) {
                myCurrentDirectory = Optional.of(chooser.getCurrentDirectory());
            }
        });
        myCurrentDirectory.ifPresent(chooser::setCurrentDirectory);
        return chooser;
    }

    private static File withExtension(File file, String extension) {
        String suffix = "." + extension;
        String path = file.getPath();
        return path.endsWith(suffix) ? file : new File(path + suffix);
    }
}
