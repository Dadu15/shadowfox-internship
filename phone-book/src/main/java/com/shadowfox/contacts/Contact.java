package com.shadowfox.contacts;

import java.util.regex.Pattern;

/**
 * Plain Old Java Object representing a single contact.
 *
 * Fields are private with public getters/setters (encapsulation) - callers
 * can never put the object into an invalid state directly, because the
 * setters validate before assigning. This keeps validation logic in exactly
 * one place instead of scattered across every caller.
 */
public class Contact {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Digits only, 7-15 of them, optionally prefixed with '+'. Covers most
    // real-world phone formats without being so loose it accepts garbage.
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?\\d{7,15}$");

    private String name;
    private String phone;
    private String email;

    public Contact(String name, String phone, String email) {
        setName(name);
        setPhone(phone);
        setEmail(email);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Phone number must be 7-15 digits (optionally starting with '+'). Got: \"" + phone + "\"");
        }
        this.phone = phone.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("\"" + email + "\" does not look like a valid email address.");
        }
        this.email = email.trim();
    }

    @Override
    public String toString() {
        return String.format("%-20s %-16s %s", name, phone, email);
    }
}
