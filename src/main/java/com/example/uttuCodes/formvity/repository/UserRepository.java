package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.dto.UserDto;
import com.example.uttuCodes.formvity.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByDisplayName(String displayName);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Email is stored lowercase; display name match uses trimmed input as-is.
     */
    default Optional<UserEntity> findByLoginIdentifier(String rawLogin) {
        if (rawLogin == null) {
            return Optional.empty();
        }
        String trimmed = rawLogin.trim();
        return findByEmail(trimmed.toLowerCase(Locale.ROOT))
                .or(() -> findByDisplayName(trimmed));
    }

    @Query("SELECT new com.example.uttuCodes.formvity.dto.UserDto(u.displayName, u.email, u.id) FROM UserEntity u")
    List<UserDto> fetchUserDto();
}
