package jbotmentores.bot;

import jbotmentores.model.DiscordInfoRepository;
import jbotmentores.model.Mentor;
import jbotmentores.model.MentorRepository;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
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
            listMentorsByDay(hook, event, event.getOption("dia").getAsLong());
            return;
        } else if (event.getOptions().stream().anyMatch(o -> o.getName().contains("skill"))) {
            listMentorsBySkill(hook, event, event.getOption("skill").getAsString());
            return;
        } else if (event.getOptions().stream().anyMatch(o -> o.getName().contains("name"))) {
            listUser(hook, event, event.getOption("name").getAsString());
            return;
        } else {
            hook.sendMessage("Desculpe, não encontrei a opcão que você digitou: " + event.getOptions())
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void listUser(InteractionHook hook, SlashCommandEvent event, String mentorName) {
        if (StringUtils.isBlank(mentorName)) {
            hook.sendMessage("Por favor, informe o nome da pessoa!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // TODO filter out slots in the past
        var mentores = mentorRepository.findByName(mentorName.toLowerCase()).collect(Collectors.toList());

        if (mentores.isEmpty()) {
            hook.sendMessage("Ué! Não encontrei ninguém com esse nome :thinking: ").queue();
        } else {
            final String mentoresFormatted = formatMentores(mentores);
            hook.sendMessage(mentoresFormatted).queue();
        }

    }

    private String formatMentores(List<Mentor> mentores) {
        StringBuilder sb = new StringBuilder("Encontrei os seguintes mentores: \n");
        for (Mentor m : mentores) {
            sb.append(m.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    private void listMentorsByDay(InteractionHook hook, SlashCommandEvent event, Long dia) {
        if (dia == null || dia < 22 || dia > 24) {
            hook.sendMessage("Desculpe, mas os dias desse evento são 22, 23 ou 24  :face_with_monocle:")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        LocalDateTime start = LocalDateTime.of(2021, Month.OCTOBER, dia.intValue(), 0, 0);
        LocalDateTime end = LocalDateTime.of(2021, Month.OCTOBER, dia.intValue(), 23, 59);

        var mentores = mentorRepository.findBySlotRange(start, end)
                .map(mentorRepository::findByEmail)
                .filter(Optional::isPresent)
                .map(Optional::get);

        var names = mentores.map(Mentor::getName)
                .sorted().collect(Collectors.toList());
        var messageBuilder = new MessageBuilder();
        if (names.isEmpty()) {
            messageBuilder.append("Desculpe, mas não encontrei mentores com esse skill  :anguished:");
        } else {
            String rawNames = String.join("\n", names);
            messageBuilder.append(rawNames);
            messageBuilder.appendFormat("\n\nEncontramos %s mentore(s) o dia %s", names.size(), dia);
        }
        hook.sendMessage(messageBuilder.build()).queue();
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
