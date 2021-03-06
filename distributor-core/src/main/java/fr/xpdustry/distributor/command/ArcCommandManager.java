package fr.xpdustry.distributor.command;

import arc.util.*;
import arc.util.CommandHandler.*;
import cloud.commandframework.Command;
import cloud.commandframework.*;
import cloud.commandframework.annotations.*;
import cloud.commandframework.arguments.parser.*;
import cloud.commandframework.arguments.standard.*;
import cloud.commandframework.captions.*;
import cloud.commandframework.context.*;
import cloud.commandframework.exceptions.*;
import cloud.commandframework.exceptions.parsing.*;
import cloud.commandframework.execution.*;
import cloud.commandframework.internal.*;
import cloud.commandframework.meta.*;
import fr.xpdustry.distributor.command.ArcRegistrationHandler.*;
import fr.xpdustry.distributor.command.argument.PlayerArgument.*;
import fr.xpdustry.distributor.command.caption.*;
import fr.xpdustry.distributor.command.sender.*;
import fr.xpdustry.distributor.message.*;
import fr.xpdustry.distributor.message.format.*;
import io.leangen.geantyref.*;
import java.util.*;
import java.util.function.*;
import mindustry.gen.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.*;

/**
 * Mindustry implementation of cloud {@link CommandManager}.
 */
public final class ArcCommandManager extends CommandManager<ArcCommandSender> {

  private final Function<Player, ArcCommandSender> commandSenderMapper;
  private final Supplier<MessageFormatter> formatterProvider;

  /**
   * Creates a new arc command manager that wraps the given arc command handler.
   *
   * @param handler the arc command handler to wrap
   */
  public ArcCommandManager(
    final @NotNull CommandHandler handler,
    final @NotNull Function<Player, @NotNull ArcCommandSender> commandSenderMapper,
    final @NotNull Supplier<@NotNull MessageFormatter> formatterProvider
  ) {
    super(CommandExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());

    setSetting(ManagerSettings.OVERRIDE_EXISTING_COMMANDS, true);
    setCommandRegistrationHandler(new ArcRegistrationHandler(handler, this));
    getParserRegistry().registerParserSupplier(TypeToken.get(Player.class), p -> new PlayerParser<>());

    this.commandSenderMapper = commandSenderMapper;
    this.formatterProvider = formatterProvider;
  }

  /**
   * Executes a command and handle the result.
   *
   * @param sender the command sender
   * @param input  the command input
   */
  @SuppressWarnings("FutureReturnValueIgnored")
  private void handleCommand(final @NotNull ArcCommandSender sender, final @NotNull String input) {
    executeCommand(sender, input).whenComplete((result, throwable) -> {
      if (throwable == null) {
        return;
      } else if (throwable instanceof ArgumentParseException t) {
        throwable = t.getCause();
      }

      if (throwable instanceof InvalidSyntaxException t) {
        handleException(sender, InvalidSyntaxException.class, t, (s, e) -> {
          final var message = getCaptionRegistry().getCaption(ArcCaptionKeys.COMMAND_INVALID_SYNTAX, s);
          final var caption = CaptionVariable.of("syntax", e.getCorrectSyntax());
          s.sendMessage(formatterProvider.get().format(MessageIntent.ERROR, message, caption));
        });
      } else if (throwable instanceof NoPermissionException t) {
        handleException(sender, NoPermissionException.class, t, (s, e) -> {
          final var message = getCaptionRegistry().getCaption(ArcCaptionKeys.COMMAND_INVALID_PERMISSION, s);
          final var caption = CaptionVariable.of("permission", e.getMissingPermission());
          s.sendMessage(formatterProvider.get().format(MessageIntent.ERROR, message, caption));
        });
      } else if (throwable instanceof NoSuchCommandException t) {
        handleException(sender, NoSuchCommandException.class, t, (s, e) -> {
          final var message = getCaptionRegistry().getCaption(ArcCaptionKeys.COMMAND_FAILURE_NO_SUCH_COMMAND, s);
          final var caption = CaptionVariable.of("command", e.getSuppliedCommand());
          s.sendMessage(formatterProvider.get().format(MessageIntent.ERROR, message, caption));
        });
      } else if (throwable instanceof ParserException t) {
        handleException(sender, ParserException.class, t, (s, e) -> {
          final var message = getCaptionRegistry().getCaption(e.errorCaption(), s);
          s.sendMessage(formatterProvider.get().format(MessageIntent.ERROR, message, e.captionVariables()));
        });
      } else if (throwable instanceof CommandExecutionException t) {
        handleException(sender, CommandExecutionException.class, t, (s, e) -> {
          final var message = getCaptionRegistry().getCaption(ArcCaptionKeys.COMMAND_FAILURE_EXECUTION, sender);
          final var caption = CaptionVariable.of("message", e.getCause().getMessage());
          s.sendMessage(formatterProvider.get().format(MessageIntent.ERROR, message, caption));
          Log.err(e);
        });
      } else {
        final var message = getCaptionRegistry().getCaption(ArcCaptionKeys.COMMAND_FAILURE_UNKNOWN, sender);
        final var caption = CaptionVariable.of("message", throwable.getMessage());
        sender.sendMessage(formatterProvider.get().format(MessageIntent.ERROR, message, caption));
        Log.err(throwable);
      }
    });
  }

  /**
   * Executes a command and handle the result.
   *
   * @param player the player
   * @param input  the command input
   */
  public void handleCommand(final @Nullable Player player, final @NotNull String input) {
    handleCommand(commandSenderMapper.apply(player), input);
  }

  /**
   * Executes a command and handle the result. The player is set to null so use this for server-side commands.
   *
   * @param input the command input
   */
  public void handleCommand(final @NotNull String input) {
    handleCommand(commandSenderMapper.apply(null), input);
  }

  /**
   * Utility method that can convert a {@link CommandHandler.Command arc command} to a {@link Command cloud command}.
   *
   * @param command the command to convert
   * @return the converted command
   * @throws IllegalArgumentException if the command is a {@link CloudCommand}
   */
  public @NotNull Command<ArcCommandSender> convertNativeCommand(final CommandHandler.@NotNull Command command) {
    if (command instanceof CloudCommand) {
      throw new IllegalArgumentException(
        "You can't convert a cloud command that has been converted to a native command back to a cloud command..."
      );
    }

    final var meta = SimpleCommandMeta.builder()
      .with(createDefaultCommandMeta())
      .with(ArcMeta.NATIVE, true)
      .with(ArcMeta.DESCRIPTION, command.description)
      .build();

    var builder = commandBuilder(command.text, meta)
      .handler(new NativeCommandExecutionHandler(command));

    for (final var parameter : command.params) {
      final var argument = StringArgument.<ArcCommandSender>newBuilder(parameter.name);
      if (parameter.variadic) argument.greedy();
      if (parameter.optional) argument.asOptional();
      builder = builder.argument(argument);
    }

    return builder.build();
  }

  public @NotNull Function<Player, ArcCommandSender> getCommandSenderMapper() {
    return commandSenderMapper;
  }

  public @NotNull Supplier<MessageFormatter> getFormatterProvider() {
    return formatterProvider;
  }

  @Override
  public boolean hasPermission(final @NotNull ArcCommandSender sender, final @NotNull String permission) {
    return sender.hasPermission(permission);
  }

  @Override
  public Command.@NotNull Builder<ArcCommandSender> commandBuilder(
    final @NotNull String name,
    final @NotNull CommandMeta meta,
    final @NotNull String... aliases
  ) {
    return super.commandBuilder(name, meta, aliases).senderType(ArcCommandSender.class);
  }

  @Override
  public Command.@NotNull Builder<ArcCommandSender> commandBuilder(
    final @NotNull String name,
    final @NotNull String... aliases
  ) {
    return super.commandBuilder(name, aliases).senderType(ArcCommandSender.class);
  }

  @Override
  public @NotNull CommandMeta createDefaultCommandMeta() {
    return SimpleCommandMeta.builder()
      .with(ArcMeta.NATIVE, false)
      .with(ArcMeta.PLUGIN, "unknown")
      .build();
  }

  public @NotNull AnnotationParser<ArcCommandSender> createAnnotationParser() {
    return new AnnotationParser<>(this, ArcCommandSender.class, parameters -> CommandMeta.simple()
      .with(createDefaultCommandMeta())
      .with(ArcMeta.DESCRIPTION, parameters.get(StandardParameters.DESCRIPTION, ""))
      .with(ArcMeta.HIDDEN, parameters.get(StandardParameters.HIDDEN, false))
      .build()
    );
  }

  /**
   * A command execution handler that calls an underlying {@link CommandHandler.Command arc command}.
   */
  public static final class NativeCommandExecutionHandler implements CommandExecutionHandler<ArcCommandSender> {

    private final CommandHandler.Command command;

    public NativeCommandExecutionHandler(final CommandHandler.@NotNull Command command) {
      this.command = command;
    }

    @Override
    public void execute(final @NotNull CommandContext<ArcCommandSender> ctx) {
      final CommandRunner<Player> runner = Reflect.get(this.command, "runner");
      final var array = ctx.getRawInput().toArray(new String[0]);
      // Removes the first argument because it's the name of the command
      final var args = Arrays.copyOfRange(array, 1, ctx.getRawInput().size());
      runner.accept(args, ctx.getSender().isPlayer() ? ctx.getSender().getPlayer() : null);
    }
  }
}
