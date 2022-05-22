package messagebroker

import akka.actor.Actor
import akka.io.{ Tcp }

class MessageDeserializer extends Actor {
  import Tcp._
  val consumerQueuesManager = context.actorSelection("/user/Supervisor/ConsumerQueuesManager")

  override def receive: Receive = {
    case Received(data) => {
      val tweetDetails = new String(data.toArray, "UTF-8").split(",")
      val tweetMessage = new TweetMessage(tweetDetails(0), tweetDetails(1))
      consumerQueuesManager ! tweetMessage
    } 
    case PeerClosed     => context.stop(self)
  }
}