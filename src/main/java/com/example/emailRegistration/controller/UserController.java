package com.example.emailRegistration.controller;


import com.example.emailRegistration.dto.UserRequest;
import com.example.emailRegistration.entity.Address;
import com.example.emailRegistration.entity.User;
import com.example.emailRegistration.repository.UserRepository;
import com.example.emailRegistration.service.EmailService;
import com.example.emailRegistration.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/maa_kulam")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired private UserRepository userRepository;
    //@Autowired private AddressRepository addressRepository;
    @Autowired private OtpService otpService;
    @Autowired private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        User user = new User();
        user.setEmail(email);
        user.setStatus("PENDING");
        userRepository.save(user);
        System.out.println("User created with email: " + email + " and status PENDING");
        String otp = otpService.generateOtp(email);
        System.out.println("OTP generated: " + email);
        emailService.sendOtp(email, otp);
        System.out.println("OTP sent to: " + email);
        return ResponseEntity.ok("OTP sent. Please verify it");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        System.out.println("VerifyOtp called: " + email);
        if(otpService.verifyOtp(email, otp)) {
            System.out.println("OTP verified successfully: " + email);
            User user = userRepository.findByEmail(email);
            user.setStatus("VERIFIED");
            userRepository.save(user);
            System.out.println("User status updated to VERIFIED for: " + email);
            return ResponseEntity.ok("OTP verified and Status set to VERIFIED.");
        }
        return ResponseEntity.status(400).body("Invalid OTP or TimeOut");
    }

    @PostMapping(value = "/create-user", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createUser(
            @RequestParam("user") String userJson,
            @RequestPart("pdf") MultipartFile pdfFile) {

        System.out.println("createUser is called with Json File: " + userJson);
        ObjectMapper mapper = new ObjectMapper();
        UserRequest userRequest;
        try {
            //Deserialize Json into Java objects
            userRequest = mapper.readValue(userJson, UserRequest.class);
            System.out.println("User JSON parsed successfully: " + userRequest.getEmail());
        } catch (Exception e) {
            System.out.println("Failed to parse user JSON: " + e.getMessage());
            return ResponseEntity.status(400).body("Invalid JSON: " + e.getMessage());
        }

        User user = userRepository.findByEmail(userRequest.getEmail());
        if (user != null && "VERIFIED".equals(user.getStatus())) {
            System.out.println("Updating the verified user: " + user.getEmail());
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setGender(userRequest.getGender());
            user.setCaste(userRequest.getCaste());
            user.setMobileNumber(userRequest.getMobileNumber());
            user.setAnnualIncome(userRequest.getAnnualIncome());
            user.setStatus("VALIDATED");

            try {
                user.setPdfFile(pdfFile.getBytes());
                System.out.println("PDF uploaded and pointed to: " + user.getEmail());
            } catch (IOException e) {
                System.out.println("Failed to read PDF file for: " + user.getEmail() + ", error: " + e.getMessage());
                return ResponseEntity.status(500).body("Failed to read PDF file.");
            }

            Address address = new Address();
            address.setStreet(userRequest.getStreet());
            address.setCity(userRequest.getCity());
            address.setState(userRequest.getState());
            address.setCountry(userRequest.getCountry());
            //Creating bi-directional relation
            address.setUser(user);
            user.setAddress(address);
            System.out.println("Address also updated: " + user.getEmail());

            userRepository.save(user);
            System.out.println("User saved successfully for: " + user.getEmail());
            return ResponseEntity.ok("User created with PDF");
        }
        System.out.println("User not verified or does not exist for email: " + userRequest.getEmail());
        return ResponseEntity.status(400).body("User not verified or does not exist.");
    }

    @GetMapping("/user/{id}/pdf")
    public ResponseEntity<?> getUserPdf(@PathVariable Long id) {
        System.out.println("getUserPdf called with user id: " + id);
        User user = userRepository.findById(id).orElse(null);
        if (user == null || user.getPdfFile() == null) {
            System.out.println("User not found and no PDF available " + id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found with the given ID.");
        }

        byte[] pdfContent = user.getPdfFile();
        System.out.println("PDF file uploaded for: " + id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("inline").filename("meradetails.pdf").build());
        System.out.println("Returning PDF file for id: " + id);
//        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("my_details.pdf").build());
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }


    //Implementation before Soft/Hard Delete methods. Can Uncomment to make it normal
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("User with ID " + id + " deleted successfully.");
    }

    @DeleteMapping("/delete-all-users")
    public ResponseEntity<?> deleteAllUsers() {
        userRepository.deleteAll();
        System.out.println("Initiated deletion command");
        return ResponseEntity.ok("All users khatham.");
    }


//    @DeleteMapping("/soft-delete/{id}")
//    public ResponseEntity<?> softDeleteUser(@PathVariable Long id) {
//        emailService.softDeleteUser(id);  // Calls service method
//        return ResponseEntity.ok("User soft deleted.");
//    }
//
//    @DeleteMapping("/soft-delete-all")
//    public ResponseEntity<?> softDeleteAllUsers() {
//        emailService.softDeleteAllUsers();
//        return ResponseEntity.ok("All users soft deleted successfully.");
//    }


//    @DeleteMapping("/hard-delete/{id}")
//    public ResponseEntity<?> hardDeleteUser(@PathVariable Long id) {
//        hardDeleteUser(id);  // Hard delete logic
//        return ResponseEntity.ok("User hard deleted.");
//    }
//
//        @DeleteMapping("/delete-all-users")
//    public ResponseEntity<?> hardDeleteAllUsers() {
//        userRepository.deleteAll();
//        return ResponseEntity.ok("All users deleted successfully.");
//    }

}
