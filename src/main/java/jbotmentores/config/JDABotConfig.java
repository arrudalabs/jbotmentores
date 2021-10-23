package jbotmentores.config;

import jbotmentores.bot.BotMessageListener;
import jbotmentores.model.JBotData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.login.LoginException;

@Configuration
public class JDABotConfig {

    private final Environment environment;
    private final BotMessageListener messageListener;
    private JDA jda;
    private JBotData jBotData;

    public JDABotConfig(Environment environment, JBotData jBotData, BotMessageListener messageListener) {
        this.environment = environment;
        this.jBotData = jBotData;
        this.messageListener = messageListener;
    }

    @PostConstruct
    void start() throws LoginException, InterruptedException {
        JDABuilder builder = JDABuilder.createDefault(environment.getProperty("BOT_TOKEN"));
        // Disable parts of the cache
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        // Enable the bulk delete event
        builder.setBulkDeleteSplittingEnabled(false);
        // Disable compression (not recommended)
        builder.setCompression(Compression.NONE);

        configureMemoryUsage(builder);

        builder.addEventListeners(new ReadyListener());
        builder.addEventListeners(messageListener);

        this.jda = builder.build();

        updateCommands(this.jda);

        // optionally block until JDA is ready
        jda.awaitReady();

    }

    private void updateCommands(JDA jda) {
        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                new CommandData("mentor", "Confira a info sobre os mentores do evento")
                        .addSubcommands(
                                new SubcommandData("list", "lista mentores")
                                        .addOptions(new OptionData(OptionType.INTEGER, "dia", "Informe o dia (22 | 23 | 24)")
                                                .addChoice("22", 22)
                                                .addChoice("23", 23)
                                                .addChoice("24", 24))
                                        .addOptions(new OptionData(OptionType.STRING, "skill", "Informe o skill"))
                                        .addOptions(new OptionData(OptionType.STRING, "name", "Informe nome do mentor para visualizar as suas skils e horários disponíveis"))
                        )

        );

        commands.queue();
    }

    @PreDestroy
    void shutdown() {
        this.jda.shutdownNow();
    }


    void configureMemoryUsage(JDABuilder builder) {
        // Disable cache for member activities (streaming/games/spotify)
        builder.disableCache(CacheFlag.ACTIVITY);

        // Disable member chunking on startup
//        builder.setChunkingFilter(ChunkingFilter.NONE);

        // Disable presence updates and typing events
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING);

        // Consider guilds with more than 50 members as "large".
        // Large guilds will only provide online members in their setup and thus reduce bandwidth if chunking is disabled.
        builder.setLargeThreshold(50);
    }


    static class ReadyListener implements EventListener {
        @Override
        public void onEvent(GenericEvent event) {
            if (event instanceof ReadyEvent)
                System.out.println("API is ready!");
        }
    }
}
