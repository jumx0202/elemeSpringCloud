package org.example.response;

import lombok.Data;
import org.example.entity.Business;
import org.example.entity.Food;

import java.util.List;

@Data
public class BusinessResponse {
    private Integer code;
    private String msg;
    private Business data;

    public BusinessResponse() {
    }

    public static BusinessResponse success(Business business) {
        BusinessResponse response = new BusinessResponse();
        response.setCode(200);
        response.setMsg("success");
        response.setData(business);
        return response;
    }

    public static BusinessResponse error() {
        BusinessResponse response = new BusinessResponse();
        response.setCode(404);
        response.setMsg("商家不存在");
        return response;
    }
} 