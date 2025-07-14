package org.example.dto;

import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;

@Data
public class OrderDetailDTO {
    private Integer id;
    private Integer businessID;
    private String userPhone;
    private String orderList;
    private Double price;
    private Integer state;
    private LocalDateTime createdAt;
}


