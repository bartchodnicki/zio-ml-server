package example

import example.models._
import example.predictor.Predictor
import ml.combust.mleap.runtime.frame.{DefaultLeapFrame, Row, Transformer}
import ml.combust.mleap.tensor.DenseTensor
import zhttp.http._
import zhttp.service.Server
import zio._
import zio.json._


object predictor {
  type Predictor = Has[Predictor.Service]

  object Predictor {
    trait Service {
      def makePrediction(features: Features): ZIO[Any, Throwable, Prediction]
    }

    val live: ZLayer[Has[Transformer], Nothing, Has[Service]] = ZLayer.fromService(make)

    def make(transformer: Transformer): Service = (features: Features) => {
      for {
        feature <- if (features.f1 > 0) UIO(features.f1) else ZIO.fail(InvalidFeaturesError("Features should be greater than 0"))
        dataset <- ZIO.succeed(Seq(Row(DenseTensor(Array(feature), List(1)))))
        frame <- ZIO.succeed(DefaultLeapFrame(transformer.inputSchema, dataset))
        result <- ZIO.effect(transformer.transform(frame).get.dataset.head(1).asInstanceOf[Double])
      } yield Prediction(result)
    }

    def makePrediction(features: Features): ZIO[Predictor, Throwable, Prediction] = ZIO.accessM[Predictor](_.get.makePrediction(features))
  }
}


object MlServer extends App {
  private val prediction: Http[predictor.Predictor, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req@Method.POST -> !! / "predict" =>
      for {
        body <- req.bodyAsString.debug
        features <- ZIO.fromEither(body.fromJson[Features].left.map(BadJsonError)).debug
        prediction <- ZIO.accessM[Predictor](_.get.makePrediction(features))
        response <- UIO(prediction.toJson).debug
      } yield Response.json(response)
  }

  private val transformerLayer: ZLayer[Any, Throwable, Has[Transformer]] = ZLayer.fromManaged(
    ZManaged.fromTry(MlTransformer.load)
  )
  private val env: ZLayer[Any, Throwable, Has[Predictor.Service]] = transformerLayer >>> predictor.Predictor.live

  val app: Http[Any, Nothing, Request, Response] = prediction.http.provideLayer(env)
    .catchAll {
      case th: BadFeaturesError => Http.response(Response(
        status = Status.BAD_REQUEST,
        headers = Headers(HeaderNames.contentType, HeaderValues.applicationJson),
        data = HttpData.fromString(PredictionError(th.message).toJson, HTTP_CHARSET)
      ))
      case NoBundleError(_) => Http.response(Response(
          status = Status.INTERNAL_SERVER_ERROR,
          headers = Headers(HeaderNames.contentType, HeaderValues.applicationJson),
          data = HttpData.fromString(PredictionError("Missing ML bundle").toJson, HTTP_CHARSET)
        ))
      case _ =>
        Http.response(Response(
          status = Status.INTERNAL_SERVER_ERROR,
          headers = Headers(HeaderNames.contentType, HeaderValues.applicationJson),
          data = HttpData.fromString(PredictionError("Internal error").toJson, HTTP_CHARSET)
        ))
    }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    Server.start(8090, app)
      .exitCode
  }
}