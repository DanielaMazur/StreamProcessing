package streamprocessing

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.collection.mutable.Document
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

 
class Aggregator extends Actor with ActorLogging {
  var pendingTweets = Map[String, JsObject]()

  val DBManager = context.actorOf(Props[DBManager], "DBManager")

  override def receive: Receive = {
    case tweet: JsObject => {
      val tweetId = (tweet \ "message" \ "tweet" \ "id_str").as[String]
      val pendingTweet = pendingTweets.get(tweetId)
      pendingTweet match {
        case Some(x) => {
          pendingTweets = pendingTweets.-(tweetId)
          DBManager ! tweet ++ x
        }
        case None => pendingTweets += (tweetId -> tweet)
      }
    }
    case _ => log.info("Aggregator Unknown message")
  }
}