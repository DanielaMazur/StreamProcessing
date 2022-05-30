package messagebroker

import akka.actor.ActorSystem
import java.net.InetSocketAddress
import akka.actor.Props
import akka.actor.Actor
import com.typesafe.config.ConfigFactory

class Supervisor extends Actor {
  val messageDeserializer = context.actorOf(Props[MessageDeserializer], "MessageDeserializer")
  val consumerConnectionHandler = context.actorOf(Props[ConsumerConnectionHandler], "ConsumerConnectionHandler")
  val consumerQueuesManager = context.actorOf(Props[ConsumerQueuesManager], "ConsumerQueuesManager")

  val config = ConfigFactory.load().getConfig("MessageBrokerConfig")
  val messageBrokerHost = config.getString("messageBrokerHost");
  val messageBrokerConsumerPort = config.getInt("messageBrokerConsumerPort");
  val messageBrokerProducerPort = config.getInt("messageBrokerProducerPort");

  val producerConnection = context.actorOf(TCPServer.props(new InetSocketAddress(messageBrokerHost, messageBrokerProducerPort), messageDeserializer), "ProducerConnection");
  val consumerConnection = context.actorOf(TCPServer.props(new InetSocketAddress(messageBrokerHost, messageBrokerConsumerPort), consumerConnectionHandler), "ConsumerConnection");

  override def postStop(): Unit = {
    // log.info(this + " shutting down")
  }

  override def receive: Receive = {
    case _ => {}
  }
}