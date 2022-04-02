package example

import zhttp.http._
import zhttp.service.Server
import zio._
import zio.json._


object MlServer extends App {

  import models._

  private def makePrediction(features: Features): Either[InvalidFeaturesError, Prediction] = {
    if (features.f1 > 0)
      Right(Prediction(MlTransformer.predict(features.f1)))
    else
      Left(InvalidFeaturesError("Features should be greater than 0"))
  }

  private val prediction: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req@Method.POST -> !! / "predict" =>
      for {
        body <- req.bodyAsString.debug
        features <- ZIO.fromEither(body.fromJson[Features].left.map(JsonError)).debug
        prediction <- ZIO.fromEither(makePrediction(features)).debug
        response <- UIO(prediction.toJson).debug
      } yield Response.json(response)
  }

  val app: HttpApp[Any, Throwable] = prediction.http.catchAll {
    case th: BadFeaturesError => Http.response(Response(
      status = Status.BAD_REQUEST,
      headers = Headers(HeaderNames.contentType, HeaderValues.applicationJson),
      data = HttpData.fromString(PredictionError(th.message).toJson, HTTP_CHARSET)
    ))
  }


  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    Server.start(8090, app).exitCode
  }
}