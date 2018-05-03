package ru.spbau.mesau;

import ru.spbau.mesau.exchange.MesAUServerRunner;

/** The state of {@link GUIRunner} as a server. */
public class ServerServiceStrategy extends ServiceStrategy {
  private final MesAUServerRunner runner;

  public ServerServiceStrategy(MesAUServerRunner runner) {
    this.runner = runner;
  }

  @Override
  public void sendMessage(Message message) {
    runner.sendMessage(message);
  }
}
