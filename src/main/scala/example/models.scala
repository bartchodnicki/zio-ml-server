package example

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

object models {
  // Request
  case class Features(f1: Double)

  // Response
  case class Prediction(result: Double)

  // Error response
  case class PredictionError(message: String)

  implicit val featuresDecoder: JsonDecoder[Features] = DeriveJsonDecoder.gen[Features]
  implicit val predictionEncoder: JsonEncoder[Prediction] = DeriveJsonEncoder.gen[Prediction]
  implicit val predictionErrorEncoder: JsonEncoder[PredictionError] = DeriveJsonEncoder.gen[PredictionError]


  case class NoBundleError(msg: String) extends Exception(msg)

  sealed trait BadFeaturesError extends Exception {
    def message: String
  }
  case class BadJsonError(override val message: String) extends BadFeaturesError
  case class InvalidFeaturesError(override val message: String) extends BadFeaturesError
}
