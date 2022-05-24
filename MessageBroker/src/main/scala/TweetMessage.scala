package messagebroker

import org.mongodb.scala.bson.annotations.BsonProperty

case class TweetMessage(@BsonProperty("id_str") val TweetId : String, @BsonProperty("lang") val Topic : String) {}