package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    Optional<UserDeviceToken> findByToken(String token);

    @Query("SELECT t.token FROM UserDeviceToken t WHERE t.userId IN :userIds")
    List<String> findAllTokensByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query("DELETE FROM UserDeviceToken t WHERE t.token = :token AND t.userId = :userId")
    int deleteByTokenAndUserId(@Param("token") String token, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserDeviceToken t WHERE t.token IN :tokens")
    void deleteByTokenIn(@Param("tokens") List<String> tokens);
}
