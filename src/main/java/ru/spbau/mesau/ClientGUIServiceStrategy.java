package ru.spbau.mesau;

import io.grpc.stub.StreamObserver;

/** The state of {@link GUIRunner} as a client. */
public class ClientGUIServiceStrategy extends GUIServiceStrategy {
  private final StreamObserver<Message> responseStreamObserver;

  public ClientGUIServiceStrategy(StreamObserver<Message> responseStreamObserver) {
    this.responseStreamObserver = responseStreamObserver;
  }

  @Override
  public void sendMessage(Message message) {
    responseStreamObserver.onNext(message);
  }
}
