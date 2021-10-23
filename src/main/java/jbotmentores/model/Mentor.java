package jbotmentores.model;

import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
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
        return print(s -> s, a -> a);
    }

    public String print(Function<Set<String>, Set<String>> skillModifier, Function<Set<Slot>, Set<Slot>> slotsModifier) {
        StringWriter out = new StringWriter();
        PrintWriter printWriter = new PrintWriter(out);

        var printedSkills = printSkills(skillModifier);
        var printedSlots = printSlots(slotsModifier);
        printWriter.print(name);

        if(StringUtils.hasText(printedSkills)){
            printWriter.print("\n" + printedSkills);
        }
        if(StringUtils.hasText(printedSlots)){
            printWriter.print("\n" + printedSlots);
        }
        printWriter.println();
        return out.toString();
    }

    public String printSkills(Function<Set<String>, Set<String>> skillModifier) {
        StringWriter skillsText = new StringWriter();
        PrintWriter out = new PrintWriter(skillsText);
        var skills = skillModifier.apply(new TreeSet<>(this.skills));

        if (!skills.isEmpty()) {
            out.println(String.format(" - Pode ajudar com: \n    %s;", skills.stream().collect(Collectors.joining(", "))));
        }

        return skillsText.toString();
    }

    public String printSlots(Function<Set<Slot>, Set<Slot>> slotsModifier) {
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);

        var processedSlot = slotsModifier.apply(new TreeSet<>(this.slots));
        if (!processedSlot.isEmpty()) {
            printWriter.println(" - Slots:");
        }

        var slotByDay = new TreeMap<Integer, Set<Slot>>();
        processedSlot
                .stream()
                .forEach(slot -> {
                    slotByDay.computeIfAbsent(slot.getStartAt().getDayOfMonth(), k -> new TreeSet<>())
                            .add(slot);
                });

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        slotByDay
                .forEach((day, slotsOftheDay) -> {

                    printWriter.println(String.format("  - Dia %s:", day));

                    slotsOftheDay.forEach(slot -> {
                        printWriter.println(String.format("      - %s as %s", timeFormatter.format(slot.getStartAt()), timeFormatter.format(slot.getEndAt())));
                    });

                    printWriter.println();
                });


        return result.toString();
    }

    public Set<Slot> getSlots() {
        return slots;
    }
}
