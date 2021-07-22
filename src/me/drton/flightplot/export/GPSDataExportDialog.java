package me.drton.flightplot.export;

import me.drton.flightplot.PreferencesUtil;
import me.drton.jmavlib.conversion.RotationConversion;
import me.drton.jmavlib.log.FormatErrorException;
import me.drton.jmavlib.log.LogReader;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.jfree.data.Range;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Matrix3d;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

public class GPSDataExportDialog extends JDialog {
    private static final String DIALOG_SETTING = "TrackExportDialog";
    private static final String EXPORTER_CONFIGURATION_SETTING = "CamExporterConfiguration";
    private static final String READER_CONFIGURATION_SETTING = "ReaderConfiguration";
    private static final String LAST_EXPORT_DIRECTORY_SETTING = "CamLastExportDirectory";

    private JPanel contentPane;
    private JButton buttonExport;
    private JButton buttonClose;
    private JLabel statusLabel;
    private JProgressBar exportProgress;
    private JButton buttonCancel;
    private File lastExportDirectory;

    private LogReader logReader;
    private boolean stopExport;

    private class ExportFormatItem {
        String description;
        String formatName;

        ExportFormatItem(String description, String formatName) {
            this.description = description;
            this.formatName = formatName;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public GPSDataExportDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonExport);
        setTitle("GPS Raw Data Export");
        buttonExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                export();
            }
        });
        buttonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopExport = true;
            }
        });
        // call onClose() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });
        // call onClose() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
    }

    private String stringFromMicroseconds(long us) {
        return String.format(Locale.ROOT, "%.3f", us / 1000000.0);
    }

    private String formatTime(long time) {
        long s = time / 1000000;
        long ms = (time / 1000) % 1000;
        return String.format(Locale.ROOT, "%02d:%02d:%02d.%03d",
                (int) (s / 3600), s / 60 % 60, s % 60, ms);
    }

    public void display(LogReader logReader, Range chartRange) {
        if (logReader == null) {
            throw new RuntimeException("Log not opened");
        }
        this.logReader = logReader;

        setVisible(true);
    }

    private double getLogSizeInSeconds() {
        return logReader.getSizeMicroseconds() / 1000000.0;
    }

    private File getDestinationFile(String extension, String description) {
        JFileChooser fc = new JFileChooser();
        if (lastExportDirectory != null) {
            fc.setCurrentDirectory(lastExportDirectory);
        }
        FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(description, extension);
        fc.setFileFilter(extensionFilter);
        fc.setDialogTitle("Export Tags");
        int returnVal = fc.showDialog(null, "Export");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            lastExportDirectory = fc.getCurrentDirectory();
            String exportFileName = fc.getSelectedFile().toString();
            String exportFileExtension = extensionFilter.getExtensions()[0];
            if (extensionFilter == fc.getFileFilter() && !exportFileName.toLowerCase().endsWith(exportFileExtension)) {
                exportFileName += ("." + exportFileExtension);
            }
            File exportFile = new File(exportFileName);
            if (!exportFile.exists()) {
                return exportFile;
            } else {
                int result = JOptionPane.showConfirmDialog(null,
                        "Do you want to overwrite the existing file?" + "\n" + exportFile.getAbsoluteFile(),
                        "File already exists", JOptionPane.YES_NO_OPTION);
                if (JOptionPane.YES_OPTION == result) {
                    return exportFile;
                }
            }
        }
        return null;
    }

    private void export() {
        final File file = getDestinationFile("ubx", "UBX");

        setStatus("Exporting...", false);
        stopExport = false;
        buttonCancel.setEnabled(true);
        exportProgress.setValue(0);
        exportProgress.setMaximum((int)getLogSizeInSeconds());

        new Thread() {
            public void run() {
                try {
                    if (null != file) {
                        BufferedOutputStream bos1 = new BufferedOutputStream(new FileOutputStream(file + "_1"));
                        BufferedOutputStream bos2 = new BufferedOutputStream(new FileOutputStream(file + "_2"));

                        Map<String, Object> data = new HashMap<String, Object>();
                        while (true) {
                            data.clear();
                            long t;
                            try {
                                t = logReader.readUpdate(data);
                            } catch (EOFException e) {
                                break;  // End of file
                            }

                            Number len = (Number) data.get("GPD0.len");
                            byte[] d = (byte[]) data.get("GPD0.data");

                            // len > 0 means it's data from the GPS (negative length is data to the GPS)

                            if (len != null && len.intValue() > 0) {
                                bos1.write(d, 0, len.intValue());
                            }

                            len = (Number) data.get("GPD1.len");
                            d = (byte[]) data.get("GPD1.data");

                            if (len != null && len.intValue() > 0) {
                                bos2.write(d, 0, len.intValue());
                            }

                            exportProgress.setValue((int)(t / 1e6));

                            if (stopExport) {
                                bos1.close();
                                bos2.close();
                                throw new InterruptedException();
                            }
                        }

                        bos1.close();
                        bos2.close();

                        exportProgress.setValue(exportProgress.getMaximum());

                        setStatus("Exported", false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FormatErrorException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    setStatus("Export canceled", false);
                }

                buttonCancel.setEnabled(false);
            }
        }.start();
    }



    private void setStatus(String status, boolean error) {
        statusLabel.setText(status);
        if (error) {
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setForeground(Color.BLACK);
        }
    }

    private Long parseExportTime(JTextField field, JLabel label) {
        try {
            long time = (long) (Double.parseDouble(field.getText()) * 1000000);
            label.setText(formatTime(time));
            return time;
        } catch (NumberFormatException e) {
            label.setText("-");
            return null;
        }
    }

    private void onClose() {
        dispose();
    }

}
