package messagebroker

import akka.actor.ActorSystem
import java.net.InetSocketAddress
import akka.actor.Props


object MessageBroker extends App {
  implicit val system = ActorSystem("MessageBroker");

  val messageParser = system.actorOf(Props[MessageParser], "MessageParser")
  val client = system.actorOf(TCPClient.props(new InetSocketAddress("localhost", 4000), messageParser), "TCPClient")

}