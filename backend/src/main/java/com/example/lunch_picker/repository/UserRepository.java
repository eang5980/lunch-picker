package com.example.lunch_picker.repository;


import com.example.lunch_picker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {}
