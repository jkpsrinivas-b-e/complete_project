package com.example.emailRegistration.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;


    public UploadResult uploadFiles(MultipartFile pdf, List<MultipartFile> images) throws IOException {
        if (images.size() > 4) {
            throw new IllegalArgumentException("Maximum 4 images allowed");
        }

        //upload pdf
        String pdfKey = "pdfs/" + System.currentTimeMillis() + "_" + pdf.getOriginalFilename();
        uploadFileToS3(pdfKey, pdf);

        // Upload image
        List<String> imageKeys = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            String imageKey = "images/" + System.currentTimeMillis() + "_" + i + "_" + image.getOriginalFilename();
            uploadFileToS3(imageKey, image);
            imageKeys.add(imageKey);
        }

        UploadResult result = new UploadResult();
        result.setPdfKey(pdfKey);
        result.setImageKeysCsv(String.join(",", imageKeys));
        return result;
    }

    private void uploadFileToS3(String key, MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileBytes.length);
        metadata.setContentType(file.getContentType());
        amazonS3.putObject(bucketName, key, new ByteArrayInputStream(fileBytes), metadata);
    }


    public static class UploadResult {
        private String pdfKey;
        private String imageKeysCsv;

        public String getPdfKey() {
            return pdfKey;
        }

        public void setPdfKey(String pdfKey) {
            this.pdfKey = pdfKey;
        }

        public String getImageKeysCsv() {
            return imageKeysCsv;
        }

        public void setImageKeysCsv(String imageKeysCsv) {
            this.imageKeysCsv = imageKeysCsv;
        }
    }
}
