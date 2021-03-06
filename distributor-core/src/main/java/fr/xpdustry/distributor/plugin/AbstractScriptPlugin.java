package fr.xpdustry.distributor.plugin;

import arc.util.*;
import cloud.commandframework.arguments.standard.*;
import fr.xpdustry.distributor.*;
import fr.xpdustry.distributor.command.*;
import fr.xpdustry.distributor.message.*;
import java.util.*;
import javax.script.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.*;

public abstract class AbstractScriptPlugin extends AbstractPlugin implements ScriptEngineFactory {

  @Override
  public abstract @NotNull String getEngineName();

  @Override
  public abstract @NotNull String getEngineVersion();

  @Override
  public abstract @NotNull List<String> getExtensions();

  @Override
  public abstract @NotNull List<String> getMimeTypes();

  @Override
  public abstract @NotNull List<String> getNames();

  @Override
  public abstract @NotNull String getLanguageName();

  @Override
  public abstract @NotNull String getLanguageVersion();

  @Override
  public abstract @Nullable Object getParameter(final @NotNull String key);

  @Override
  public abstract @NotNull String getMethodCallSyntax(final @NotNull String obj, final @NotNull String m, final @NotNull String... args);

  @Override
  public abstract @NotNull String getOutputStatement(final @NotNull String toDisplay);

  @Override
  public abstract @NotNull String getProgram(final @NotNull String... statements);

  @Override
  public abstract @NotNull ScriptEngine getScriptEngine();

  @SuppressWarnings("NullAway") // <- ScriptException#getMessage() can't be null.
  @Override
  public void registerSharedCommands(final @NotNull ArcCommandManager manager) {
    manager.command(manager.commandBuilder(getExtensions().get(0))
      .meta(ArcMeta.DESCRIPTION, "Run arbitrary " + Strings.capitalize(getLanguageName()) + " code.")
      .meta(ArcMeta.PARAMETERS, "<script...>")
      .meta(ArcMeta.PLUGIN, asLoadedMod().name)
      .permission(ArcPermission.ADMIN.or(ArcPermission.SCRIPT))
      .argument(StringArgument.greedy("script"))
      .handler(ctx -> {
        try {
          final var output = getScriptEngine().eval(ctx.<String>get("script"));
          final var formatter = Distributor.getMessageFormatter(ctx.getSender());
          ctx.getSender().sendMessage(formatter.format(MessageIntent.NONE, String.valueOf(output)));
        } catch (ScriptException e) {
          ctx.getSender().sendMessage(e.getMessage());
        }
      })
    );
  }
}
