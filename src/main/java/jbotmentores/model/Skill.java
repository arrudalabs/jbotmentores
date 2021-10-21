package jbotmentores.model;

public record Skill(String name) {
    public Skill {
        name = name.trim().toUpperCase();
    }
}
