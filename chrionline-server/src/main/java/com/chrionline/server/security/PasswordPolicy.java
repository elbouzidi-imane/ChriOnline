package com.chrionline.server.security;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class PasswordPolicy {
    private PasswordPolicy() {
    }

    public static String validate(String password, String... forbiddenTerms) {
        if (password == null || password.isBlank()) {
            return "Mot de passe obligatoire";
        }
        if (password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caracteres";
        }
        if (!containsUppercase(password)) {
            return "Le mot de passe doit contenir au moins une lettre majuscule";
        }
        if (!containsLowercase(password)) {
            return "Le mot de passe doit contenir au moins une lettre minuscule";
        }
        if (!containsDigit(password)) {
            return "Le mot de passe doit contenir au moins un chiffre";
        }
        if (!containsSpecialCharacter(password)) {
            return "Le mot de passe doit contenir au moins un caractere special";
        }
        if (containsForbiddenTerm(password, forbiddenTerms)) {
            return "Le mot de passe ne doit pas contenir l'email, le nom, le prenom ou la date de naissance";
        }
        return null;
    }

    public static String[] buildForbiddenTerms(String email, String nom, String prenom, String dateNaissance) {
        Date parsedBirthDate = parseBirthDate(dateNaissance);
        if (parsedBirthDate == null) {
            return new String[]{email, nom, prenom, dateNaissance};
        }
        SimpleDateFormat[] formats = new SimpleDateFormat[]{
                new SimpleDateFormat("yyyy-MM-dd"),
                new SimpleDateFormat("yyyyMMdd"),
                new SimpleDateFormat("ddMMyyyy"),
                new SimpleDateFormat("dd-MM-yyyy"),
                new SimpleDateFormat("dd/MM/yyyy"),
                new SimpleDateFormat("MMddyyyy"),
                new SimpleDateFormat("MM-dd-yyyy"),
                new SimpleDateFormat("MM/dd/yyyy")
        };
        String[] terms = new String[3 + formats.length];
        terms[0] = email;
        terms[1] = nom;
        terms[2] = prenom;
        for (int i = 0; i < formats.length; i++) {
            terms[3 + i] = formats[i].format(parsedBirthDate);
        }
        return terms;
    }

    private static boolean containsForbiddenTerm(String password, String... forbiddenTerms) {
        String normalizedPassword = password.toLowerCase();
        for (String term : forbiddenTerms) {
            if (term == null || term.isBlank()) {
                continue;
            }
            String normalizedTerm = term.trim().toLowerCase();
            if (normalizedPassword.contains(normalizedTerm)) {
                return true;
            }
            int atIndex = normalizedTerm.indexOf('@');
            if (atIndex > 0) {
                String localPart = normalizedTerm.substring(0, atIndex);
                if (localPart.length() >= 3 && normalizedPassword.contains(localPart)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Date parseBirthDate(String dateNaissance) {
        if (dateNaissance == null || dateNaissance.isBlank()) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(dateNaissance.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean containsUppercase(String password) {
        return password.chars().anyMatch(Character::isUpperCase);
    }

    private static boolean containsLowercase(String password) {
        return password.chars().anyMatch(Character::isLowerCase);
    }

    private static boolean containsDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }

    private static boolean containsSpecialCharacter(String password) {
        return password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
    }
}
