package com.bhavya.documentworkflowplatform.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    private final Path uploadDirectory =
            Paths.get("uploads");

    public FileStorageService() throws IOException {

        if (!Files.exists(uploadDirectory)) {
            Files.createDirectories(uploadDirectory);
        }
    }

    public String saveFile(MultipartFile file) throws IOException {

        String fileName = System.currentTimeMillis()
                + "_" + file.getOriginalFilename();

        Path filePath = uploadDirectory.resolve(fileName);

        Files.copy(file.getInputStream(), filePath);

        return filePath.toString();
    }
}
