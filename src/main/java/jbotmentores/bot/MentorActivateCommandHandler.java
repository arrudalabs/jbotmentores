package jbotmentores.bot;

import jbotmentores.model.DiscordInfo;
import jbotmentores.model.DiscordInfoRepository;
import jbotmentores.model.MentorRepository;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class MentorActivateCommandHandler {

    private final MentorRepository mentorRepository;
    private final DiscordInfoRepository discordInfoRepository;

    public MentorActivateCommandHandler(MentorRepository mentorRepository, DiscordInfoRepository discordInfoRepository) {
        this.mentorRepository = mentorRepository;
        this.discordInfoRepository = discordInfoRepository;
    }

    @Transactional
    public void handle(InteractionHook hook, SlashCommandEvent event) {

        if (event.getOptions().stream().anyMatch(o -> o.getName().contains("e-mail"))) {
            var email=event.getOption("e-mail").getAsString();
            mentorRepository.findByEmail(email)
                    .ifPresentOrElse(
                            mentor -> {
                                DiscordInfo discordInfo = discordInfoRepository
                                        .findByMentor(mentor)
                                        .orElseGet(() -> new DiscordInfo(mentor));
                                discordInfo.setDiscordRef(event.getMember().getAsMention());
                                discordInfoRepository.save(discordInfo);
                                hook.sendMessage(String.format("Mentor %s ativado com sucesso! :clap:", discordInfo.getDiscordRef()))
                                        .setEphemeral(true)
                                        .queue();
                            }, () -> {
                                hook.sendMessage("Desculpe, mas esse e-mail informado não está vinculado a nenhum mentor :anguished:")
                                        .setEphemeral(true)
                                        .queue();
                            });
            return;
        }
        hook.sendMessage("Desculpe, mas não entendi sua solicitação :anguished:")
                .setEphemeral(true)
                .queue();
    }


}
