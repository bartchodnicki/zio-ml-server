package example


import example.models.NoBundleError
import ml.combust.bundle.BundleFile
import ml.combust.bundle.dsl.Bundle
import ml.combust.mleap.runtime.MleapSupport._
import ml.combust.mleap.runtime.frame.Transformer
import resource.{ExtractableManagedResource, managed}

import scala.util.{Failure, Success, Try}

object MlTransformer {

  def load: Try[Transformer] = {
    val bundleName = "/ml-bundle"
    val bundle: ExtractableManagedResource[Bundle[Transformer]] = for (bf <- managed(BundleFile(MlTransformer.getClass.getResource(bundleName).toString))) yield {
      bf.loadMleapBundle().get
    }
    bundle.tried.transform(b => Success(b.root), _ => Failure(NoBundleError(bundleName)) )
  }
}