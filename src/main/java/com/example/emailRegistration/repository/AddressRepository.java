package com.example.emailRegistration.repository;


import com.example.emailRegistration.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}