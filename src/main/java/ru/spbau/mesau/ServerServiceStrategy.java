package ru.spbau.mesau;

import ru.spbau.mesau.exchange.MesAUServiceRunner;

public class ServerServiceStrategy extends ServiceStrategy {
  private final MesAUServiceRunner runner;

  public ServerServiceStrategy(MesAUServiceRunner runner) {
    this.runner = runner;
  }

  @Override
  public void sendMessage(Message message) {
    runner.sendMessage(message);
  }
}
