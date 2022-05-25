package consumer

import akka.actor.Actor
import akka.actor.ActorLogging
import java.net.InetSocketAddress
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

class Consumer extends Actor with ActorLogging {
  val config = ConfigFactory.load().getConfig("ConsumerConfig")
  val messageBrokerHost = config.getString("messageBrokerHost");
  val messageBrokerPort = config.getInt("messageBrokerPort");
  val client = context.actorOf(TCPClient.props(new InetSocketAddress(messageBrokerHost, messageBrokerPort)))

  override def receive: Receive = {
      case "connected" => {
        client ! ByteString.fromArray("en".getBytes("UTF-8"))
        Thread.sleep(1000) // ms
        client ! ByteString.fromArray("ar".getBytes("UTF-8")) 
        // client ! ByteString.fromArray("es".getBytes("UTF-8")) 
      }
  }
}