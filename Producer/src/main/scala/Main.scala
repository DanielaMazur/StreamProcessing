package producer

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import java.net.InetSocketAddress

object Main extends App {
  implicit val system = ActorSystem("StreamProcessing", ConfigFactory.load().getConfig("ProducerConfig"))

  val config = ConfigFactory.load().getConfig("ProducerConfig")
  val messageBrokerHost = config.getString("messageBrokerHost");
  val messageBrokerPort = config.getInt("messageBrokerPort");

  val CDCMongoDBProducer = system.actorOf(Props[CDCMongoDBProducer], "CDCMongoDBProducer");
  val TweetMessageSerializer = system.actorOf(Props[TweetMessageSerializer], "TweetMessageSerializer");
  val client = system.actorOf(TCPClient.props(new InetSocketAddress(messageBrokerHost, messageBrokerPort)), "TCPClient")
}