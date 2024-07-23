package com.springboot.restcontroller.Exception;

public class StudentNotFoundException extends Exception {
    public StudentNotFoundException(Integer id) {
        super("Could not find student with id " + id);
    }
}
