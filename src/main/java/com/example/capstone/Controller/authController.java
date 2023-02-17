package com.example.capstone.Controller;
import com.example.capstone.Jwt.JwtTokenUtil;
import com.example.capstone.Models.AuthenticationRequest;
import com.example.capstone.Models.AuthenticationResponse;
import com.example.capstone.Models.User;
import com.example.capstone.Models.UserRepository;
import com.example.capstone.Services.EmailServices;
import com.example.capstone.Services.PasswordDecrption;
import com.example.capstone.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.*;


@RestController
@RequestMapping("/api")
public class authController {
    @Autowired UserRepository userRepository;
    @Autowired EmailServices emailServices;
    @Autowired AuthenticationManager authenticationManager;
    @Autowired JwtTokenUtil jwtTokenUtil;
    @Autowired UserService userService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired PasswordDecrption passwordDecrption;
    @PostMapping("/signin")
    public ResponseEntity<?> signIN(@Valid @RequestBody User user, BindingResult bindingResult)
            throws MessagingException, UnsupportedEncodingException {
        if(!userRepository.existsById(user.getEmail())) {
            if (bindingResult.getAllErrors().isEmpty()) {
                String decrptPass = passwordDecrption.Decryption(user.getPassword());
                user.setProvider("LOCAL");
                user.setPassword(passwordEncoder.encode(decrptPass));
                userRepository.save(user);
                emailServices.register(user);
                return ResponseEntity
                        .ok(new AuthenticationResponse("Verification Link Has Been Send to " + user.getEmail() + ", Verify to activate Account !!!"));
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
        else{
            Optional<User> userDetail = userRepository.findById(user.getEmail());
            if(userDetail.get().getEnabled()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthenticationResponse("Email Already Exists Use Different mail"));}
            else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthenticationResponse("Email Already Waiting for Verification , Pls check mail"));
            }
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String code){
        String[] paramList = code.split("-");
        String verifyCode = paramList[0];
        String email = paramList[1];
        Optional<User> user = userRepository.findById(email);
        if(user.get().getVerifyotp().equals(verifyCode) && user.get().getVerifyotp()!=null){
            user.get().setEnabled(true);
            user.get().setVerifyotp(null);
            userRepository.save(user.get());
            return ResponseEntity.ok(new AuthenticationResponse("Account has been Verified Succesffully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthenticationResponse("Invalid Credx`entials"));
    }

    @PostMapping("/auth")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> userLogin(@RequestBody AuthenticationRequest authenticationRequest){
        String emailId = authenticationRequest.getEmail();
        String encrppassword = authenticationRequest.getPassword();
        String password = passwordDecrption.Decryption(encrppassword);
        if(userRepository.existsById(emailId)) {
            Optional<User> user = userRepository.findById(emailId);
            if (user.get().getEnabled()) {
                try {
                    authenticationManager
                            .authenticate(new UsernamePasswordAuthenticationToken(emailId, password));
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new AuthenticationResponse("Invalid Credentials"));
                }
                final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getEmail());
                final String jwt = jwtTokenUtil.generateToken(userDetails);
                return ResponseEntity.ok(new AuthenticationResponse(jwt));
            }else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthenticationResponse("Account is not Enabled"));
            }
        }else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new AuthenticationResponse("Account Not found , Please create an account"));
        }
    }

    @GetMapping("/auth/forgot/{email}")
    public ResponseEntity<?> forgotPas(@PathVariable String email) throws MessagingException, UnsupportedEncodingException {
        if(userRepository.existsById(email)) {
            Optional<User> user = userRepository.findById(email);
            emailServices.forgotPassword(user.get());
            return ResponseEntity.ok(new AuthenticationResponse("Forgot Password link has been Sent to " + email));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse("Email not found"));
    }

    @PostMapping("/auth/forgot/verify")
    public ResponseEntity<?> forgotPasswordLink(@RequestBody HashMap<String,String> forgotUser,@RequestParam String code) {
        String[] paramList = code.split("-");
        String verifyCode = paramList[0];
        String emailId = paramList[1];
        try {
            Optional<User> user = userRepository.findById(emailId);
            if (user.get().getVerifyotp() != null && user.get().getVerifyotp().equals(verifyCode)) {
                if (forgotUser.get("newPassword").equals(forgotUser.get("reNewPassword"))) {
                    user.get().setPassword(passwordEncoder.encode(forgotUser.get("newPassword")));
                    user.get().setVerifyotp(null);
                    userRepository.save(user.get());
                    return ResponseEntity.ok(new AuthenticationResponse("Password has been changed successfully"));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new AuthenticationResponse("Confirm Password Did not Mathch"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new AuthenticationResponse("Verification is not Valid"));
            }
        }
        catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Verification");
        }
    }

    @GetMapping("/oauthsuces/{jwt}")
    public AuthenticationResponse oauthSuccessfulLogin(@PathVariable String jwt){
        return new AuthenticationResponse(jwt);
    }

    @GetMapping("/oauthUnsuces")
    public String oauthUnsuccessfulLogin(){
        return "<h1>Oauth Login failed</h1>";
    }

}

