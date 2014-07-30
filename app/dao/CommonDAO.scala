package dao


import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import scala.concurrent.Future
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.MongoController
import play.api.mvc.Controller
import models.Worklog
import org.joda.time.DateTime
import models.User
import reactivemongo.core.commands.LastError
import play.api.libs.json.JsObject
import play.modules.reactivemongo.json.collection.JSONCollection

trait CommonDAOComponent {
  val worklogDAO: WorklogDAO
  val userDAO: UserDAO
}

trait WorklogDAO extends BaseDAO[Worklog]{  
  def create(issueKey:String, start:DateTime, end:DateTime, comments:Seq[String])(implicit user: User): Future[BSONObjectID]
}

trait UserDAO extends BaseDAO[User] {
  def findOneByUsername(username:String):Future[Option[User]]
}

trait MongoCommonDAOComponent extends CommonDAOComponent {

  val worklogDAO = new MongoWorklogDAO
  val userDAO = new MongoUserDAO

  class MongoWorklogDAO extends BaseReactiveMongoDAO[Worklog] with WorklogDAO{
    override def coll = db.collection[JSONCollection]("Worklogs")
    
    override def create(issueKey:String, start:DateTime, end:DateTime, comments:Seq[String])(implicit user: User): Future[BSONObjectID] = {
      val worklog = Worklog(issueKey, user.userId, start, end, comments, false)
      insert(worklog)
    }
  }
  
  class MongoUserDAO extends BaseReactiveMongoDAO[User] with UserDAO {
    
    override def coll = db.collection[JSONCollection]("Users")
    
    override def findOneByUsername(username:String): Future[Option[User]] = {
      findOne(Json.obj("username" -> username))
    }
  }
}