package messagebroker

import akka.actor.Actor
import akka.io.{ Tcp }
import akka.util.ByteString
import akka.actor.ActorRef

class ConsumerConnectionHandler extends Actor {

  import Tcp._
 
  case class TopicsList(topics: Iterable[String])
  val consumerQueuesManager = context.actorSelection("/user/Supervisor/ConsumerQueuesManager")

  override def receive: Receive = {
    case Received(data) => {
      val lang = new String(data.toArray, "UTF-8").trim()
      consumerQueuesManager ! (lang, sender())
    } 
    case PeerClosed     => context.stop(self)
  }
}