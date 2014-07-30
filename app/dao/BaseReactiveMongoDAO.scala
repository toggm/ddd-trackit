package dao

import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.Format
import play.modules.reactivemongo.json.BSONFormats._
import scala.concurrent.ExecutionContext
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import reactivemongo.core.protocol.Query
import reactivemongo.api.QueryOpts
import play.api.libs.json.JsUndefined
import play.api.libs.json.JsString
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject

trait BaseDAO[T] {
  def coll: JSONCollection

  def get(id: BSONObjectID)(implicit ctx: ExecutionContext): Future[Option[(T, BSONObjectID)]]

  def insert(t: T)(implicit ctx: ExecutionContext): Future[BSONObjectID]

  def find(sel: JsObject, limit: Int = 0, skip: Int = 0, sort: JsObject = Json.obj(), projection: JsObject = Json.obj())(implicit ctx: ExecutionContext): Future[Traversable[(T, BSONObjectID)]]
  
  def findOne(sel: JsObject)(implicit ctx: ExecutionContext): Future[Option[T]]
}

abstract class BaseReactiveMongoDAO[T](implicit ctx: ExecutionContext, format: Format[T]) extends BaseDAO[T] {

  lazy val db = ReactiveMongoPlugin.db

  def get(id: BSONObjectID)(implicit ctx: ExecutionContext): Future[Option[(T, BSONObjectID)]] = {
    coll.find(Json.obj("_id" -> id)).cursor[JsObject].headOption.map(_.map(js => (js.as[T], id)))
  }

  def insert(t: T)(implicit ctx: ExecutionContext): Future[BSONObjectID] = {
    val id = BSONObjectID.generate
    val obj = format.writes(t).as[JsObject]
    obj \ "_id" match {
      case _: JsUndefined =>
        coll.insert(obj ++ Json.obj("_id" -> id))
          .map { _ => id }

      case JsObject(Seq((_, JsString(oid)))) =>
        coll.insert(obj).map { _ => BSONObjectID(oid) }

      case JsString(oid) =>
        coll.insert(obj).map { _ => BSONObjectID(oid) }

      case f => sys.error(s"Could not parse _id field: $f")
    }
  }

  def find(sel: JsObject, limit: Int = 0, skip: Int = 0, sort: JsObject = Json.obj(), projection: JsObject = Json.obj())(implicit ctx: ExecutionContext): Future[Traversable[(T, BSONObjectID)]] = {
    val cursor = coll.find(sel).projection(projection).sort(sort).options(QueryOpts().skip(skip)).cursor[JsObject]
    val l = if (limit != 0) cursor.collect[Traversable](limit) else cursor.collect[Traversable]()
    l.map(_.map(js => (js.as[T], (js \ "_id").as[BSONObjectID])))
  }
  
  def findOne(sel: JsObject)(implicit ctx: ExecutionContext): Future[Option[T]] = {
    val cursor = coll.find(sel).cursor[JsObject]
    val l = cursor.collect[Traversable](1)
    l.map(_.headOption.map(js => js.as[T]))
  }

  def findStream(sel: JsObject, skip: Int = 0, pageSize: Int = 0)(implicit ctx: ExecutionContext): Enumerator[TraversableOnce[(T, BSONObjectID)]] = {
    val cursor = coll.find(sel).options(QueryOpts().skip(skip)).cursor[JsObject]
    val enum = if (pageSize != 0) cursor.enumerateBulks(pageSize) else cursor.enumerateBulks()
    enum.map(_.map(js => (js.as[T], (js \ "_id").as[BSONObjectID])))
  }
}