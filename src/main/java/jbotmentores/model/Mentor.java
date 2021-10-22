package jbotmentores.model;

import java.util.Objects;
import java.util.Set;

public class Mentor {

    private final String email;
    private final String name;
    private final Set<Skill> skills;
    private final Set<Slot> slots;

    public Mentor(String email, String name, Set<Skill> skills, Set<Slot> slots) {
        this.email = email;
        this.name = name;
        this.skills = skills;
        this.slots = slots;
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

    public Set<Skill> getSkills() {
        return skills;
    }

    public Set<Slot> getSlots() {
        return slots;
    }
}
