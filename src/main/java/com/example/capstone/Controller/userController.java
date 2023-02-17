package com.example.capstone.Controller;

import com.example.capstone.DTO.userDto;
import com.example.capstone.Models.AuthenticationResponse;
import com.example.capstone.Models.User;
import com.example.capstone.Models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping( "/api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class userController {
    @Autowired
    UserRepository userRepository;
    @PutMapping("/update")
    public ResponseEntity<?> profileUpdate(@Valid @RequestBody userDto userDto, BindingResult bindingResult){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userRepository.existsById(email)){
            Optional<User> user = userRepository.findOneById(email);
            if (bindingResult.getAllErrors().isEmpty()) {
                user.get().setFirstname(userDto.getFirstname());
                user.get().setLastname(userDto.getLastname());
                user.get().setDob(userDto.getDob());
                user.get().setGender(userDto.getGender());
                userRepository.save(user.get());
                return ResponseEntity
                        .ok(new AuthenticationResponse("User Has been Updated"));
            }
            List<ObjectError> error = bindingResult.getAllErrors();
            List<String> listError = new ArrayList<>();
            for (ObjectError i : error) {
                listError.add(i.getDefaultMessage());
            }
            HashMap<String, List> errorMessage = new HashMap<>();
            errorMessage.put("Validation Error", listError);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("User not Found"));
    }

    @GetMapping("/requestList")
    public ResponseEntity<?> requestList(){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findOneById(myEmail);
        if(!user.get().getFriendrequest().isEmpty()){
            return ResponseEntity.ok(user.get().getFriendrequest());
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthenticationResponse("Friends List is Empty"));
        }
    }


    @GetMapping("/myFriends")
    public ResponseEntity<?> MyFriendsList(){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findOneById(myEmail);
        if(!user.get().getFriends().isEmpty()){
            return ResponseEntity.ok(user.get().getFriends());
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthenticationResponse("Friends List is Empty"));
        }
    }

    @GetMapping("/unfollow/{id}")
    public ResponseEntity<?> unfollowFirend(@PathVariable String id){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userRepository.existsById(myEmail) && userRepository.existsById(id)){
            Optional<User> myUser = userRepository.findOneById(myEmail);
            Optional<User> thatUser = userRepository.findOneById(id);
            if(myUser.get().getFriends().contains(id) && thatUser.get().getFriends().contains(myEmail)){
                myUser.get().getFriends().remove(id);
                thatUser.get().getFriends().remove(myEmail);
                userRepository.save(myUser.get());
                userRepository.save(thatUser.get());
                return ResponseEntity
                        .ok(new AuthenticationResponse("Friend has been Removed, For nexttime Follow you need to give a Request"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthenticationResponse("Friend not in the List"));
            }
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthenticationResponse("User not Found"));
        }

    }

    @GetMapping("/accept/{id}")
    public ResponseEntity<?> acceptRequest(@PathVariable String id){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userRepository.existsById(myEmail) && userRepository.existsById(id)){
            Optional<User> myUser = userRepository.findOneById(myEmail);
            Optional<User> thatUser = userRepository.findOneById(id);
            if(myUser.get().getFriendrequest().contains(id)){
                myUser.get().getFriends().add(id);
                ArrayList<String> myUserFrdReq = myUser.get().getFriendrequest();
                myUserFrdReq.remove(id);
                thatUser.get().getFriends().add(myEmail);
                userRepository.save(thatUser.get());
                userRepository.save(myUser.get());
                return ResponseEntity
                        .ok(new AuthenticationResponse("Friend Request has been Accepted"));
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthenticationResponse("Friend Request not Found"));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthenticationResponse("User not Found"));
    }

    @GetMapping("/follow/{id}")
    public ResponseEntity<?> followUser(@PathVariable String id){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userRepository.existsById(id)){
            Optional<User> user = userRepository.findOneById(id);
            if(user.get().getFriends().contains(myEmail)){
                return ResponseEntity.ok(new AuthenticationResponse("You are already friend with each other"));
            }
            user.get().getFriendrequest().add(myEmail);
            userRepository.save(user.get());
            return ResponseEntity.ok(new AuthenticationResponse("Friend Request has been sent to " + id));
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthenticationResponse("Request User not Found"));
        }
    }

    @GetMapping("/reject/{id}")
    public ResponseEntity<?> rejectUser(@PathVariable String id){
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findOneById(myEmail);
        if(user.get().getFriendrequest().contains(id)){
            user.get().getFriendrequest().remove(id);
            userRepository.save(user.get());
            return ResponseEntity.ok(new AuthenticationResponse("Friend request has been rejected"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Request not found"));
    }
}
