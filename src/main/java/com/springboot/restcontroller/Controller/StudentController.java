package com.springboot.restcontroller.Controller;

import com.springboot.restcontroller.Exception.StudentNotFoundException;
import com.springboot.restcontroller.Model.Student;
import com.springboot.restcontroller.Repository.StudentRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class StudentController {

    private final StudentRepository repository;

    StudentController(StudentRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/students")
    CollectionModel<EntityModel<Student>> getAllStudents() {
        List<EntityModel<Student>> students = repository.findAll().stream().
                map(student -> EntityModel.of(student,
                        linkTo(methodOn(StudentController.class).getStudent(student.getId())).withSelfRel(),
                        linkTo(methodOn(StudentController.class).getAllStudents()).withRel("students")))
                .collect(Collectors.toList());
        return CollectionModel.of(students, linkTo(methodOn(StudentController.class).getAllStudents()).withSelfRel());
    }

    @PostMapping("/students")
    Student newStudent(@RequestBody Student student) {
        return repository.save(student);
    }

    @GetMapping("/students/{id}")
    EntityModel<Student> getStudent(@PathVariable Integer id) {
        Student student;
        try {
            student = repository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
        } catch (StudentNotFoundException e) {
            throw new RuntimeException(e);
        }
        return EntityModel.of(student,
                linkTo(methodOn(StudentController.class).getStudent(id)).withSelfRel(),
                linkTo(methodOn(StudentController.class).getAllStudents()).withSelfRel());
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
