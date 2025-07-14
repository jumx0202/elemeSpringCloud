package org.example.service;

import org.example.entity.User;

public interface IUserService {
    User login(String phoneNumber, String password);
    Integer register(String phoneNumber, String password, String confirmPassword, String name, String email);
}
