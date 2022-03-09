package com.snofty.learnSpringRective.repository;

import com.snofty.learnSpringRective.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<User, String> {
    public User findByName(String name);
}
