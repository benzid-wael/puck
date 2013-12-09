package puck.parser.gen

import com.nativelibs4java.opencl.{CLQueue, CLBuffer, CLEvent, CLKernel}
import puck.linalg.CLMatrix

/**
 *
 *
 * @author dlwh
 */
case class CLBinaryRuleUpdater(kernels: IndexedSeq[CLKernel]) {
  def update(parent: CLMatrix[Float], parentPointers: CLBuffer[Int],
             left: CLMatrix[Float], right: CLMatrix[Float],
             masks: CLMatrix[Int], events: CLEvent*)(implicit queue: CLQueue) = synchronized {
    require(parent.rows <= parentPointers.getElementCount)
    require(parent.rows == left.rows)
    require(parent.cols == left.cols)
    require(parent.majorStride == left.majorStride)
    require(parent.rows == right.rows)
    require(parent.cols == right.cols)
    require(parent.majorStride == right.majorStride)
    kernels.map { k =>
      k.setArgs(parent.data.safeBuffer, parentPointers,
        left.data.safeBuffer, right.data.safeBuffer,
        masks.data.safeBuffer,
        Integer.valueOf(parent.majorStride), Integer.valueOf(parent.rows) )
      k.enqueueNDRange(queue, Array(parent.rows), events: _*)
    }

  }
}