package jbotmentores.model;

import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import java.util.Objects;

@Entity
public class DiscordInfo {
    @Id
    @Email
    @NotNull
    private String mentorEmail;

    private String discordRef;

    /**
     * Don't use it! It's required by JPA
     */
    public DiscordInfo() {
    }

    public DiscordInfo(@NotNull Mentor mentor) {
        this.mentorEmail = mentor.getEmail();
    }

    public String getMentorEmail() {
        return mentorEmail;
    }

    public void setMentorEmail(String mentorEmail) {
        this.mentorEmail = mentorEmail;
    }

    public String getDiscordRef() {
        return discordRef;
    }

    public void setDiscordRef(String discordRef) {
        this.discordRef = discordRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordInfo that = (DiscordInfo) o;
        return mentorEmail.equals(that.mentorEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mentorEmail);
    }
}
