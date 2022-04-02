package example


import ml.combust.bundle.BundleFile
import ml.combust.bundle.dsl.Bundle
import ml.combust.mleap.runtime.MleapSupport._
import ml.combust.mleap.runtime.frame.{DefaultLeapFrame, Row, Transformer}
import ml.combust.mleap.tensor.DenseTensor
import resource.{ExtractableManagedResource, managed}

object MlTransformer {

  def load: Transformer = {
    val bundle: ExtractableManagedResource[Bundle[Transformer]] = for (bf <- managed(BundleFile(MlTransformer.getClass.getResource("/ml-bundle").toString))) yield {
      bf.loadMleapBundle().get
    }
    bundle.opt.get.root
  }

  private val transformer: Transformer = load

  def predict(feature: Double): Double = {
    val dataset = Seq(Row(DenseTensor(Array(feature), List(1))))
    val frame = DefaultLeapFrame(transformer.inputSchema, dataset)
    transformer.transform(frame).get.dataset.head(1).asInstanceOf[Double]
  }
}