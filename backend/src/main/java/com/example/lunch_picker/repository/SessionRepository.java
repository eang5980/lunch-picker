package com.example.lunch_picker.repository;

import com.example.lunch_picker.model.LunchSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<LunchSession, String> {}