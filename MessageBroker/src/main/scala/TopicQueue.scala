package messagebroker

import akka.actor.Actor
import scala.collection.mutable.Queue
import akka.actor.ActorRef
import akka.io.Tcp
import akka.util.ByteString

class TopicQueue extends Actor {
  var queue = Queue[TweetMessage]()
  var subscribers = List[ActorRef]()

  override def receive: Receive = {
    case message: TweetMessage => {
      queue.enqueue(message)
      subscribers.foreach(s => s ! Tcp.Write(ByteString(s"${message.TweetId},${message.Topic}")))
    }
    case sender: ActorRef => {
      subscribers = subscribers :+ sender
    }
  }
}