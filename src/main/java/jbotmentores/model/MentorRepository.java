package jbotmentores.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, String> {
    Optional<Mentor> findByEmail(String email);

    @Query(
            nativeQuery = true,
            value = "select * from mentor where lower(name) like '%' || ? || '%'"
    )
    Stream<Mentor> findByName(String name);

    @Query(
            nativeQuery = true,
            value = "select distinct mentor_email from mentor_skills where lower(skills) like lower('%' || ? || '%')"
    )
    Stream<String> findBySkill(String skill);

    @Query(
            nativeQuery = true,
            value = "select distinct mentor_email from mentor_slots where start_at between ? and ?"
    )
    Stream<String> findBySlotRange(LocalDateTime start, LocalDateTime end);
}
