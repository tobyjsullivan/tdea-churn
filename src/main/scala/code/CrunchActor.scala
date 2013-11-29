package code

import akka.actor._

import code.ZiggyTest._

object CrunchActor {
  def props = Props[CrunchActor]
  
  case class WorkRange(min: Long, max: Long)
  case object Done
  case class Result(res: Long)
}

class CrunchActor extends Actor {
  val ziggyCtx = generateContext()
  
	def receive: Receive = {
	  case CrunchActor.WorkRange(min, max) => {
	    
	    val res = testRange(min, max, ziggyCtx)
	    
	    println(s"Finished range: $min - $max")
	    
	    if(res != -1) sender ! CrunchActor.Result(res)
	    
	    sender ! CrunchActor.Done
	  }
	}
}