package jbotmentores.bot;

import jbotmentores.model.JBotData;
import jbotmentores.model.Mentor;
import net.dv8tion.jda.api.MessageBuilder;
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
        if ("list".equals(event.getSubcommandName())) {
            if (event.getOptions().stream().anyMatch(o -> o.getName().contains("dia"))) {
                listMentorsByDay(event, event.getOption("dia").getAsLong());
            }

            if (event.getOptions().stream().anyMatch(o -> o.getName().contains("skill"))) {
                listMentorsBySkill(event, event.getOption("skill").getAsString());
            }

            if (event.getOptions().stream().anyMatch(o -> o.getName().contains("user"))) {
                listUser(event, event.getOption("user").getAsUser());
            }
        }
    }

    private void listUser(SlashCommandEvent event, User user) {

        event.reply("Entendi: " + event.getCommandPath() + " " + event.getSubcommandGroup() + " " + event.getOptions())
                .setEphemeral(true)
                .queue();

    }

    private void listMentorsBySkill(SlashCommandEvent event, String skill) {
        event.deferReply(true).queue(
                hook -> {
                    var mentores = jBotData.getMentores()
                            .stream()
                            .filter(Objects::nonNull)
                            .filter(m -> m.getSkills().stream().anyMatch(s -> s.getName().toLowerCase().contains(skill.toLowerCase())))
                            .collect(Collectors.toList());


                    String response = mentores.stream().map(Mentor::getName)
                            .collect(Collectors.joining("\n"));

                    if(response.isEmpty()){
                        response="Desculpe, mas não encontrei mentores com esse skill  :anguished:";
                    }

                    hook.getInteraction().getMember().getUser().getAsTag();
                    hook.sendMessage(response).queue();
                }
        );
    }

    private void listMentorsByDay(SlashCommandEvent event, Long dia) {

    }
}
