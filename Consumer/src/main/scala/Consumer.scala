package consumer

import akka.actor.Actor
import akka.actor.ActorLogging
import java.net.InetSocketAddress
import akka.util.ByteString

class Consumer extends Actor with ActorLogging {

  val client = context.actorOf(TCPClient.props(new InetSocketAddress("localhost", 2222)))

  override def receive: Receive = {
      case "connected" => {
        client ! ByteString.fromArray("en".getBytes("UTF-8"))
        Thread.sleep(1000) // ms
        client ! ByteString.fromArray("ar".getBytes("UTF-8")) 
        // client ! ByteString.fromArray("es".getBytes("UTF-8")) 
      }
  }
}