package producer

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.util.ByteString
import java.net.InetSocketAddress
import com.typesafe.config.ConfigFactory

class TweetMessageSerializer extends Actor with ActorLogging {
  val client = context.actorSelection("/user/TCPClient")

  override def receive = {
    case tweetMessage: TweetMessage => {
      val tweetMessageByteArray = s"${tweetMessage.TweetId},${tweetMessage.Topic}".getBytes("UTF-8");
      client ! ByteString.fromArray(tweetMessageByteArray) 
    }
    case _ => log.info(this + " Unknown message ")
  }
}