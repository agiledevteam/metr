package com.lge.metr;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;

class CodeFatRenderer extends DefaultTableCellRenderer {
    NumberFormat formatter;
    public CodeFatRenderer() {
        super();
        formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        setHorizontalAlignment(RIGHT);
    }

    public void setValue(Object value) {
        setText((value == null) ? "" : formatter.format((double)value) + "%");
    }
}

public class MetrGui extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    MyTableModel model = new MyTableModel();
    CodeFatRenderer codeFatRenderer = new CodeFatRenderer();
    JTable table = new JTable(model) {
        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
          if (column == 4) {
            return codeFatRenderer;
          }
          return super.getCellRenderer(row, column);
        }
    };
    {
      table.setAutoCreateRowSorter(true);
      table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      table.getSelectionModel().addListSelectionListener(new RowListener());
    }
    private class RowListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
            setSelection();
        }
    }
    private void setSelection() {
      int[] selection = table.getSelectedRows();
      if (selection.length != 1) {
        model2.setDetail(null);
      } else {
        int selected = table.convertRowIndexToModel(selection[0]);
        Stat stat = model.getDetail(selected);
        model2.setDetail(stat);
      }
    }
    DetailModel model2 = new DetailModel();
    JTable table2 = new JTable(model2) {
        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
          if (column == 4) {
            return codeFatRenderer;
          }
          return super.getCellRenderer(row, column);
        }
    };
    {
        table2.setAutoCreateRowSorter(true);
        table2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }
    class DetailModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private String[] columnNames = { "Type", "Method", "SLOC",
                "FLOC", "Code Fat" };
        private ExeStat[] exes = new ExeStat[0];

        public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return exes.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            ExeStat stat = exes[row];
            if (col == 0)
                return stat.typeName();
            else if (col == 1)
                return stat.methodName();
            else if (col == 2)
                return stat.sloc();
            else if (col == 3)
                return stat.floc();
            else
                return stat.codefat();
        }
        public void setDetail(Stat stat) {
          if (stat == null) {
            exes = new ExeStat[0];
          } else {
            exes = stat.exes();
          }
          fireTableDataChanged();
        }
    };
    class MyTableModel extends AbstractTableModel {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String[] columnNames = { "Location", "Filename", "SLOC",
                "FLOC", "Code Fat" };
        private List<String> files = new ArrayList<>();
        private HashMap<String, Stat> stats = new HashMap<>();
        private Stat defaultStat = new Stat(new ExeStat[0]);

        public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return files.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            File f = new File(files.get(row));
            if (col == 0)
                return f.getParent();
            else if (col == 1)
                return f.getName();
            else {
                Stat stat = stats.get(files.get(row));
                if (col == 2) {
                    return stat.sloc();
                } else if (col == 3) {
                    return stat.floc();
                } else {
                    return stat.codefat();
                }
            }
        }

        public void add(File file) {
            String absolutePath = file.getAbsolutePath();
            int index = Collections.binarySearch(files, absolutePath);
            if (index < 0) {
                stats.put(absolutePath, defaultStat );
                int insertIndex = -(index + 1);
                files.add(insertIndex, absolutePath);
                fireTableRowsInserted(insertIndex, insertIndex);
                asyncMetr(file);
            }
        }

        public void put(File file, Stat result) {
            String absolutePath = file.getAbsolutePath();
            int index = Collections.binarySearch(files, absolutePath);
            if (index >= 0) {
                stats.put(absolutePath, result);
                fireTableRowsUpdated(index, index);

                int[] selection = table.getSelectedRows();
                if (selection.length == 1 && selection[0] == index) {
                   setSelection();
                }
            }
        }

        public Stat getDetail(int row) {
            return stats.get(files.get(row));
        }

        public void clear() {
            stats.clear();
            files.clear();
            fireTableDataChanged();
        }

        public void refresh() {
          for (String filepath: files)
            asyncMetr(new File(filepath));
        }
    }

    final int threads = Runtime.getRuntime().availableProcessors();
    ExecutorService executor = Executors.newFixedThreadPool(threads);

    void asyncMetr(final File file) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                final Stat result = Metr.metr(file);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        model.put(file, result);
                    }
                });
            }
        });
    }

    private TransferHandler handler = new TransferHandler() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public boolean canImport(TransferHandler.TransferSupport support) {
            if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return false;
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            Transferable t = support.getTransferable();
            try {
                List<File> l = (List<File>) t
                        .getTransferData(DataFlavor.javaFileListFlavor);

                for (File f : l) {
                    for (File f2 : FileUtil.java_gatherFiles(f, ".java"))
                        model.add(f2);
                }

            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (IOException e) {
                return false;
            }

            return true;
        }
    };

    public MetrGui() {
        super("Metr GUI");
        getContentPane().add(createDummyToolBar(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        JScrollPane scrollPane2 = new JScrollPane(table2);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                   scrollPane, scrollPane2);
        splitPane.setDividerLocation(400);

        getContentPane().add(splitPane);

        setTransferHandler(handler);
    }

    private static void createAndShowGUI(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        MetrGui test = new MetrGui();
        test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        test.setSize(800, 600);
        test.setLocationRelativeTo(null);
        test.setVisible(true);
        test.table.requestFocus();
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(args);
            }
        });
    }

    private JToolBar createDummyToolBar() {
        JToolBar tb = new JToolBar();
        JButton b;
        b = new JButton("Clear");
        b.setRequestFocusEnabled(false);
        b.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            model.clear();
            model2.setDetail(null);
          }
        });
        tb.add(b);
        b = new JButton("Refresh");
        b.setRequestFocusEnabled(false);
        b.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            model.refresh();
          }
        });
        tb.add(b);
        tb.setFloatable(false);
        return tb;
    }

    private JMenuBar createDummyMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.add(createDummyMenu("File"));
        mb.add(createDummyMenu("Edit"));
        mb.add(createDummyMenu("Search"));
        mb.add(createDummyMenu("View"));
        mb.add(createDummyMenu("Tools"));
        mb.add(createDummyMenu("Help"));

        JMenu demo = new JMenu("Demo");
        demo.setMnemonic(KeyEvent.VK_D);
        mb.add(demo);

        return mb;
    }

    private JMenu createDummyMenu(String str) {
        JMenu menu = new JMenu(str);
        JMenuItem item = new JMenuItem("[Empty]");
        item.setEnabled(false);
        menu.add(item);
        return menu;
    }
}
