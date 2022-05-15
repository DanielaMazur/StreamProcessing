package streamprocessing

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.util.ByteString
import java.net.InetSocketAddress

class TweetMessageSerializer extends Actor with ActorLogging {
  val client = context.actorOf(TCPClient.props(new InetSocketAddress("localhost", 1111)), "TCPClient")

  override def receive = {
    case tweetMessage: TweetMessage => {
      val tweetMessageByteArray = s"${tweetMessage.TweetId},${tweetMessage.Topic}".getBytes("UTF-8");
      client ! ByteString.fromArray(tweetMessageByteArray) 
    }
    case _ => log.info(this + " Unknown message ")
  }
}