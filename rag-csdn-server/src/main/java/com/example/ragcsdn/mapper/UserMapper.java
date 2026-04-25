package com.example.ragcsdn.mapper;

import com.example.ragcsdn.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 更新用户 CSDN 登录态
     */
    int updateCsdnCookie(@Param("id") Long id,
                         @Param("csdnCookieEncrypted") String csdnCookieEncrypted,
                         @Param("csdnCookieUpdateTime") LocalDateTime csdnCookieUpdateTime);

    /**
     * 删除用户
     */
    int deleteById(@Param("id") Long id);
}

