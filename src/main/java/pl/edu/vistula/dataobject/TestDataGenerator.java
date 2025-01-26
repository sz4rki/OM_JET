package pl.edu.vistula.dataobject;

import java.security.SecureRandom;
import java.util.Random;

public class TestDataGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new SecureRandom();

    public static String generateRandomEmail() {
        int length = 8 + RANDOM.nextInt(5);
        StringBuilder email = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            email.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return email.toString() + "@example.com";
    }

    public static String generateRandomPassword() {
        int length = 8 + RANDOM.nextInt(5);
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    public static String generateRandomName() {
        int length = 3 + RANDOM.nextInt(10);
        StringBuilder name = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            name.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return name.toString();
    }

    public static void main(String[] args) {
        System.out.println("Generated Email: " + generateRandomEmail());
        System.out.println("Generated Password: " + generateRandomPassword());
        System.out.println("Generated First Name: " + generateRandomName());
        System.out.println("Generated Last Name: " + generateRandomName());
    }

}
