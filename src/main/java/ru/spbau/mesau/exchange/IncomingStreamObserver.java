package ru.spbau.mesau.exchange;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.spbau.mesau.Message;

/** Class that feeds incoming {@link Message}s to provided {@link Consumer<Message>}. */
public class IncomingStreamObserver implements StreamObserver<Message> {
  private static final Logger logger = Logger.getLogger(IncomingStreamObserver.class.getName());

  private final Consumer<Message> incomingMessageConsumer;

  IncomingStreamObserver(Consumer<Message> incomingMessageConsumer) {
    this.incomingMessageConsumer = incomingMessageConsumer;
  }

  @Override
  public void onNext(Message value) {
    logger.info("incoming message: " + value);
    incomingMessageConsumer.accept(value);
  }

  @Override
  public void onError(Throwable t) {
    logger.log(Level.WARNING, "Error in the incoming stream", Status.fromThrowable(t));
  }

  @Override
  public void onCompleted() {
    logger.info("Stream has been completed");
  }
}
