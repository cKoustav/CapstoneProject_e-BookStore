package com.ebookstore.repository;

import com.ebookstore.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository extends AbstractFileRepository<User, String> {

    public UserRepository(@Value("${app.storage.directory:data}") String storageDir) {
        super(storageDir, "users.json", User.class, User::getId);
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return findAll().stream()
                .filter(u -> username.equalsIgnoreCase(u.getUsername()))
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return findAll().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
