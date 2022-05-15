package messagebroker

import akka.actor.Actor
import akka.io.{ Tcp }

class MessageDeserializer extends Actor {
  import Tcp._
  val topicsManager = context.actorSelection("/user/Supervisor/TopicsManager")

  override def receive: Receive = {
    case Received(data) => {
      val tweetDetails = new String(data.toArray, "UTF-8").split(",")
      val tweetMessage = new TweetMessage(tweetDetails(0), tweetDetails(1))
      topicsManager ! tweetMessage
    } 
    case PeerClosed     => context.stop(self)
  }
}