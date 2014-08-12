package dao

import scala.concurrent.Future

import models.User
import models.Worklog
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.modules.reactivemongo.json.collection.JSONCollection

trait CommonDAOComponent {
  val worklogDAO: WorklogDAO
  val userDAO: UserDAO
}

trait WorklogDAO extends BaseDAO[Worklog] {
  def findOpenByIssueId(issueKey: String, userId: String): Future[Option[Worklog]]

  def delete(worklog: Worklog)
}

trait UserDAO extends BaseDAO[User] {
  def findOneByUsername(username: String): Future[Option[User]]
}

trait MongoCommonDAOComponent extends CommonDAOComponent {

  val worklogDAO = new MongoWorklogDAO
  val userDAO = new MongoUserDAO

  class MongoWorklogDAO extends BaseReactiveMongoDAO[Worklog] with WorklogDAO {
    override def coll = db.collection[JSONCollection]("Worklogs")

    override def findOpenByIssueId(issueKey: String, userId: String): Future[Option[Worklog]] = {
      findOne(Json.obj("issueKey" -> issueKey, "userId" -> userId, "end" -> ""))
    }

    override def delete(worklog: Worklog) = {
      //TODO: implement
    }
  }

  class MongoUserDAO extends BaseReactiveMongoDAO[User] with UserDAO {

    override def coll = db.collection[JSONCollection]("Users")

    override def findOneByUsername(username: String): Future[Option[User]] = {
      findOne(Json.obj("username" -> username))
    }
  }
}