package com.example.capstone.Models;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@CacheConfig(cacheNames = {"Feed"})
public interface UserFeedRepository  extends MongoRepository<UserFeed,String> {

    @Cacheable(key = "#email")
    default Optional<UserFeed> findByOneId(String email){
        return findById(email);
    }

    @Cacheable(key = "#email")
    default List<UserFeed> findByEmail(String email) {
        List<UserFeed> myFeed = new ArrayList<>();
        List<UserFeed> allFeed = findAll();
        for(UserFeed i:allFeed){
            if(i.getEmail().equals(email) && i.isAvailable()){
                myFeed.add(i);
            }
        }
        return myFeed;
    }
    default List<UserFeed> findByEmailAndVisiblity(String email,String visibility) {
        List<UserFeed> myFeed = new ArrayList<>();
        List<UserFeed> allFeed = findAll();

        for (UserFeed i : allFeed) {
            if (visibility.equals("friends")) {
                if (i.getEmail().equals(email) && (i.getVisiblity().equals("friends") || i.getVisiblity().equals("public"))
                && i.isAvailable()) {
                    myFeed.add(i);
                }
            }else if(visibility.equals("public") && i.isAvailable()  && !email.equals(i.getEmail()) && i.getVisiblity().equals("public")) {
                myFeed.add(i);
            }
        }
        return myFeed;
    }
}
