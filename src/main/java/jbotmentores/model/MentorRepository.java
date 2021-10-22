package jbotmentores.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, String> {
    Optional<Mentor> findByEmail(String email);

    @Query(
            nativeQuery = true,
            value = "select distinct mentor_email from mentor_skills where lower(skills) like lower('%' || ? || '%')"
    )
    Stream<String> findBySkill(String skill);
}
