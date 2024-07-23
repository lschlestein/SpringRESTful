package com.springboot.restcontroller.Controller;

import com.springboot.restcontroller.Exception.StudentNotFoundException;
import com.springboot.restcontroller.Model.Student;
import com.springboot.restcontroller.Repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentController {

    private final StudentRepository repository;

    StudentController(StudentRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/students")
    List<Student> all() {
        return repository.findAll();
    }

    @PostMapping("/students")
    Student newStudent(@RequestBody Student student) {
        return repository.save(student);
    }

    @GetMapping("/students/{id}")
    Student getStudent(@PathVariable Integer id) throws StudentNotFoundException {
        return repository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
    }

    @PutMapping("/students/{id}")
    Student updateStudent(@PathVariable Integer id, @RequestBody Student newStudent) throws StudentNotFoundException {
        return repository.findById(id).map(student -> {
            student.setName(newStudent.getName());
            student.setCourse(newStudent.getCourse());
            student.setEmail(newStudent.getEmail());
            return repository.save(student);
        }).orElseGet(() -> {
            return repository.save(newStudent);
        });
    }

    @DeleteMapping("/students/{id}")
    void deleteStudent(@PathVariable Integer id) {
        repository.deleteById(id);
    }
}
