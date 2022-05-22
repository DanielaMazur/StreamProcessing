package messagebroker

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.util.ByteString
import akka.io.Tcp
import scala.collection.mutable
import akka.actor.SupervisorStrategy
import scala.collection.mutable.Queue
import akka.actor.ActorLogging

object TopicsManager {
  def props(subscriber: ActorRef) = Props(classOf[TopicsManager], subscriber)
}

class TopicsManager(subscriber: ActorRef) extends Actor with ActorLogging {
  var topicQueues: Map[String, ActorRef] = Map()
  var messagesBuffer = Queue[String]()
  var isFirstMessage = true;

  override def receive: Receive = {
    case message: TweetMessage => {
      if(topicQueues.contains(message.Topic)){
        topicQueues(message.Topic) ! message
        messagesBuffer.enqueue(message.Topic)
        if(isFirstMessage){
          isFirstMessage = false;
          topicQueues(messagesBuffer.front) ! "send"
        }
      }
    }
    case "ack" => {
      try {
        topicQueues(messagesBuffer.dequeue()) ! "ack"
        topicQueues(messagesBuffer.front) ! "send"
      }catch{
        case _: Throwable => log.info("MessagesBuffer queue was empty")
      }
    }
    case "shutdown" => {
      context.parent ! ("shutdown", self)
      self ! SupervisorStrategy.Stop
    }
    case (lang: String) => {
      if(!topicQueues.contains(lang)){
        val newTopicQueue = context.actorOf(TopicQueue.props(subscriber))
        topicQueues += (lang -> newTopicQueue)
      }
    }
  }
}