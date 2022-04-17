package streamprocessing

import play.api.libs.json.JsValue
import scala.util.Random
import streamprocessing.IPanicMessage

class SentimentWorker extends Worker {

  override def receive = {
    case event: JsValue => {
      try {
        log.info((event \ "message" \ "tweet" \ "text").as[String])
        Thread.sleep(Random.between(50, 500))
      }catch{
        case _: Throwable => ???
      }
    }
  }
}