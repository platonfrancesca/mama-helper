package com.example.mamahelper.repository;

import com.example.mamahelper.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildRepository extends JpaRepository<Child, Long> {
}
