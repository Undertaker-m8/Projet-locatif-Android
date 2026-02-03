package com.example.miniprojet.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHasher {

    /**
     * Hash un mot de passe avec BCrypt
     * @param password Mot de passe en clair
     * @return Le mot de passe hashé
     */
    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    /**
     * Vérifie si un mot de passe correspond au hash
     * @param password Mot de passe en clair
     * @param hashedPassword Mot de passe hashé
     * @return true si le mot de passe correspond
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
        return result.verified;
    }

    /**
     * Valide la force du mot de passe
     * @param password Mot de passe à valider
     * @return Message d'erreur ou null si valide
     */
    public static String validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractères";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Le mot de passe doit contenir au moins une majuscule";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Le mot de passe doit contenir au moins une minuscule";
        }

        if (!password.matches(".*[0-9].*")) {
            return "Le mot de passe doit contenir au moins un chiffre";
        }

        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return "Le mot de passe doit contenir au moins un caractère spécial";
        }

        return null; // Mot de passe valide
    }
}