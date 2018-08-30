package gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import play.api.Play
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{Helpers, TestServer}
import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  val port = Helpers.testServerPort
  val app = new GuiceApplicationBuilder().build()
  val testServer = TestServer(port, app)

  val httpConf = http
      .baseURL(s"http://localhost:$port")

  val noOverheadScn = scenario("noOverhead")
    .exec(http("/ok").get("/ok"))

  val nunjucksScn = scenario("basic")
    .exec(http("/").get("/"))
    .pause(5)

  setUp(
    noOverheadScn.inject(
      constantUsersPerSec(10) during 10.seconds
    ),
    nunjucksScn.inject(
      constantUsersPerSec(10) during 10.seconds
    )
  ).protocols(httpConf)

  before {
    testServer.start()
  }

  after {
    testServer.stop()
  }
}
