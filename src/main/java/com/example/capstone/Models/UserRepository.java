package com.example.capstone.Models;

import com.example.capstone.Models.User;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
@Repository
@CacheConfig(cacheNames = {"User"})
public interface UserRepository extends MongoRepository<User,String> {

    @Cacheable(key="#s")
    public default Optional<User> findOneById(String s){
        return findById(s);
    }
}
