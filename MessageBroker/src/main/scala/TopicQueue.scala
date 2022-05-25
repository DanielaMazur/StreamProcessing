package messagebroker

import akka.actor.Actor
import scala.collection.mutable.Queue
import akka.actor.ActorRef
import akka.io.Tcp
import akka.util.ByteString
import akka.actor.Props
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Cancellable

object TopicQueue {
  def props(subscriber: ActorRef) = Props(classOf[TopicQueue], subscriber)
}

class TopicQueue(subscriber: ActorRef) extends Actor {
  var queue = Queue[TweetMessage]()
  var numberOfRetries = 0
  var scheduledRetry: Cancellable = null

  override def receive: Receive = {
    case message: TweetMessage => {
      queue.enqueue(message)
    }
    case "ack" => {
      queue.dequeue()
      scheduledRetry.cancel()
    }
    case "send" => {
      val message = queue.front
      subscriber ! Tcp.Write(ByteString(s"${message.TweetId},${message.Topic}\n"))
      scheduledRetry = context.system.scheduler.scheduleOnce(100.milliseconds, self, "retry")
    }
    case "retry" => {
      if(numberOfRetries == 3){
        context.parent ! "shutdown"
      }else {
        self ! "send"
        numberOfRetries += 1
      }
    }
  }
}