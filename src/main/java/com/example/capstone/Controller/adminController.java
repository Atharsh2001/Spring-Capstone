package com.example.capstone.Controller;

import com.example.capstone.Models.AuthenticationResponse;
import com.example.capstone.Models.User;
import com.example.capstone.Models.UserRepository;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Optional;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class adminController {
    @Autowired
    UserRepository userRepository;
    @RequestMapping(value = "/categories")
    public ResponseEntity<?> allCategories(){
            Optional<User> admin = userRepository.findOneById("admin@capstone.in");
            return ResponseEntity.ok(admin.get().getCategories());
    }

    @PutMapping("/category/{item}")
    public ResponseEntity<?> addCategorie(@PathVariable String item) {
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userRepository.findOneById(myEmail).get().getRole().equals("ADMIN")) {
            Optional<User> admin = userRepository.findOneById(myEmail);
            if(!admin.get().getCategories().contains(item)) {
                admin.get().getCategories().add(item);
                userRepository.save(admin.get());
                return ResponseEntity.ok(new AuthenticationResponse("Category has been added"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("category already exists"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("User not found"));
    }

    @DeleteMapping("/category/{item}")
    public ResponseEntity<?> deleteCategory(@PathVariable String item) {
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userRepository.findOneById(myEmail).get().getRole().equals("ADMIN")) {
            Optional<User> admin = userRepository.findOneById(myEmail);
            if(admin.get().getCategories().contains(item)) {
                admin.get().getCategories().remove(item);
                userRepository.save(admin.get());
                return ResponseEntity.ok(new AuthenticationResponse("Category has been added"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("category not exists"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("User not found"));
    }

    @DeleteMapping("/category/deleteall")
    public ResponseEntity<?> deleteAll(){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userRepository.findOneById(myEmail).get().getRole().equals("ADMIN")) {
            Optional<User> admin = userRepository.findOneById(myEmail);
            admin.get().setCategories(new ArrayList<>());
            userRepository.save(admin.get());
            return ResponseEntity.ok(new AuthenticationResponse("Category has been reset"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("User not found"));
    }

    @PostMapping("/csv/upload")
    public ResponseEntity<?> uploadCsvFile(@RequestBody MultipartFile file){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userRepository.findOneById(myEmail).get().getRole().equals("ADMIN")) {
            Optional<User> admin = userRepository.findOneById(myEmail);
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Csv is Empty"));
            }else {
                try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                    CSVReader csvReader = new CSVReader(reader);
                    String[] nextRecord;
                    while ((nextRecord = csvReader.readNext()) != null) {
                        if(!admin.get().getCategories().contains(nextRecord[0])){
                            admin.get().getCategories().add(nextRecord[0]);
                        }
                    }
                    userRepository.save(admin.get());
                } catch (Exception ex) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Error while reading Csv"));
                }
            }
            return ResponseEntity.ok(new AuthenticationResponse("Categories added succesfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("No Access to Users"));
    }
}
