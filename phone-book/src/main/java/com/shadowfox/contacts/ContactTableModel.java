package com.shadowfox.contacts;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Binds {@link ContactManager}'s data to a Swing {@link javax.swing.JTable}.
 * This is the Swing equivalent of JavaFX's ObservableList + TableView data
 * binding: the table asks this model for rows/columns on demand, and
 * calling {@link #refresh()} after any mutation tells the table to redraw.
 */
public class ContactTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"Name", "Phone", "Email"};

    private final ContactManager manager;
    private List<Contact> rows;

    public ContactTableModel(ContactManager manager) {
        this.manager = manager;
        this.rows = manager.listAll();
    }

    /** Call after any add/update/delete/import so the table reflects the latest data. */
    public void refresh() {
        rows = manager.listAll();
        fireTableDataChanged();
    }

    public Contact contactAt(int rowIndex) {
        return rows.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Contact c = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> c.getName();
            case 1 -> c.getPhone();
            case 2 -> c.getEmail();
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // edits go through the dialog, not inline cell editing
    }
}
