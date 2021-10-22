package jbotmentores.bot;

import jbotmentores.model.*;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Validator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class BotMessageListener extends ListenerAdapter {

    private MentorListCommandHandler mentorListCommandHandler;
    private MentorActivateCommandHandler mentorActivateCommandHandler;

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
