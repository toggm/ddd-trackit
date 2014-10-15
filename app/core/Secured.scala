package core

import play.api.mvc.RequestHeader

import play.api.mvc.Results
import play.api.mvc.Security
import play.api.mvc.Request
import controllers.routes
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.mvc.Action
import models.User
import dao.MongoCommonDAOComponent
import dao.CommonDAOComponent
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.mvc.SimpleResult

trait Secured {
  this: CommonDAOComponent =>
  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login)

  def withAuth(f: => String => Request[AnyContent] => Future[SimpleResult]) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action.async {
        request =>
          f(user)(request)
      }
    }
  }

  /**
   * This method shows how you could wrap the withAuth method to also fetch your user
   */
  def withUser(f: User => Request[AnyContent] => Future[SimpleResult]) = withAuth { username =>
    implicit request =>
      userDAO.findOneByUsername(username).flatMap { option =>
        option.map { user =>
          f(user)(request)
        }.getOrElse(Future.successful(onUnauthorized(request)))
      }
  }
}

object Secured extends Secured with MongoCommonDAOComponent
