package ru.spbau.mesau;

import ru.spbau.mesau.exchange.MesAUServerRunner;

/** The state of {@link GUIRunner} as a server. */
public class ServerGUIServiceStrategy extends GUIServiceStrategy {
  private final MesAUServerRunner runner;

  public ServerGUIServiceStrategy(MesAUServerRunner runner) {
    this.runner = runner;
  }

  @Override
  public void sendMessage(Message message) {
    runner.sendMessage(message);
  }
}
