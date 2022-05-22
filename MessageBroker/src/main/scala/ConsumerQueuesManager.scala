package messagebroker

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import scala.collection.mutable.Map

class ConsumerQueuesManager extends Actor {
  val consumerTopicManagerMap = Map[ActorRef, ActorRef]();

  override def receive: Receive = {
    case ("shutdown", sender: ActorRef) => {
      consumerTopicManagerMap.remove(sender)
    }
    case (lang: String, sender: ActorRef) => {
        if(!consumerTopicManagerMap.contains(sender)){
          val topicsManager = context.actorOf(TopicsManager.props(sender))
          consumerTopicManagerMap += (sender -> topicsManager)
        }
        consumerTopicManagerMap(sender) ! lang
    }
    case message: TweetMessage => {
      consumerTopicManagerMap.values.foreach(topicManager => topicManager ! message)
    }
  }
}