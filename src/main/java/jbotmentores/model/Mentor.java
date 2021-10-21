package jbotmentores.model;

import java.util.Objects;

public class Mentor {

    private final String email;
    private final String name;

    public Mentor(String email, String name) {
        this.email = email;
        this.name = name;
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
}
