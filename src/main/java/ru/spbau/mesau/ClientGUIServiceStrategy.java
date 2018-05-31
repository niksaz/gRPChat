package ru.spbau.mesau;

import ru.spbau.mesau.exchange.MesAUClient;

/** The state of {@link GUIRunner} as a client. */
public class ClientGUIServiceStrategy extends GUIServiceStrategy {
  private final MesAUClient client;

  public ClientGUIServiceStrategy(MesAUClient client) {
    this.client = client;
  }

  @Override
  public void sendMessage(Message message) {
    client.sendMessage(message);
  }
}
