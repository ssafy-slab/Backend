package com.ssafy.ssafy_slap.user.mapper;

import com.ssafy.ssafy_slap.user.domain.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    boolean existsActiveByEmail(@Param("email") String email);

    Optional<AppUser> findActiveByEmail(@Param("email") String email);

    Optional<AppUser> findActiveById(@Param("userId") Long userId);

    void insertLocalUser(@Param("user") AppUser user);

    void updateNickname(
            @Param("userId") Long userId,
            @Param("nickname") String nickname
    );

    void softDelete(@Param("userId") Long userId);
}
