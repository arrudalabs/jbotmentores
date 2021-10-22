package jbotmentores.bot;

import jbotmentores.model.JBotData;
import jbotmentores.model.Mentor;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;
import java.util.stream.Collectors;

public class BotMessageListener extends ListenerAdapter {

    private JBotData jBotData;

    public BotMessageListener(JBotData jBotData) {
        this.jBotData = jBotData;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("ativar".equals(event.getSubcommandName())) {
            activateMentor(event, event.getOption("email").getAsString());
            return;
        }
        if ("list".equals(event.getSubcommandName())) {
            if (event.getOptions().stream().anyMatch(o -> o.getName().contains("dia"))) {
                listMentorsByDay(event, event.getOption("dia").getAsLong());
                return;
            } else if (event.getOptions().stream().anyMatch(o -> o.getName().contains("skill"))) {
                listMentorsBySkill(event, event.getOption("skill").getAsString());
                return;
            } else if (event.getOptions().stream().anyMatch(o -> o.getName().contains("user"))) {
                listUser(event, event.getOption("user").getAsUser());
                return;
            }
        }
        event.reply("Desculpe, mas não entendi sua solicitação :anguished:")
                .setEphemeral(true)
                .queue();

    }

    private void listUser(SlashCommandEvent event, User user) {
        event.reply("Desculpe, mas ainda não estou pronto  :anguished:")
                .setEphemeral(true)
                .queue();

    }

    private void listMentorsByDay(SlashCommandEvent event, Long dia) {
        event.reply("Desculpe, mas ainda não estou pronto  :anguished:")
                .setEphemeral(true)
                .queue();

    }

    private void listMentorsBySkill(SlashCommandEvent event, String skill) {
        event.deferReply(true).queue(
                hook -> {
                    var mentores = jBotData.listMentoresBySkill(skill);

                    String response = mentores.stream().map(Mentor::getName)
                            .collect(Collectors.joining("\n"));

                    if (response.isEmpty()) {
                        response = "Desculpe, mas não encontrei mentores com esse skill  :anguished:";
                    }

                    hook.getInteraction().getMember().getUser().getAsTag();
                    hook.sendMessage(response).queue();
                }
        );
    }

    private void activateMentor(SlashCommandEvent event, String email) {
        event.reply("Desculpe, mas ainda não estou pronto  :anguished:")
                .setEphemeral(true)
                .queue();
    }
}
