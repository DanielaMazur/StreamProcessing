package messagebroker

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.util.ByteString
import akka.io.Tcp
import scala.collection.mutable
import akka.actor.SupervisorStrategy

object TopicsManager {
  def props(subscriber: ActorRef) = Props(classOf[TopicsManager], subscriber)
}

class TopicsManager(subscriber: ActorRef) extends Actor {
  var topicQueues: Map[String, ActorRef] = Map()
  var topicQueueWaitingAck: ActorRef = null;

  override def receive: Receive = {
    case message: TweetMessage => {
      if(topicQueues.contains(message.Topic)){
        topicQueues(message.Topic) ! message
      }
    }
    case "ack" => {
      if(topicQueueWaitingAck != null){
        topicQueueWaitingAck ! "ack"
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
    case (topicQueue: ActorRef) => {
      if(topicQueueWaitingAck == null){
        topicQueueWaitingAck = topicQueue
        topicQueue ! "send"
      }
    } 
  }
}