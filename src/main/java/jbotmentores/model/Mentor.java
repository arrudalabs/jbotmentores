package jbotmentores.model;

import java.time.temporal.ChronoField;
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
                result.append("Dia: ").append(s.getFrom().get(ChronoField.DAY_OF_MONTH)).append(" ");

            result.append("Horarios: ").append(s.getFrom().toLocalTime())
                    .append("-")
                    .append(s.getTo().toLocalTime())
                    .append("\n");
        }
        return result.toString();
    }

    public Set<Slot> getSlots() {
        return slots;
    }
}
