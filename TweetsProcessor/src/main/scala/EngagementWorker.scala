package tweetsprocessor

import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json

import scala.util.Random
import akka.actor.ActorRef

class EngagementWorker extends Worker {
  override def receive = {
    case (event: JsValue, replyTo: ActorRef) => {
      try {
        log.info((event \ "message" \ "tweet" \ "text").as[String])
        val favorites = (event \ "message" \ "tweet" \ "favorite_count").as[Int]
        val retweets = (event \ "message" \ "tweet" \ "retweet_count").as[Int]
        val followers = (event \ "message" \ "tweet" \ "user" \ "followers_count").as[Int]
        var engagementRatio = 0
        if(followers != 0){
          engagementRatio = (favorites + retweets) / followers
        }
        replyTo ! event.as[JsObject] + ("engagement_ratio" -> Json.toJson(engagementRatio))
        Thread.sleep(Random.between(50, 500))
      }catch{
        case _: Throwable => log.warning("EngagementWorker => Something went wrong")
      }
    }
  }
}