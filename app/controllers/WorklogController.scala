package controllers

import play.api.mvc.Controller
import org.joda.time.DateTime
import dao.CommonDAOComponent
import dao.MongoCommonDAOComponent
import play.api.mvc.Action
import core.Secured
import play.api.libs.concurrent.Execution.Implicits._

class WorklogController extends Controller  with Secured {
  this: CommonDAOComponent =>
	def appendWorklog(issueKey:String, start:DateTime, end:DateTime, comment:Option[String]=None) = 
	  withUser { implicit user => request =>
	  //worklogDAO.create(issueKey, start, end, Seq(comment)) map (_ => Ok)
	  worklogCommandDAO.append(issueKey, start, end, comment) map (_ => Ok)
	}
	
	def startWorklog(issueKey:String, start:DateTime=DateTime.now) = {
	  withUser { implicit user => request => 
	  	worklogCommandDAO.start(issueKey, start) map (_ => Ok)
	  }
	}
	
	def endWorklog(issueKey:String, end:DateTime=DateTime.now, comment:Option[String]=None) = {
	  withUser { implicit user => request => 
	  	worklogCommandDAO.end(issueKey, end,comment) map (_ => Ok)
	  }
	}
}

object WorklogController extends WorklogController with MongoCommonDAOComponent