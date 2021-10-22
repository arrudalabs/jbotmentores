package jbotmentores.bot;

import jbotmentores.model.JBotData;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotMessageListener extends ListenerAdapter {

    private JBotData jBotData;

    public BotMessageListener(JBotData jBotData) {
        this.jBotData = jBotData;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getSubcommandName() == "list") {
            if (event.getOptions().contains("dia")) {
                listMentorsByDay(event, event.getOption("dia").getAsLong());
            }

            if (event.getOptions().contains("skill")) {
                listMentorsBySkill(event, event.getOption("skill").getAsString());
            }

            if (event.getOptions().contains("user")) {
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
        var mentores = jBotData.mentores.stream().filter(m -> m.getSkills().stream().anyMatch(s -> s.getName().toLowerCase().contains(skill.toLowerCase())));

        event.reply(mentores.toString())
                .setEphemeral(true)
                .queue();
    }

    private void listMentorsByDay(SlashCommandEvent event, Long dia) {

    }
}
