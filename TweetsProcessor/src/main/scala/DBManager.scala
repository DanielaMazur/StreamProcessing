package tweetsprocessor

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.stream.impl.Completed
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.Observer
import org.mongodb.scala.bson.collection.mutable.Document
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject

import scala.collection.mutable.ListBuffer
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

 
class DBManager extends Actor with ActorLogging {
  val batchSize: Int = ConfigFactory.load().getConfig("StreamProcessingConfig").getInt("akka.actor.deployment.dbBatchSize");
  var pendingTweets = ListBuffer[JsObject]()

  val mongodbUrl = ConfigFactory.load().getConfig("StreamProcessingConfig").getString("mongodbHost");

  val mongoClient: MongoClient = MongoClient(mongodbUrl)
  val database: MongoDatabase = mongoClient.getDatabase("TweetsDB")

  database.getCollection("Users").drop().subscribe((_)=>{});
  database.getCollection("Tweets").drop().subscribe((_)=>{});

  database.createCollection("Users").subscribe((_)=>{});
  database.createCollection("Tweets").subscribe((_)=>{});

  val usersCollection: MongoCollection[Document] = database.getCollection("Users")
  val tweetsCollection: MongoCollection[Document] = database.getCollection("Tweets")

  case class TweetsBatch(tweetsBatch: ListBuffer[JsObject])

  override def receive: Receive = {
    case tweet: JsObject => {
      pendingTweets.addOne(tweet)
      if(pendingTweets.length >= batchSize){
        val batch = pendingTweets.take(batchSize)
        pendingTweets.remove(0, batchSize)
        self ! TweetsBatch(batch)
      }
    }
    case TweetsBatch(tweetsBatch) => {
      var users = Seq[Document]();
      var tweets = Seq[Document]();

      tweetsBatch.foreach(tweet => {
        val userJson = (tweet \ "message" \ "tweet" \ "user").as[JsObject]
        val tweetJson = ((tweet \ "message" \ "tweet").as[JsObject] - "user").as[JsObject]
        users = users :+ Document(userJson.toString());
        tweets = tweets :+ Document(tweetJson.toString());
      })
      
      usersCollection.insertMany(users).subscribe((_) => {});
      tweetsCollection.insertMany(tweets).subscribe((_) => {});
    }
    case _ => log.info("DBManager Unknown message")
  }
}