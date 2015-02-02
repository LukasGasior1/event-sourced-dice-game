# Event sourced dice game
Simple dice game implemented using CQRS and event-sourcing.

## Architecture overview
![Architecture overview](https://raw.githubusercontent.com/LukasGasior1/event-sourced-dice-game/master/doc/diagram.png)

## Running

1. Run RabbitMQ (Docker is enough for quick start): `docker run -d -p 5672:5672 -p 15672:15672 dockerfile/rabbitmq`
2. Run game server: `sbt "project game" run`
3. Run statistics app (optional): `sbt "project statistics" run`
4. Run web application: `sbt "project webapp" run`

Once webapp is running, navigate to `http://127.0.0.1:9000/` to play.

If statistics app is running, navigate to `http://127.0.0.1:8083/stats` to get dice rolls stats.

More details can be found on [ScalaC Team Blog](http://blog.scalac.io/) (soon).
