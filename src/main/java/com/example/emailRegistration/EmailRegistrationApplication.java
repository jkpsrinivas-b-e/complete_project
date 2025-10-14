package com.example.emailRegistration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@OpenAPIDefinition(
        info = @Info(
                title = "Email Registration API",
                version = "1.0",
                description = "API documentation for email registration, address, and PDF upload"
        )
)

@SpringBootApplication
public class EmailRegistrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailRegistrationApplication.class, args);
	}

}