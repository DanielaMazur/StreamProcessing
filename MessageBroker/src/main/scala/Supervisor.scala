package messagebroker

import akka.actor.ActorSystem
import java.net.InetSocketAddress
import akka.actor.Props
import akka.actor.Actor

class Supervisor extends Actor {
  val messageDeserializer = context.actorOf(Props[MessageDeserializer], "MessageDeserializer")
  val consumerConnectionHandler = context.actorOf(Props[ConsumerConnectionHandler], "ConsumerConnectionHandler")
  val consumerQueuesManager = context.actorOf(Props[ConsumerQueuesManager], "ConsumerQueuesManager")
  val CDCMongoDBProducer = context.actorOf(Props[CDCMongoDBProducer], "CDCMongoDBProducer");

  val producerConnection = context.actorOf(TCPServer.props(new InetSocketAddress("localhost", 1111), messageDeserializer), "ProducerConnection");
  val consumerConnection = context.actorOf(TCPServer.props(new InetSocketAddress("localhost", 2222), consumerConnectionHandler), "ConsumerConnection");

  override def receive: Receive = {
    case _ => {}
  }
}