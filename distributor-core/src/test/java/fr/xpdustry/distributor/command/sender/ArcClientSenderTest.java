package fr.xpdustry.distributor.command.sender;

import arc.util.*;

import mindustry.gen.*;

import fr.xpdustry.distributor.command.caption.*;
import fr.xpdustry.distributor.localization.*;
import fr.xpdustry.distributor.string.*;
import fr.xpdustry.distributor.util.*;

import cloud.commandframework.captions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ArcClientSenderTest{
    private Player player;
    private TestTranslator translator;
    private ArcClientSender sender;

    @BeforeEach
    public void setup(){
        player = new TestPlayer();
        translator = new TestTranslator();
        sender = new ArcClientSender(player, translator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NONE", "DEBUG", "INFO", "ERROR", "SUCCESS"})
    public void test_send_message(String intent){
        final var message = "Hello @";
        final var expected = formatString(intent, message, "Bob");

        sender.sendMessage(MessageIntent.valueOf(intent), message, "Bob");
        assertEquals(expected, player.lastText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"NONE", "DEBUG", "INFO", "ERROR", "SUCCESS"})
    public void test_send_caption(String intent){
        final var caption = StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NUMBER;
        translator.addTranslation(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NUMBER.getKey(), SimpleCaptionRegistry.ARGUMENT_PARSE_FAILURE_NUMBER);
        final var expected = formatString(intent, "'@' is not a valid number in the range @ to @", "30", "10", "20");
        final var variables = new CaptionVariable[]{
            CaptionVariable.of("input", "30"),
            CaptionVariable.of("min", "10"),
            CaptionVariable.of("max", "20")
        };

        sender.sendMessage(MessageIntent.valueOf(intent), caption, variables);
        assertEquals(expected, player.lastText());
    }

    public String formatString(String intent, String text, Object... args){
        return switch(intent){
            case "DEBUG" -> "[gray]" + Strings.format(text.replace("@", "[lightgray]@[]"), args);
            case "ERROR" -> "[scarlet]" + Strings.format(text.replace("@", "[orange]@[]"), args);
            case "SUCCESS" -> Strings.format(text.replace("@", "[green]@[]"), args);
            case "NONE", "INFO" -> Strings.format(text, args);
            default -> throw new IllegalArgumentException("Unable to resolve formatter: " + intent);
        };
    }
}
