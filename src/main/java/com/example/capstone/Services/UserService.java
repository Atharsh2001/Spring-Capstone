package com.example.capstone.Services;

import com.example.capstone.Models.User;
import com.example.capstone.Models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"UsrLogin"})
public class UserService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Override
    @Cacheable(key = "#email")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> foundUser = userRepository.findById(email);
        if(foundUser.isEmpty()){
            return null;
        }
        String emailId = foundUser.get().getEmail();
        String password = foundUser.get().getPassword();
        return new org.springframework.security.core.userdetails.User(emailId,password,new ArrayList<>());
    }

    public void processOAuthPostLogin(String email,String fullname,String provider) {
        if(provider.equals("GOOGLE")) {
            String[] allname = fullname.split(" ");
            if (!userRepository.existsById(email)) {
                User newUser = new User(email, allname[0], allname[1], "123456789", "null", "null");
                newUser.setProvider("GOOGLE");
                newUser.setEnabled(true);
                userRepository.save(newUser);
            }
        }
        if (!userRepository.existsById(email)) {
            User newUser = new User(email, fullname, "null", "123456789", "null", "null");
            newUser.setProvider("FACEBOOK");
            newUser.setEnabled(true);
            userRepository.save(newUser);
        }
    }
}
