package com.springboot.restcontroller.Repository;

import com.springboot.restcontroller.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}
