package consumer

import akka.actor.ActorSystem
import akka.actor.Props

object Main extends App {
  implicit val system = ActorSystem("Consumer");

  val consumer1 = system.actorOf(Props[Consumer], "Consumer1")
  // val consumer2 = system.actorOf(Props[Consumer], "Consumer2")
}