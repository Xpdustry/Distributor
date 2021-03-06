package fr.xpdustry.distributor.message;

import arc.struct.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

public interface MessageReceiver {

  static MessageReceiver ofPlayer(final @NotNull Player player) {
    return new ClientMessageReceiver(player);
  }

  static MessageReceiver ofPlayers(final @NotNull Iterable<Player> players) {
    return new ForwardingMessageReceiver(Seq.with(players).map(ClientMessageReceiver::new));
  }

  static MessageReceiver ofReceivers(final @NotNull Iterable<MessageReceiver> receivers) {
    return new ForwardingMessageReceiver(receivers);
  }

  static MessageReceiver server() {
    return ServerMessageReceiver.INSTANCE;
  }

  void sendMessage(final @NotNull String message);
}
