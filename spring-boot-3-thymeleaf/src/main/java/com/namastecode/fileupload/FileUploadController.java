package com.namastecode.fileupload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileUploadController {

    @Value("${files.upload-dir}")
    private String uploadDir;

    @GetMapping("/")
    public String fileUpload() {
        return "file-upload";
    }

    @PostMapping("/upload")
    public String handleUpload(
            @RequestParam("file") MultipartFile file,
            Model model
    ) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file");
            return "file-upload";
        }
        try {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            Path destination = Paths.get(uploadDir, filename);
            Files.createDirectories(destination.getParent());
            file.transferTo(destination);
            model.addAttribute("message", "Success: " + filename + " (" + file.getSize() + " bytes)");
            return "file-upload";
        } catch (IOException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            return "file-upload";
        }
    }
}