package messagebroker

import akka.actor.ActorSystem
import java.net.InetSocketAddress
import akka.actor.Props
import akka.actor.Actor

object Main extends App  {
  implicit val system = ActorSystem("MessageBroker");

  val supervisor = system.actorOf(Props[Supervisor], "Supervisor")
 }