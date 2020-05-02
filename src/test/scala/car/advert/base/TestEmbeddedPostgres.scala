package car.advert.base

import cats.effect.{ Blocker, ContextShift, IO }

import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor

import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, Suite }

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import org.flywaydb.core.Flyway

trait TestEmbeddedPostgres extends BeforeAndAfterEach with BeforeAndAfterAll { self: Suite =>

  private lazy val database   = "postgres"
  private lazy val dbUsername = "postgres"
  private lazy val dbPass     = "postgres"

  private lazy val pg: EmbeddedPostgres = EmbeddedPostgres.start()
  private lazy val url: String          = pg.getJdbcUrl(dbUsername, database)
  private lazy val flyWay: Flyway       = Flyway.configure().dataSource(url, dbUsername, dbPass).load()

  private val blocker: Blocker                = Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  implicit val cs: ContextShift[IO]           = IO.contextShift(ExecutionContexts.synchronous)
  implicit lazy val xa: doobie.Transactor[IO] = Transactor.fromDriverManager[IO]("org.postgresql.Driver", url, dbUsername, dbPass, blocker)

  override protected def beforeAll(): Unit = {
    super.afterAll()
    flyWay.migrate()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    pg.close()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    flyWay.migrate()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    flyWay.clean()
  }

}
