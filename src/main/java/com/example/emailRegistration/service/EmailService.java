package com.example.emailRegistration.service;


import com.example.emailRegistration.dto.UserRequest;
import com.example.emailRegistration.entity.User;
import com.example.emailRegistration.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;


    public String sendOtp(String email, String otp) {
        String subject = "OTP for Email verification";
        String message = """
            Hi,<br>
            Thank you for showing interest in our service.<br>
            Please use the following One Time Password to verify your login and create your profile.<br>
            <b>OTP: </b> """ + otp;
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(message, true);
            mailSender.send(mimeMessage);
            return "OTP email sent successfully.";
        } catch (MessagingException e) {
            return "Failed to send OTP email: " + e.getMessage();
        }
    }


//    public void sendOtp(String to, String otp) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("OTP for registration");
//        message.setText("Your OTP is: " + otp);
//        mailSender.send(message);
//    }

//    // Soft delete by ID
//    public void softDeleteUser(Long id) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Comment this out when you want hard delete
//        user.setDeleted(true);
//        userRepository.save(user);
//    }
//
//    // Soft delete all users
//    public void softDeleteAllUsers() {
//        List<User> users = userRepository.findAll();
//        for (User user : users) {
//            user.setDeleted(true);
//        }
//        userRepository.saveAll(users);
//    }



//    // Hard delete by ID
//    public void hardDeleteUser(Long id) {
//        // Comment this out when you want soft delete
//        userRepository.deleteById(id);
//    }
//
//    //Hard delete all users
//    public void hardDeleteAllUsers() {
//        userRepository.deleteAll();
//    }

}
