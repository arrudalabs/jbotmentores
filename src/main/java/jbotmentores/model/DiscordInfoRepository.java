package jbotmentores.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscordInfoRepository extends JpaRepository<DiscordInfo, String> {

    default Optional<DiscordInfo> findByMentor(Mentor mentor) {
        if (mentor == null) return Optional.empty();
        return findByMentorEmail(mentor.getEmail());
    }

    Optional<DiscordInfo> findByMentorEmail(String email);

}
