package com.shadowfox.contacts;

import java.util.List;
import java.util.Scanner;

/**
 * Baseline build for Task 2: Simple Contact Management System.
 * Menu layout mirrors the mockup in the internship resources PDF.
 */
public class ConsoleContactApp {

    private final Scanner scanner = new Scanner(System.in);
    private final ContactManager manager = new ContactManager();

    public void run() {
        System.out.println("**** Welcome to Contact Management System ****");
        boolean keepGoing = true;

        while (keepGoing) {
            printMenu();
            String choice = readLine("Enter the choice: ").trim();

            try {
                switch (choice) {
                    case "1" -> addContact();
                    case "2" -> listContacts();
                    case "3" -> searchContact();
                    case "4" -> editContact();
                    case "5" -> deleteContact();
                    case "0" -> keepGoing = false;
                    default -> System.out.println("Invalid choice. Please pick an option from the menu.");
                }
            } catch (IllegalArgumentException e) {
                // All validation errors (bad email/phone, duplicates, not
                // found, etc.) surface here with a clear message instead of
                // crashing the session.
                System.out.println("Error: " + e.getMessage());
            }

            if (keepGoing) {
                System.out.println();
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("====================");
        System.out.println("     MAIN MENU");
        System.out.println("====================");
        System.out.println("[1] Add a new Contact");
        System.out.println("[2] List all Contacts");
        System.out.println("[3] Search for contact");
        System.out.println("[4] Edit a Contact");
        System.out.println("[5] Delete a Contact");
        System.out.println("[0] Exit");
        System.out.println("====================");
    }

    private void addContact() {
        String name = readLine("Name: ");
        String phone = readLine("Phone: ");
        String email = readLine("Email: ");
        Contact contact = manager.addContact(name, phone, email);
        System.out.println("Added: " + contact);
    }

    private void listContacts() {
        List<Contact> all = manager.listAll();
        if (all.isEmpty()) {
            System.out.println("No contacts yet.");
            return;
        }
        System.out.printf("%-20s %-16s %s%n", "NAME", "PHONE", "EMAIL");
        all.forEach(System.out::println);
        System.out.println(all.size() + " contact(s).");
    }

    private void searchContact() {
        String query = readLine("Search (name/phone/email): ");
        List<Contact> results = manager.search(query);
        if (results.isEmpty()) {
            System.out.println("No matches.");
            return;
        }
        results.forEach(System.out::println);
    }

    private void editContact() {
        String phone = readLine("Phone number of contact to edit: ");
        // Confirm it exists before asking for new details, so we fail fast
        // with a clear message instead of collecting input for nothing.
        manager.findByPhone(phone).orElseThrow(() ->
                new IllegalArgumentException("No contact found with phone " + phone));

        String newName = readLine("New name: ");
        String newPhone = readLine("New phone: ");
        String newEmail = readLine("New email: ");
        manager.updateContact(phone, newName, newPhone, newEmail);
        System.out.println("Contact updated.");
    }

    private void deleteContact() {
        String phone = readLine("Phone number of contact to delete: ");
        String confirm = readLine("Are you sure? (y/n): ").trim().toLowerCase();
        if (!confirm.equals("y")) {
            System.out.println("Cancelled.");
            return;
        }
        manager.deleteContact(phone);
        System.out.println("Deleted.");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
