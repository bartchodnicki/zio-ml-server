package example

import zio.test._
import zhttp.http._

object MlServerSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("Test server")(
    testM("should return prediction") {
      val app = MlServer.app
      val req = Request(Method.POST, URL(!! / "predict"), Headers(HeaderNames.contentType, HeaderValues.applicationJson),
        data = HttpData.fromString("""{"f1": 0.2}"""))
      assertM(app(req).map(_.status))(Assertion.equalTo(Status.OK))
    } +
      testM("should return error when bad json") {
        val app = MlServer.app
        val req = Request(Method.POST, URL(!! / "predict"), Headers(HeaderNames.contentType, HeaderValues.applicationJson),
          data = HttpData.fromString("""{"feature": 0.2}"""))
        assertM(app(req).map(_.status))(Assertion.equalTo(Status.BAD_REQUEST))
      } +

      testM("should return error when bad feature value") {
        val app = MlServer.app
        val req = Request(Method.POST, URL(!! / "predict"), Headers(HeaderNames.contentType, HeaderValues.applicationJson),
          data = HttpData.fromString("""{"f1": -5}"""))
        assertM(app(req).map(_.status))(Assertion.equalTo(Status.BAD_REQUEST))
      }
  )
}
