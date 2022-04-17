package streamprocessing

import play.api.libs.json.JsValue
import scala.util.Random
import streamprocessing.IPanicMessage

class EngagementWorker extends Worker {
  override def receive = {
    case event: JsValue => {
      try {
        log.info((event \ "message" \ "tweet" \ "text").as[String])
        val favorites = (event \ "message" \ "tweet" \ "favorite_count").as[Int]
        val retweets = (event \ "message" \ "tweet" \ "retweet_count").as[Int]
        val followers = (event \ "message" \ "tweet" \ "user" \ "followers_count").as[Int]
        val engagementRatio = (favorites + retweets) / followers
        log.info("Engagement ratio " + engagementRatio)
        Thread.sleep(Random.between(50, 500))
      }catch{
        case _: Throwable => ???
      }
    }
  }
}