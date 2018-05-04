package ru.spbau.mesau.exchange;

import ru.spbau.mesau.Message;

class FakeMessages {
  static Message serverMessage =
    Message.newBuilder()
      .setContent("server-content")
      .setFromDateTimestamp(0)
      .setAuthor("server")
      .build();

  static Message clientMessage =
    Message.newBuilder()
      .setContent("client-content")
      .setFromDateTimestamp(0)
      .setAuthor("client")
      .build();
}
