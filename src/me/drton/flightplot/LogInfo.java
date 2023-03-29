package me.drton.flightplot;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import me.drton.jmavlib.log.LogReader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * User: ton Date: 27.10.13 Time: 17:45
 */
public class LogInfo {
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JTable infoTable;
    private DefaultTableModel infoTableModel;
    private JTable parametersTable;
    private DefaultTableModel parametersTableModel;
    private DateFormat dateFormat;

    public LogInfo() {
        mainFrame = new JFrame("Log Info");
        $$$setupUI$$$();
        mainFrame.setContentPane(mainPanel);
        mainFrame.pack();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public JFrame getFrame() {
        return mainFrame;
    }

    public void setVisible(boolean visible) {
        mainFrame.setVisible(visible);
    }

    public void updateInfo(LogReader logReader) {
        while (infoTableModel.getRowCount() > 0) {
            infoTableModel.removeRow(0);
        }
        while (parametersTableModel.getRowCount() > 0) {
            parametersTableModel.removeRow(0);
        }
        if (logReader != null) {
            infoTableModel.addRow(new Object[]{"Format", logReader.getFormat()});
            infoTableModel.addRow(new Object[]{"System", logReader.getSystemName()});
            infoTableModel.addRow(new Object[]{
                    "Length, s", String.format(Locale.ROOT, "%.3f", logReader.getSizeMicroseconds() * 1e-6)});
            String startTimeStr = "";
            String endTimeStr = "";
            if (logReader.getUTCTimeReferenceMicroseconds() > 0) {
                startTimeStr = dateFormat.format(
                        new Date((logReader.getStartMicroseconds() + logReader.getUTCTimeReferenceMicroseconds()) / 1000)) + " UTC";
                endTimeStr = dateFormat.format(
                        new Date((logReader.getStartMicroseconds() + logReader.getSizeMicroseconds() + logReader.getUTCTimeReferenceMicroseconds()) / 1000)) + " UTC";
            }
            infoTableModel.addRow(new Object[]{
                    "Start Time", startTimeStr});
            infoTableModel.addRow(new Object[]{
                    "End Time", endTimeStr});
            infoTableModel.addRow(new Object[]{"Updates count", logReader.getSizeUpdates()});
            infoTableModel.addRow(new Object[]{"Errors", logReader.getErrors().size()});
            Map<String, Object> ver = logReader.getVersion();
            infoTableModel.addRow(new Object[]{"Hardware Version", ver.get("HW")});
            infoTableModel.addRow(new Object[]{"Firmware Version", ver.get("FW")});
            infoTableModel.addRow(new Object[]{"Firmware Tag", ver.get("Tag")});
            infoTableModel.addRow(new Object[]{"UID", ver.get("UID")});
            infoTableModel.addRow(new Object[]{"Compile Time", ver.get("CTS")});
            Map<String, Object> parameters = logReader.getParameters();
            List<String> keys = new ArrayList<String>(parameters.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                parametersTableModel.addRow(new Object[]{key, parameters.get(key).toString()});
            }
        }
    }

    private void createUIComponents() {
        // Info table
        infoTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        infoTableModel.addColumn("Property");
        infoTableModel.addColumn("Value");
        infoTable = new JTable(infoTableModel);
        // Parameters table
        parametersTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        parametersTableModel.addColumn("Parameter");
        parametersTableModel.addColumn("Value");
        parametersTable = new JTable(parametersTableModel);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(300);
        mainPanel.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(panel1);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Info", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setViewportView(infoTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel2);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel2.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane2.setViewportView(parametersTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
