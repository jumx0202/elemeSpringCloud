package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user")
@Data  // Lombok 注解，自动生成 getter/setter
public class User {
    @Id
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column
    private String password;
    
    @Column
    private String gender;
    
    @Column
    private String name;
    
    @Column
    private String email;
    
    // 如果不使用 Lombok，需要手动添加 getter/setter
    /*
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ... 其他 getter/setter
    */
}
