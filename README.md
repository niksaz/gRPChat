# gRPChat

Messaging app based on [gRPC](https://grpc.io).

Authors: Mikita Sazanovich, Daniil Smirnov, Egor Bogomolov.

## Running

To run the app with gradle you need to have [Google protobuf compiler](https://github.com/google/protobuf/releases).

First, assemble the jar file:
```
./gradlew jar
```

To run the instance execute:
```
java -jar ./build/libs/protobuf-1.0-SNAPSHOT.jar
```

Run the first instance and 'Become server', then the second one and 'Connect to someone'.
