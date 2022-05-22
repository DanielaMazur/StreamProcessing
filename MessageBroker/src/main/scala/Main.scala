package messagebroker

import akka.actor.ActorSystem
import akka.actor.Props

object Main extends App  {
  implicit val system = ActorSystem("MessageBroker");

  val supervisor = system.actorOf(Props[Supervisor], "Supervisor")
 }