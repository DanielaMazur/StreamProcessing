package streamprocessing

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import streamprocessing.IPanicMessage

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Random
import scala.util.Success
  
class SentimentWorker extends Worker {
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val futureResponse = Http(context.system).singleRequest(HttpRequest(
    uri =  Uri("http://localhost:4000/emotion_values"),
 
  ))
  val emotionValuesFuture = Await.ready(futureResponse, Duration.Inf).value.get

  val emotion_values_map: Map[String, Int] = emotionValuesFuture match {
    case Success(res) => Await.ready(Unmarshal(res.entity).to[String].map(x => x.split("\n").map(keyValue => 
      {
        val keyValuePair = keyValue.split("\\s+");
        keyValuePair(0) -> keyValuePair(1).toInt
      }
    ).collect { case t@(_: String, _: Int) => t }.toMap), Duration.Inf).value.get.getOrElse(Map[String, Int]()) 
    case Failure(e) =>  Map[String, Int]()
  }

  override def receive = {
    case event: JsValue => {
      try {
        val text = (event \ "message" \ "tweet" \ "text").as[String]
        val score = text.split("\\s+").map(word => emotion_values_map.getOrElse(word, 0)).sum
        log.info("Sentiment Score " + score)
        Thread.sleep(Random.between(50, 500))
      }catch{
        case _: Throwable => log.warning("SentimentWorker => Something went wrong")
      }
    }
  }
}