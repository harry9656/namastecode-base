package com.namastecode.fileupload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            RedirectAttributes redirectAttributes
    ) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file");
            return "redirect:/";
        }
        try {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            Path destination = Paths.get(uploadDir, filename);
            Files.createDirectories(destination.getParent());
            file.transferTo(destination);
            redirectAttributes.addFlashAttribute("message", "Success: " + filename + " (" + file.getSize() + " bytes)");
            return "redirect:/";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "Error: " + e.getMessage());
            return "redirect:/";
        }
    }
}