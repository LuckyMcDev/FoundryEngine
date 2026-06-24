package de.luckymcdev.foundryengine.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Computes SHA-256 hashes of folder contents for change detection.
 */
public class FolderHash {
    /**
     * Computes a SHA-256 hash of all files in a folder, sorted by path.
     */
    public static String hashFolder(Path folder) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        List<Path> files = new ArrayList<>();

        try (var stream = Files.walk(folder)) {
            stream.filter(Files::isRegularFile)
                    .forEach(files::add);
        }

        files.sort(Comparator.comparing(p -> folder.relativize(p).toString()));

        for (Path file : files) {
            String relativePath = folder.relativize(file).toString().replace("\\", "/");
            digest.update(relativePath.getBytes());

            try (InputStream is = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
        }

        byte[] hash = digest.digest();

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }
}

