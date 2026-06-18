package com.ssafy.ssafy_slap.user.mapper;

import com.ssafy.ssafy_slap.user.domain.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    boolean existsActiveByEmail(@Param("email") String email);

    boolean existsActiveByNickname(@Param("nickname") String nickname);

    Optional<AppUser> findActiveByEmail(@Param("email") String email);

    Optional<AppUser> findByEmail(@Param("email") String email);

    Optional<AppUser> findActiveById(@Param("userId") Long userId);

    Optional<AppUser> findActiveByOAuthAccount(
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId
    );

    Optional<AppUser> findByOAuthAccount(
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId
    );

    void insertLocalUser(@Param("user") AppUser user);

    void anonymizeDeletedUserEmail(
            @Param("userId") Long userId,
            @Param("deletedEmail") String deletedEmail
    );

    void insertOAuthAccount(
            @Param("userId") Long userId,
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId,
            @Param("email") String email
    );

    void updateNickname(
            @Param("userId") Long userId,
            @Param("nickname") String nickname
    );

    void updatePasswordHash(
            @Param("userId") Long userId,
            @Param("passwordHash") String passwordHash
    );

    void deleteOAuthAccounts(@Param("userId") Long userId);

    void anonymizeAndSoftDelete(
            @Param("userId") Long userId,
            @Param("deletedEmail") String deletedEmail
    );

    void softDelete(@Param("userId") Long userId);
}
