package org.example.client.util;

import java.util.Scanner;

public class IOUtil {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static String getClientInput() {
        return SCANNER.nextLine();
    }

    public static void closeClientInput() {
        SCANNER.close();
    }
}
