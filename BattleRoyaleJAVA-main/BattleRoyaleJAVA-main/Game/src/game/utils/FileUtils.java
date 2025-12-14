package game.utils;

import game.Player;
import game.exceptions.FileSaveException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileUtils {

    private static final int MAX_LOG_SIZE = 1000;

    public static void saveGameResult(
            String filename,
            Player winner,
            List<String> log
    ) throws FileSaveException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

            writer.write("=== BATTLE ROYALE RESULT ===");
            writer.newLine();
            writer.newLine();

            if (winner != null) {
                writer.write("Winner:");
                writer.newLine();
                writer.write(winner.toString());
            } else {
                writer.write("No winner (all players eliminated)");
            }

            writer.newLine();
            writer.newLine();

            writer.write("=== OPERATIONS LOG (last "
                    + Math.min(MAX_LOG_SIZE, log.size())
                    + " events) ===");
            writer.newLine();

            int start = Math.max(0, log.size() - MAX_LOG_SIZE);
            for (int i = start; i < log.size(); i++) {
                writer.write(log.get(i));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new FileSaveException("Failed to save game result.", e);
        }
    }
}
