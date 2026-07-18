package com.shadowfox.contacts;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Tier 1 (Grounded Upgrade) for Task 2: a table-based dashboard instead of
 * a CLI menu. Double-clicking a row opens it for editing - this is the
 * "Data Binding between Objects and UI components" concept from the
 * handbook, implemented with a JTable + AbstractTableModel (the Swing
 * equivalent of a JavaFX TableView + ObservableList).
 *
 * Also wires up Tier 2 (vCard export/import) via toolbar buttons.
 */
public class ContactGUI extends JFrame {

    private final ContactManager manager = new ContactManager();
    private final ContactTableModel tableModel = new ContactTableModel(manager);
    private final VCardService vCardService = new VCardService();
    private final JTable table = new JTable(tableModel);

    public ContactGUI() {
        super("ShadowFox Contact Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) editContact(tableModel.contactAt(row));
                }
            }
        });

        setSize(640, 420);
        setLocationRelativeTo(null);
    }

    private JToolBar buildToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton addBtn = new JButton("Add Contact");
        addBtn.addActionListener(e -> addContact());

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelected());

        JButton exportBtn = new JButton("Export All (.vcf)");
        exportBtn.addActionListener(e -> exportAll());

        JButton importBtn = new JButton("Import (.vcf)");
        importBtn.addActionListener(e -> importFile());

        toolbar.add(addBtn);
        toolbar.add(deleteBtn);
        toolbar.addSeparator();
        toolbar.add(exportBtn);
        toolbar.add(importBtn);
        return toolbar;
    }

    private void addContact() {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        if (!showContactForm("Add Contact", nameField, phoneField, emailField)) return;

        try {
            manager.addContact(nameField.getText(), phoneField.getText(), emailField.getText());
            tableModel.refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void editContact(Contact contact) {
        JTextField nameField = new JTextField(contact.getName());
        JTextField phoneField = new JTextField(contact.getPhone());
        JTextField emailField = new JTextField(contact.getEmail());
        if (!showContactForm("Edit Contact", nameField, phoneField, emailField)) return;

        try {
            manager.updateContact(contact.getPhone(), nameField.getText(), phoneField.getText(), emailField.getText());
            tableModel.refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    /** Shows a small OK/Cancel form. Returns true if the user pressed OK. */
    private boolean showContactForm(String title, JTextField name, JTextField phone, JTextField email) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        panel.add(new JLabel("Name:"));
        panel.add(name);
        panel.add(new JLabel("Phone:"));
        panel.add(phone);
        panel.add(new JLabel("Email:"));
        panel.add(email);

        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
        return result == JOptionPane.OK_OPTION;
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a contact first.");
            return;
        }
        Contact contact = tableModel.contactAt(row);
        int confirm = JOptionPane.showConfirmDialog(
                this, "Delete " + contact.getName() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        manager.deleteContact(contact.getPhone());
        tableModel.refresh();
    }

    private void exportAll() {
        List<Contact> all = manager.listAll();
        if (all.isEmpty()) {
            showError("No contacts to export.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("contacts.vcf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            vCardService.exportAll(all, chooser.getSelectedFile().toPath());
            JOptionPane.showMessageDialog(this, "Exported " + all.size() + " contact(s).");
        } catch (RuntimeException ex) {
            showError("Export failed: " + ex.getMessage());
        }
    }

    private void importFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path file = chooser.getSelectedFile().toPath();
        try {
            VCardService.ImportResult result = vCardService.importFile(file, manager);
            tableModel.refresh();
            String message = "Imported " + result.imported().size() + " contact(s).";
            if (!result.skipped().isEmpty()) {
                message += "\nSkipped " + result.skipped().size() + ":\n" + String.join("\n", result.skipped());
            }
            JOptionPane.showMessageDialog(this, message);
        } catch (RuntimeException ex) {
            showError("Import failed: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new ContactGUI().setVisible(true));
    }
}
