package jbotmentores.model;

import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
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
        var slotsFormatted = formatSlots();
        return String.format("**%s**", name) + "\nPode ajudar com: " +
                skills.toString() +
                "\n" +
                slotsFormatted +
                "\n\n";
    }

    private String formatSlots() {
        StringBuilder result = new StringBuilder("Horários disponíveis: :clock1: \n");
        for (Slot s : slots) {
            result.append(" às ").append(s.getStartAt().toLocalTime())
                    .append("-")
                    .append(s.getEndAt().toLocalTime())
                    .append("\n");
        }
        return result.toString();
    }

    // TODO sort slots by DAY
    public Set<Slot> getSlots() {
        return slots;
    }
}
