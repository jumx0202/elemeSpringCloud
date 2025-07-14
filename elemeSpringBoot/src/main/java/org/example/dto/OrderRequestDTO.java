package org.example.dto;

import lombok.Data;

import java.util.List;
@Data
public class OrderRequestDTO {
    private Integer businessID;
    private String userPhone;
    private List<Integer> orderList;
    private Double price;

}
