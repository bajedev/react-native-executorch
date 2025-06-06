package com.swmansion.rnexecutorch.models.classification

import com.facebook.react.bridge.ReactApplicationContext
import com.swmansion.rnexecutorch.models.BaseModel
import com.swmansion.rnexecutorch.utils.ImageProcessor
import com.swmansion.rnexecutorch.utils.softmax
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.pytorch.executorch.EValue

class ClassificationModel(
  reactApplicationContext: ReactApplicationContext,
) : BaseModel<Mat, Map<String, Float>>(reactApplicationContext) {
  private fun getModelImageSize(): Size {
    val inputShape = module.getInputShape(0)
    val width = inputShape[inputShape.lastIndex]
    val height = inputShape[inputShape.lastIndex - 1]

    return Size(height.toDouble(), width.toDouble())
  }

  fun preprocess(input: Mat): EValue {
    Imgproc.resize(input, input, getModelImageSize())
    return ImageProcessor.matToEValue(input, module.getInputShape(0))
  }

  fun postprocess(output: Array<EValue>): Map<String, Float> {
    val tensor = output[0].toTensor()
    val probabilities = softmax(tensor.dataAsFloatArray.toTypedArray())

    val result = mutableMapOf<String, Float>()

    for (i in probabilities.indices) {
      result[imagenet1k_v1_labels[i]] = probabilities[i]
    }

    return result
  }

  override fun runModel(input: Mat): Map<String, Float> {
    val modelInput = preprocess(input)
    val modelOutput = forward(modelInput)
    return postprocess(modelOutput)
  }
}
