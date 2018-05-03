package ru.spbau.mesau;

/** Specifies the state of the service that {@link GUIRunner} serves as. */
public abstract class ServiceStrategy {
  public abstract void sendMessage(Message message);
}
