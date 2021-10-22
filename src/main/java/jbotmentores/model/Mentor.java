package jbotmentores.model;

import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.temporal.ChronoField;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Entity
public class Mentor {

    @Id
    @NotNull
    @Email
    private String email;
    private String name;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "mentor_skills",
            joinColumns = @JoinColumn(name = "mentor_email")
    )
    private Set<String> skills;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "mentor_slots",
            joinColumns = @JoinColumn(name = "mentor_email")
    )
    private Set<Slot> slots;

    /**
     * Don't use it! It's required by JPA
     */
    @Deprecated
    public Mentor() {
    }

    public Mentor(String email, String name, Set<String> skills, Set<Slot> slots) {
        this.email = email;
        this.name = name;
        this.skills = skills.stream().filter(StringUtils::hasText).collect(Collectors.toCollection(TreeSet::new));
        this.slots = slots.stream().filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mentor mentor = (Mentor) o;
        return Objects.equals(email, mentor.email) && Objects.equals(name, mentor.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, name);
    }

    public Set<String> getSkills() {
        return skills;
    }

    @Override
    public String toString() {
        return this.toString(true);
    }

    public String toString(boolean printDay) {
        var slotsFormatted = formatSlots(printDay);
        return String.format("%s - Pode ajudar com: %s - %s ", name, skills.toString(), slotsFormatted);
    }

    private String formatSlots(boolean printDay) {
        StringBuilder result = new StringBuilder();
        for (Slot s : slots) {
            if (printDay)
                result.append("Dia: ").append(s.getStartAt().get(ChronoField.DAY_OF_MONTH)).append(" ");

            result.append("Horarios: ").append(s.getStartAt().toLocalTime())
                    .append("-")
                    .append(s.getEndAt().toLocalTime())
                    .append("\n");
        }
        return result.toString();
    }

    public Set<Slot> getSlots() {
        return slots;
    }
}
