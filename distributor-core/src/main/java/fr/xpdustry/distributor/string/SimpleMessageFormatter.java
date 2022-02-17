package fr.xpdustry.distributor.string;

import arc.util.Nullable;
import arc.util.*;

import cloud.commandframework.captions.*;
import org.jetbrains.annotations.*;


/** This formatter performs basic formatting without any variations specified by {@link MessageIntent intents}. */
public final class SimpleMessageFormatter implements MessageFormatter{
    private static final SimpleMessageFormatter INSTANCE = new SimpleMessageFormatter();
    private final CaptionVariableReplacementHandler handler = new SimpleCaptionVariableReplacementHandler();

    public static SimpleMessageFormatter getInstance(){
        return INSTANCE;
    }

    @Override public @NotNull String format(
        final @NotNull MessageIntent intent,
        final @NotNull String message,
        final @Nullable Object... args
    ){
        return Strings.format(message, args);
    }

    @Override public @NotNull String format(
        final @NotNull MessageIntent intent,
        final @NotNull String message,
        final @NotNull CaptionVariable... vars
    ){
        return handler.replaceVariables(message, vars);
    }
}