package jbotmentores.bot;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class BotMessageListener extends ListenerAdapter {

    private final MentorListCommandHandler mentorListCommandHandler;
    private final MentorActivateCommandHandler mentorActivateCommandHandler;

    public BotMessageListener(MentorListCommandHandler mentorListCommandHandler, MentorActivateCommandHandler mentorActivateCommandHandler) {
        this.mentorListCommandHandler = mentorListCommandHandler;
        this.mentorActivateCommandHandler = mentorActivateCommandHandler;
    }

    @Override
    @Transactional
    public void onSlashCommand(final SlashCommandEvent event) {
        event.deferReply(true).queue(
                hook -> {
                    if ("ativar".equals(event.getSubcommandName())) {
                        mentorActivateCommandHandler.handle(hook, event);
                        return;
                    }
                    if ("list".equals(event.getSubcommandName())) {
                        mentorListCommandHandler.handle(hook, event);
                        return;
                    }
                    hook.sendMessage("Desculpe, mas não entendi sua solicitação :anguished:")
                            .setEphemeral(true)
                            .queue();
                });

    }
}
