package com.example.capstone.Services;

import com.example.capstone.Models.User;
import com.example.capstone.Models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.List;

@Service
public class EmailServices {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;


    public void register(User user) throws MessagingException, UnsupportedEncodingException {
        Random r = new Random();
        int n = r.nextInt();
        String code = Integer.toHexString(n);
        user.setVerifyotp(code);
        userRepository.save(user);
        sendVerificationEmail(user,code);
    }

    private void sendVerificationEmail(User user,String code) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "chindrasuKudumbam@gmail.com";
        String senderName = "Capstone";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br><br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">Click Me To Verify</a></h3><br><br>"
                + "Thank you,<br>"
                + "Capstone";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        content = content.replace("[[name]]", user.getFirstname()+" "+user.getLastname());
        String verifyURL = "http://127.0.0.1:8080/api/verify?code=" + code+"-"+user.getEmail();
        content = content.replace("[[URL]]", verifyURL);
        helper.setText(content, true);
        mailSender.send(message);
    }

    public void forgotPassword(User user)   throws MessagingException,UnsupportedEncodingException{
        Random r = new Random();
        int n = r.nextInt();
        String code = Integer.toHexString(n);
        user.setVerifyotp(code);
        userRepository.save(user);
        sendForgotMail(user,code);
    }
    private void sendForgotMail(User user,String code) throws MessagingException,UnsupportedEncodingException{
        String toAddress = user.getEmail();
        String fromAddress = "chindrasuKudumbam@gmail.com";
        String senderName = "Capstone";
        String subject = "Forgot Password";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to change Your Password:<br><br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">Click Me To Change Password</a></h3><br><br>"
                + "Thank you,<br>"
                + "Capstone";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        content = content.replace("[[name]]", user.getFirstname()+" "+user.getLastname());
        String verifyURL = "http://127.0.0.1:8080/api/auth/forgot/verify?code=" + code+"-"+user.getEmail();
        content = content.replace("[[URL]]", verifyURL);
        helper.setText(content, true);
        mailSender.send(message);
    }
    public void resetPassword(User user)   throws MessagingException,UnsupportedEncodingException{
        Random r = new Random();
        int n = r.nextInt();
        String code = Integer.toHexString(n);
        user.setVerifyotp(code);
        userRepository.save(user);
        sendResetMail(user,code);
    }

    private void sendResetMail(User user,String code) throws MessagingException,UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "chindrasuKudumbam@gmail.com";
        String senderName = "Capstone";
        String subject = "Reset Password";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to Reset Your Password:<br><br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">Click Me To Change Password</a></h3><br><br>"
                + "Thank you,<br>"
                + "Capstone";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        content = content.replace("[[name]]", user.getFirstname() + " " + user.getLastname());
        String verifyURL = "http://127.0.0.1:8080/api/auth/reset/verify?code=" + code + "-" + user.getEmail();
        content = content.replace("[[URL]]", verifyURL);
        helper.setText(content, true);
        mailSender.send(message);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void mailNewsLetter() throws MessagingException,UnsupportedEncodingException{
        String toAddress = "";
        String fromAddress = "chindrasuKudumbam@gmail.com";
        String senderName = "Capstone";
        String subject = "Month end Sale !!! 50 OFF !!!";
        String content = "Dear [[name]],<br>"
                + "News Letters Here !!!";
        List<User> allUser =  userRepository.findAll();
        for(User user:allUser){
            if(user.getEnabled()) {
                toAddress = user.getEmail();
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message);
                helper.setFrom(fromAddress, senderName);
                helper.setTo(toAddress);
                helper.setSubject(subject);
                content = content.replace("[[name]]", user.getFirstname() + " " + user.getLastname());
                helper.setText(content, true);
                mailSender.send(message);
            }
        }
    }
}
