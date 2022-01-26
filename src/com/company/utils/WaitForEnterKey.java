package com.company.utils;

import java.io.BufferedReader;
import java.io.IOException;

public class WaitForEnterKey {
    public static void wait(BufferedReader reader) throws IOException {
        System.out.println("Press Enter and pass the move to another player");
        while (true) {
            char c = (char) reader.read();
            if (c == '\n') {
                break;
            }
        }
        clearScreen();
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
