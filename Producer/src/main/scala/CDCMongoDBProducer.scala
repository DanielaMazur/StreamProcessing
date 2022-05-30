package producer

import akka.actor.Actor
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.model.changestream.FullDocument
import org.mongodb.scala.bson.collection.immutable.Document
import akka.actor.ActorLogging
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromRegistries, fromProviders}
import com.typesafe.config.ConfigFactory

class CDCMongoDBProducer extends Actor with ActorLogging {
  val tweetMessageSerializer = context.actorSelection("/user/TweetMessageSerializer")
  
  val mongodbUrl = ConfigFactory.load().getConfig("ProducerConfig").getString("mongodbHost");

  val codecRegistry = fromRegistries(fromProviders(classOf[TweetMessage]), MongoClient.DEFAULT_CODEC_REGISTRY)

  val mongoClient: MongoClient = MongoClient(mongodbUrl)
  val database: MongoDatabase = mongoClient.getDatabase("TweetsDB").withCodecRegistry(codecRegistry)

  val tweetsCollection: MongoCollection[TweetMessage] = database.getCollection("Tweets")
  
  val observer = DBObserver(tweetMessageSerializer)
  tweetsCollection.watch().subscribe(observer)
  observer.await()

  override def receive: Receive = {
    case _ => {}
  }
}