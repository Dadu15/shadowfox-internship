package com.shadowfox.contacts;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Tier 2 (Creative Upgrade) for Task 2: export/import contacts as standard
 * vCard (.vcf) files - the same format used by phones, Gmail, and Outlook,
 * so contacts built here can round-trip into a real address book.
 *
 * Pitch (per the Innovation Pitch protocol): a hand-rolled line-based parser
 * rather than a full vCard library. vCard 3.0 is a simple, well-documented
 * line format (BEGIN:VCARD / FN: / TEL: / EMAIL: / END:VCARD), and pulling
 * in a whole dependency for three fields would be overkill - the parser
 * below is ~30 lines and has no external dependencies, which matters since
 * this internship's baseline stack avoids extra libraries where reasonable.
 */
public class VCardService {

    /** Exports every contact into one .vcf file, one VCARD block per contact. */
    public void exportAll(List<Contact> contacts, Path outputFile) {
        StringBuilder sb = new StringBuilder();
        for (Contact c : contacts) {
            appendVCard(sb, c);
        }
        writeFile(outputFile, sb.toString());
    }

    public void exportOne(Contact contact, Path outputFile) {
        StringBuilder sb = new StringBuilder();
        appendVCard(sb, contact);
        writeFile(outputFile, sb.toString());
    }

    private void appendVCard(StringBuilder sb, Contact c) {
        sb.append("BEGIN:VCARD\r\n");
        sb.append("VERSION:3.0\r\n");
        sb.append("FN:").append(escape(c.getName())).append("\r\n");
        sb.append("TEL;TYPE=CELL:").append(escape(c.getPhone())).append("\r\n");
        sb.append("EMAIL:").append(escape(c.getEmail())).append("\r\n");
        sb.append("END:VCARD\r\n");
    }

    private String escape(String value) {
        // vCard escapes commas, semicolons, and backslashes per RFC 6350.
        return value.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;");
    }

    private void writeFile(Path path, String content) {
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + path, e);
        }
    }

    /**
     * Parses a .vcf file into Contact objects. Contacts that fail validation
     * (e.g. malformed email/phone) or duplicate an existing phone number are
     * skipped and reported back rather than aborting the whole import.
     */
    public ImportResult importFile(Path inputFile, ContactManager manager) {
        List<String> lines;
        try {
            lines = Files.readAllLines(inputFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + inputFile, e);
        }

        List<Contact> imported = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        String name = null, phone = null, email = null;
        for (String rawLine : lines) {
            String line = rawLine.strip();
            if (line.equalsIgnoreCase("BEGIN:VCARD")) {
                name = null;
                phone = null;
                email = null;
            } else if (line.startsWith("FN:")) {
                name = unescape(line.substring("FN:".length()));
            } else if (line.startsWith("TEL")) {
                int colon = line.indexOf(':');
                if (colon >= 0) phone = unescape(line.substring(colon + 1));
            } else if (line.startsWith("EMAIL")) {
                int colon = line.indexOf(':');
                if (colon >= 0) email = unescape(line.substring(colon + 1));
            } else if (line.equalsIgnoreCase("END:VCARD")) {
                try {
                    imported.add(manager.addContact(name, phone, email));
                } catch (IllegalArgumentException | NullPointerException e) {
                    skipped.add((name != null ? name : "(unnamed)") + " - " + e.getMessage());
                }
            }
        }

        return new ImportResult(imported, skipped);
    }

    private String unescape(String value) {
        return value.strip().replace("\\,", ",").replace("\\;", ";").replace("\\\\", "\\");
    }

    public record ImportResult(List<Contact> imported, List<String> skipped) {
    }
}
