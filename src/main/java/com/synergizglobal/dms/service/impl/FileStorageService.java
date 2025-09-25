package com.synergizglobal.dms.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
	
	
	@Value("${file.upload-dir}") 
	private String uploadDir;

    private static final int MAX_FILENAME_LENGTH = 200;
    private static final String CORRESPONDENCE_ROOT = "Correspondence";
    public List<String> saveFiles(List<MultipartFile> files) throws IOException {
        List<String> storedRelativePaths = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            throw new IOException("No files to save!");
        }

       

        // Base upload directory (absolute, normalized)
        Path baseUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // Build target directory: {uploadDir}/correspondence/{mailDirection}/{userId}
        Path targetDir = baseUploadPath.resolve(Paths.get(CORRESPONDENCE_ROOT)).normalize();

        // Security check: ensure targetDir is inside baseUploadPath
        if (!targetDir.startsWith(baseUploadPath)) {
            throw new IOException("Invalid storage path (outside configured upload-dir).");
        }

        // Create directories if they don't exist
        Files.createDirectories(targetDir);

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String safeName = sanitizeFileName(original);

            // Truncate file name if too long (preserve extension)
            if (safeName.length() > MAX_FILENAME_LENGTH) {
                String ext = "";
                int dot = safeName.lastIndexOf('.');
                if (dot >= 0) {
                    ext = safeName.substring(dot);
                    safeName = safeName.substring(0, Math.min(safeName.length(), MAX_FILENAME_LENGTH - ext.length())) + ext;
                } else {
                    safeName = safeName.substring(0, MAX_FILENAME_LENGTH);
                }
            }

            String uniquePrefix = String.valueOf(System.currentTimeMillis());
            String storedFileName = uniquePrefix + "_" + safeName;

            Path destination = targetDir.resolve(storedFileName).normalize();

            // Extra security check
            if (!destination.startsWith(baseUploadPath)) {
                throw new IOException("Invalid destination path.");
            }

            // Copy file to destination (replace if exists)
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            // store relative path for DB, e.g. "correspondence/OUTGOING/12345/163024234_filename.pdf"
            Path relative = baseUploadPath.relativize(destination);
            storedRelativePaths.add(relative.toString().replace("\\", "/"));
        }

        return storedRelativePaths;
    }

    /**
     * Sanitizes filenames by removing path separators, collapsing spaces,
     * replacing non-alphanumeric (except . _ -) with underscores, and preventing
     * multiple consecutive dots.
     */
    private String sanitizeFileName(String filename) {
        String name = filename.replaceAll("[\\\\/]+", "_")           // remove slashes/backslashes
                .replaceAll("[\\s]+", "_")             // collapse whitespace
                .replaceAll("[^A-Za-z0-9._-]", "_");   // allow only safe chars
        name = name.replaceAll("\\.{2,}", ".");                      // collapse multiple dots
        if (name.startsWith(".")) name = "file" + name;              // avoid starting with dot
        return name;
    }
}