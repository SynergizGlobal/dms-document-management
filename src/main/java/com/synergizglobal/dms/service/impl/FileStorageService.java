package com.synergizglobal.dms.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
	
	
	@Value("${file.upload-dir}") 
	private String uploadDir;
	
	public List<String> saveFiles(List<MultipartFile> files) throws IOException {
        List<String> storedFilePaths = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            throw new IOException("No files to save!");
        }

        // Ensure the upload directory exists
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue; // skip empty file
            }

            // Generate a unique file name
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // Resolve the full file path
            Path filePath = uploadPath.resolve(fileName);

            // Save the file to the target location (replace if already exists)
            Files.copy(file.getInputStream(), filePath);

            storedFilePaths.add(filePath.toString());
        }

        return storedFilePaths;
    }
    
}
