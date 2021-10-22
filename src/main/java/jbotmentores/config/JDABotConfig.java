package jbotmentores.config;

import jbotmentores.bot.BotMessageListener;
import jbotmentores.model.JBotData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.login.LoginException;

@Configuration
public class JDABotConfig {

    public final Environment environment;

    private JDA jda;

    private JBotData jBotData;

    public JDABotConfig(Environment environment, JBotData jBotData) {
        this.environment = environment;
        this.jBotData = jBotData;
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
        builder.addEventListeners(new BotMessageListener(jBotData));

        this.jda = builder.build();

        // optionally block until JDA is ready
        jda.awaitReady();

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
