package messagebroker

import akka.actor.Actor
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.model.changestream.FullDocument
import org.mongodb.scala.bson.collection.immutable.Document
import akka.actor.ActorLogging
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromRegistries, fromProviders}

class CDCMongoDBProducer extends Actor with ActorLogging {
  val consumerQueuesManager = context.actorSelection("/user/Supervisor/ConsumerQueuesManager")

  val codecRegistry = fromRegistries(fromProviders(classOf[TweetMessage]), MongoClient.DEFAULT_CODEC_REGISTRY)

  val mongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("TweetsDB").withCodecRegistry(codecRegistry)

  val tweetsCollection: MongoCollection[TweetMessage] = database.getCollection("Tweets")
  
  val observer = DBObserver(consumerQueuesManager)
  tweetsCollection.watch().subscribe(observer)
  observer.await()

  override def receive: Receive = {
    case _ => {}
  }
}