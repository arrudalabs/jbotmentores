package jbotmentores.bot;

import jbotmentores.model.DiscordInfoRepository;
import jbotmentores.model.Mentor;
import jbotmentores.model.MentorRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MentorListCommandHandler {

    private final MentorRepository mentorRepository;
    private final DiscordInfoRepository discordInfoRepository;

    public MentorListCommandHandler(MentorRepository mentorRepository, DiscordInfoRepository discordInfoRepository) {
        this.mentorRepository = mentorRepository;
        this.discordInfoRepository = discordInfoRepository;
    }

    @Transactional
    public void handle(InteractionHook hook, SlashCommandEvent event) {

        if (event.getOptions().stream().anyMatch(o -> o.getName().contains("dia"))) {
            return;
        } else if (event.getOptions().stream().anyMatch(o -> o.getName().contains("skill"))) {
            listMentorsBySkill(hook, event, event.getOption("skill").getAsString());
            return;
        } else if (event.getOptions().stream().anyMatch(o -> o.getName().contains("user"))) {
            listUser(hook, event, event.getOption("user").getAsUser());
            return;
        }
    }

    private void listUser(InteractionHook hook, SlashCommandEvent event, User user) {
        hook.sendMessage("Desculpe, mas ainda não estou pronto  :anguished:")
                .setEphemeral(true)
                .queue();

    }

    private void listMentorsByDay(InteractionHook hook, SlashCommandEvent event, Long dia) {
        hook.sendMessage("Desculpe, mas ainda não estou pronto  :anguished:")
                .setEphemeral(true)
                .queue();
    }

    private void listMentorsBySkill(InteractionHook hook, SlashCommandEvent event, String skill) {
        var mentores =
                mentorRepository.findBySkill(skill)
                        .map(mentorRepository::findByEmail)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        String response = mentores.stream().map(Mentor::getName)
                .collect(Collectors.joining("\n"));

        if (response.isEmpty()) {
            response = "Desculpe, mas não encontrei mentores com esse skill  :anguished:";
        }

        hook.getInteraction().getMember().getUser().getAsTag();
        hook.sendMessage(response).queue();
    }


}
