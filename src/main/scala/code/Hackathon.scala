package code

import akka.actor._

object Hackathon extends App {
  val locked = "EQ7fIpT7i/Q="
  val res = "123456"
    
  val startVal = 195807001737L
  val uberMaxVal = 281474976710656L
  
  val system = ActorSystem("hackathon")
  
  val master = system.actorOf(MasterActor.props(startVal, uberMaxVal))
  
  
  
}
