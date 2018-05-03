package ru.spbau.mesau.exchange;

import io.grpc.stub.StreamObserver;
import java.util.function.Consumer;
import java.util.logging.Logger;
import ru.spbau.mesau.MesAUGrpc.MesAUImplBase;
import ru.spbau.mesau.Message;

public class MesAUImpl extends MesAUImplBase {
  private static final Logger logger = Logger.getLogger(MesAUImpl.class.getName());

  private final Consumer<Message> messageConsumer;

  private volatile StreamObserver<Message> lastResponseObserver;

  MesAUImpl(Consumer<Message> messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

  @Override
  public StreamObserver<Message> chat(StreamObserver<Message> responseObserver) {
    logger.info("Someone wants to chat()");
    lastResponseObserver = responseObserver;
    return new IncomingStreamObserver(messageConsumer);
  }

  void sendMessage(Message message) {
    StreamObserver<Message> responseObserver = lastResponseObserver;
    if (responseObserver != null) {
      responseObserver.onNext(message);
    }
  }
}
