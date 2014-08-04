package handler

import dao.CommonDAOComponent
import akka.actor.{ Actor, Props }
import models.StartWorklog
import models.EndWorklog
import models.AppendWorklog

class WorklogCommandHandler extends Actor {
  this: CommonDAOComponent => 
    
	def receive = {
	  case StartWorklog =>
	    
	  case EndWorklog =>
	    
	  case AppendWorklog =>
	}
}

object WorklogCommandHandler {
  def props() = Props(classOf[WorklogCommandHandler])
}