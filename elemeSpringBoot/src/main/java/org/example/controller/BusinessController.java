package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.entity.Business;
import org.example.response.BusinessResponse;
import org.example.service.IBusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
//API子路径
@RequestMapping("/business")
//标签
@Tag(name = "商家服务接口")
//跨域
@CrossOrigin(origins = "*" )
public class BusinessController {
    //资源
    @Resource
    private IBusinessService businessService;

    @PostMapping("/getAll")
    public ResponseEntity<List<Business>> getAll() {
        List<Business> businessList = businessService.getAll();
        if (businessList != null && !businessList.isEmpty()) {
            for (Business business : businessList) {
                // 调用 setDiscounts 方法将 discounts 字符串转换为列表
                business.setDiscounts(business.getDiscounts());
            }
            return new ResponseEntity<>(businessList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/getBusinessById")
    public BusinessResponse getBusinessById(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("ID");
        Business business = businessService.findBusinessById(id);
        if (business == null) {
            return BusinessResponse.error();
        }
        System.out.println("Returning business: " + business);
        return BusinessResponse.success(business);
    }

}
