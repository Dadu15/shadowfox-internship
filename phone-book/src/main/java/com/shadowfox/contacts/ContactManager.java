package com.shadowfox.contacts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * In-memory contact store implementing CRUD (Create, Read, Update, Delete).
 *
 * Data structure choice: {@link ArrayList} for the contact list itself,
 * because the dominant operations here are "list everyone" and "search by
 * substring," both of which are sequential scans regardless of the backing
 * structure - ArrayList's cache-friendly contiguous storage and O(1)
 * indexed access make it the better fit than a LinkedList (which would only
 * pay off if we were doing frequent insertions/deletions in the middle of
 * the list, which this app doesn't do).
 *
 * A separate {@link HashSet} of phone numbers is kept alongside the list
 * purely so duplicate-phone checks are O(1) instead of an O(n) scan on
 * every add.
 */
public class ContactManager {

    private final List<Contact> contacts = new ArrayList<>();
    private final Set<String> knownPhoneNumbers = new HashSet<>();

    public Contact addContact(String name, String phone, String email) {
        Contact contact = new Contact(name, phone, email); // validates internally
        if (knownPhoneNumbers.contains(contact.getPhone())) {
            throw new IllegalArgumentException(
                    "A contact with phone number " + contact.getPhone() + " already exists.");
        }
        contacts.add(contact);
        knownPhoneNumbers.add(contact.getPhone());
        return contact;
    }

    public List<Contact> listAll() {
        return new ArrayList<>(contacts); // defensive copy - callers can't mutate internal state
    }

    /** Case-insensitive substring search across name, phone, and email. */
    public List<Contact> search(String query) {
        String needle = query.trim().toLowerCase();
        List<Contact> results = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.getName().toLowerCase().contains(needle)
                    || c.getPhone().toLowerCase().contains(needle)
                    || c.getEmail().toLowerCase().contains(needle)) {
                results.add(c);
            }
        }
        return results;
    }

    public Optional<Contact> findByPhone(String phone) {
        return contacts.stream().filter(c -> c.getPhone().equals(phone.trim())).findFirst();
    }

    public void updateContact(String existingPhone, String newName, String newPhone, String newEmail) {
        Contact existing = findByPhone(existingPhone)
                .orElseThrow(() -> new IllegalArgumentException("No contact found with phone " + existingPhone));

        boolean phoneChanged = !existing.getPhone().equals(newPhone.trim());
        if (phoneChanged && knownPhoneNumbers.contains(newPhone.trim())) {
            throw new IllegalArgumentException("Phone number " + newPhone + " is already used by another contact.");
        }

        // Validate before mutating anything, so a bad update never leaves
        // the contact half-updated.
        Contact validated = new Contact(newName, newPhone, newEmail);

        if (phoneChanged) {
            knownPhoneNumbers.remove(existing.getPhone());
            knownPhoneNumbers.add(validated.getPhone());
        }
        existing.setName(validated.getName());
        existing.setPhone(validated.getPhone());
        existing.setEmail(validated.getEmail());
    }

    public void deleteContact(String phone) {
        Contact existing = findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("No contact found with phone " + phone));
        contacts.remove(existing);
        knownPhoneNumbers.remove(existing.getPhone());
    }

    public int count() {
        return contacts.size();
    }
}
