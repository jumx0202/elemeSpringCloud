package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.User;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据手机号和密码查询用户
     *
     * @param phoneNumber 手机号
     * @param password 密码
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE phone_number = #{phoneNumber} AND password = #{password} AND status = 1")
    User findByPhoneNumberAndPassword(@Param("phoneNumber") String phoneNumber, 
                                     @Param("password") String password);

    /**
     * 根据手机号查询用户
     *
     * @param phoneNumber 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE phone_number = #{phoneNumber} AND status = 1")
    User findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * 检查手机号是否已存在
     *
     * @param phoneNumber 手机号
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM user WHERE phone_number = #{phoneNumber}")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE email = #{email} AND status = 1")
    User findByEmail(@Param("email") String email);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM user WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);
} 