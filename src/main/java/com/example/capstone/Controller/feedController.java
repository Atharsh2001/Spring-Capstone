package com.example.capstone.Controller;

import com.example.capstone.Models.*;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/feed")
@CrossOrigin(origins = "http://localhost:3000")
public class feedController {
    @Autowired
    UserFeedRepository userFeedRepository;
    @Autowired
    UserRepository userRepository;
    @GetMapping("/myfeed")
    public ResponseEntity<?> myFeed(){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!userFeedRepository.findByEmail(myEmail).isEmpty()){
            return ResponseEntity.ok(userFeedRepository.findByEmail(myEmail));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Feed is empty");
    }

    @GetMapping("/myfeed/{id}")
    public ResponseEntity<?> myFeedById(@PathVariable String id){
        if(userFeedRepository.existsById(id)){
            return ResponseEntity.ok(userFeedRepository.findByOneId(id).get());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Id not found"));
    }

    @PostMapping("/newpost")
    public ResponseEntity<?> newPost(@RequestBody UserFeed userFeed){
        if(userRepository.existsById(userFeed.getEmail())
                && userFeed.getEmail().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            userFeedRepository.save(userFeed);
            return ResponseEntity.ok(new AuthenticationResponse("Feed uploaded successfuly"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Email not valid"));
    }

    @GetMapping("/view/friend")
    public List<UserFeed> viewFriendsPost(){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findById(myEmail);
        ArrayList<String> myFrdlist = user.get().getFriends();
        List<UserFeed> allFrdFeeds = new ArrayList<>();
        for(String frds:myFrdlist) {
            ArrayList<UserFeed> frdsPost = (ArrayList<UserFeed>) userFeedRepository.findByEmailAndVisiblity(frds, "friends");
            allFrdFeeds.addAll(frdsPost);
        }
        return allFrdFeeds;
    }

    @GetMapping("/view/public")
    public List<UserFeed> viewPublicPost(){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userFeedRepository.findByEmailAndVisiblity(myEmail,"public");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePost(@PathVariable String id){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userFeedRepository.existsById(id)
                && userFeedRepository.findById(id).get().getEmail().equals(myEmail)){
            Optional<UserFeed> userFeed = userFeedRepository.findById(id);
            userFeed.get().setAvailable(false);
            userFeedRepository.save(userFeed.get());
            return ResponseEntity.ok(new AuthenticationResponse("Post has been Deleted , You can see it in Archive"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Id not found"));
    }

    @GetMapping("/archive")
    public ResponseEntity<?> myArchive(){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<UserFeed> myFeed = new ArrayList<>();
        List<UserFeed> allFeed = userFeedRepository.findAll();
        for(UserFeed i:allFeed){
            if(i.getEmail().equals(myEmail) && !i.isAvailable()){
                myFeed.add(i);
            }
        }
        if(myFeed.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthenticationResponse("Your archive is empty"));
        }else {
            return ResponseEntity.ok(myFeed);
        }
    }

    @GetMapping("/archive/restore/{id}")
    public ResponseEntity<?> restoreArchive(@PathVariable String id){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userFeedRepository.existsById(id)
                && userFeedRepository.findById(id).get().getEmail().equals(myEmail)){
            Optional<UserFeed> userFeed = userFeedRepository.findById(id);
            userFeed.get().setAvailable(true);
            userFeedRepository.save(userFeed.get());
            return ResponseEntity.ok(new AuthenticationResponse("Archive post has been Restored"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Post Id not found"));
    }

    @GetMapping("/export")
    public void exportCsv(HttpServletResponse response) throws Exception{
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<UserFeed> myFeed = userFeedRepository.findByEmail(myEmail);
        String filename = myEmail+".csv";
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"");
        StatefulBeanToCsv<UserFeed> writer = new StatefulBeanToCsvBuilder<UserFeed>(response.getWriter())
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withOrderedResults(false)
                .build();
        writer.write(myFeed);

    }
}
