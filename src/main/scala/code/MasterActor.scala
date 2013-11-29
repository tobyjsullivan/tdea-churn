package code

import akka.actor._
import scala.concurrent.duration._

object MasterActor {
  def props(min: Long, max: Long) = Props(new MasterActor(min, max))
  
  case class Chunk(min: Long, max: Long)
  
  case object SpawnCruncher
  
  case class ReportPulse(oldMax: Long, oldTime: Long)
}

class MasterActor(min: Long, max: Long) extends Actor {
  import context.dispatcher
  
  val chunkSize = 1000000L
  val numCrunchers = 4
  
  val reportRate = 15 seconds
  
  var lastMax = min
  
  context.system.scheduler.scheduleOnce(reportRate, self, MasterActor.ReportPulse(min, System.currentTimeMillis()))
  
  for {
    i <- 0 until numCrunchers
  } self ! MasterActor.SpawnCruncher
  
  def assignNextChunk(crunchActor: ActorRef) {
    val min = lastMax + 1
    val max = min + chunkSize
    val chunk = MasterActor.Chunk(min, max)
    
//    println(s"Starting range: $min - $max")
    
    crunchActor ! CrunchActor.WorkRange(min, max)
    
    lastMax = max
  }
  
  def receive: Receive = {
    case MasterActor.SpawnCruncher =>
      assignNextChunk(context.actorOf(CrunchActor.props))
      
    case CrunchActor.Done => {
      assignNextChunk(sender)
    }
    
    case CrunchActor.Result(x) => {
      println("SUCCESS!!!!! " + x)
      // TODO: Write to file
      
      context.system.shutdown
    }
    
    case MasterActor.ReportPulse(oldMax, oldTime) => {
      val newMax = lastMax
      val newTime = System.currentTimeMillis()
      
      val crunched = newMax - oldMax
      val secondsElapsed = (newTime - oldTime).toDouble / 1000
      
      val rate = crunched.toDouble / secondsElapsed
      
      println("Rate: " + rate + "/second");
      
      context.system.scheduler.scheduleOnce(reportRate, self, MasterActor.ReportPulse(newMax, newTime))
    }
  }
}