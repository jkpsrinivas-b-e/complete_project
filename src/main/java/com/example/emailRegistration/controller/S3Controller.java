package com.example.emailRegistration.controller;

import com.example.emailRegistration.entity.User;
import com.example.emailRegistration.repository.UserRepository;
import com.example.emailRegistration.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @RequestParam("userId") Long userId,
            @RequestParam("pdf") MultipartFile pdf,
            @RequestParam("images") List<MultipartFile> images) {

        if (pdf == null || pdf.isEmpty()) {
            return ResponseEntity.badRequest().body("PDF file is required.");
        }

        if (images == null || images.size() > 4) {
            return ResponseEntity.badRequest().body("Maximum 4 images are allowed.");
        }

        try {
            S3Service.UploadResult uploadResult = s3Service.uploadFiles(pdf, images);

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found.");
            }

            user.setS3Pdf(uploadResult.getPdfKey());
            user.setS3Images(uploadResult.getImageKeysCsv());
            userRepository.save(user);

            return ResponseEntity.ok("Files uploaded and keys saved for user ID " + userId);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading files to S3: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
