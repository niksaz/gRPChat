package ru.spbau.mesau;

import io.grpc.stub.StreamObserver;

public class ClientServiceStrategy extends ServiceStrategy {
  private final StreamObserver<Message> responseStreamObserver;

  public ClientServiceStrategy(StreamObserver<Message> responseStreamObserver) {
    this.responseStreamObserver = responseStreamObserver;
  }

  @Override
  public void sendMessage(Message message) {
    responseStreamObserver.onNext(message);
  }
}
