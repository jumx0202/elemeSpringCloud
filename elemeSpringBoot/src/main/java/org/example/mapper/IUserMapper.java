package org.example.mapper;

import org.example.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserMapper extends CrudRepository<User, String> {
    User findByPhoneNumberAndPassword(String phoneNumber, String password);
    boolean existsByPhoneNumber(String phoneNumber);
}
