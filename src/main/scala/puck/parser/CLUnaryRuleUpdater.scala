package puck.parser

import puck._
import scala.collection.JavaConverters._
import com.nativelibs4java.opencl.{CLContext, CLQueue, CLEvent}
import puck.linalg.CLMatrix
import scala.Array
import java.util.zip._
import puck.util._

/**
 *
 *
 * @author dlwh
 */
case class CLUnaryRuleUpdater(kernels: IndexedSeq[RuleKernel]) {
  def this(kernels: java.util.List[RuleKernel]) = this(kernels.asScala.toIndexedSeq)

  def update(profiler: CLProfiler,
             parent: CLMatrix[Float],
             child: CLMatrix[Float],  events: CLEvent*)(implicit queue: CLQueue) = synchronized {
    require(parent.rows == child.rows)
    require(parent.cols == child.cols)
    require(parent.majorStride == child.majorStride)
    kernels.flatMap(_.kernels).map { k =>
      k.setArgs(parent.data.safeBuffer,  child.data.safeBuffer,
        Integer.valueOf(parent.majorStride), Integer.valueOf(parent.rows) )
      k.enqueueNDRange(queue, Array(parent.rows), events: _*) profileIn profiler
    }

  }

  def write(name: String, out: ZipOutputStream) {
    ZipUtil.serializedEntry(out, "$name/numKernels", Integer.valueOf(kernels.length))
    for(i <- 0 until kernels.length) {
      kernels(i).write(s"$name/$i", out)
    }
  }
}


object CLUnaryRuleUpdater {
  def read(in: ZipFile, name: String)(implicit ctxt: CLContext) = {
    val x = ZipUtil.deserializeEntry[Integer](in.getInputStream(in.getEntry(s"$name/numKernels")))
    val kernels = for(i <- 0 until x.intValue()) yield RuleKernel.read(in, s"$name/$i/")
    new CLUnaryRuleUpdater(kernels)
  }
}