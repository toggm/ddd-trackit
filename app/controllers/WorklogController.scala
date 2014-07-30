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
	def appendWorklog(issueKey:String, start:DateTime, end:DateTime, comment:String) = 
	  withUser { implicit user => request =>
	  worklogDAO.create(issueKey, start, end, Seq(comment)) map (_ => Ok)	  
	}	
}

object WorklogController extends WorklogController with MongoCommonDAOComponent