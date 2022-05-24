package messagebroker

import akka.actor.ActorSystem
import akka.actor.Props

// mongod --port 27017 --dbpath 'C:\Program Files\MongoDB\Server\5.0\data' --replSet rs0
// C:\Users\user\AppData\Local\Programs\mongosh => enter => rs.initiate()
object Main extends App  {
  implicit val system = ActorSystem("MessageBroker");

  val supervisor = system.actorOf(Props[Supervisor], "Supervisor")
 }