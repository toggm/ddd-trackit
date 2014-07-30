package models

import play.api.libs.json.Json
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID

sealed class BackendSystem(systemId:BSONObjectID) {  
}

case class JiraSystem(systemId: BSONObjectID, url: String, username:String, password:Seq[Char]) extends BackendSystem(systemId)
