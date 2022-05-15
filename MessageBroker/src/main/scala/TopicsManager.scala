package messagebroker

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.util.ByteString
import akka.io.Tcp
import scala.collection.mutable

class TopicsManager extends Actor {
  var topicQueues: Map[String, ActorRef] = Map()
  
  override def receive: Receive = {
    case message: TweetMessage => {
      if(topicQueues.contains(message.Topic)){
        topicQueues(message.Topic) ! message
      }else {
        val newTopicQueue = context.actorOf(Props[TopicQueue])
        topicQueues += (message.Topic -> newTopicQueue)
        newTopicQueue ! message
      }
    }
    case (lang: String, replyTo: ActorRef) => {
      if(topicQueues.contains(lang)){
        topicQueues(lang) ! replyTo
      }else {
        replyTo ! Tcp.Write(ByteString("Sorry, the topic you are looking for is missing, available topics are: " + topicQueues.keys))
      }
    }
  }
}