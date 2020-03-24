package car.advert.repository

import doobie.Fragment
import doobie.Query0
import doobie.Read
import doobie._
import doobie.implicits._
import doobie.refined.implicits._

import car.advert.http.Pagination
import car.advert.model.Instances._

trait SqlPaginationSupport {

  def paginate[A: Read](pagination: Pagination)(query: Fragment): Query0[A] = {
    import pagination._
//    (query ++ fr" OFFSET $offset LIMIT $limit").queryWithLogHandler[A](LogHandler.jdkLogHandler)
    (query ++ fr" OFFSET $offset LIMIT $limit").query[A]
  }
}
