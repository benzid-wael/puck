package puck.util

import com.nativelibs4java.opencl.{CLMem, CLQueue, CLBuffer}
import org.bridj.Pointer

/**
 *
 * @author dlwh
 */
class MemBufPair[T](val dev: CLBuffer[T], val ptr: Pointer[T])(implicit queue: CLQueue, man: Manifest[T]) {

  def data: Array[T] = {
    dev.read(queue, ptr, true)
    man match {
      case Manifest.Float => ptr.getFloats.asInstanceOf[Array[T]]
      case Manifest.Double => ptr.getDoubles.asInstanceOf[Array[T]]
      case Manifest.Int => ptr.getInts.asInstanceOf[Array[T]]
      case Manifest.Long => ptr.getLongs.asInstanceOf[Array[T]]
      case Manifest.Short => ptr.getShorts.asInstanceOf[Array[T]]
      case Manifest.Char => ptr.getChars.asInstanceOf[Array[T]]
      case _ => ptr.getArray.asInstanceOf[Array[T]]
    }
  }

  def data_=(arr: Array[T]) {
    assert(arr.length <= dev.getElementCount, s"Passed in array of size ${arr.length} was bigger than buffer of size ${dev.getElementCount}")
    man match {
      case Manifest.Float => ptr.setFloats(arr.asInstanceOf[Array[Float]])
      case Manifest.Double => ptr.setDoubles(arr.asInstanceOf[Array[Double]])
      case Manifest.Int => ptr.setInts(arr.asInstanceOf[Array[Int]])
      case Manifest.Long => ptr.setLongs(arr.asInstanceOf[Array[Long]])
      case Manifest.Short => ptr.setShorts(arr.asInstanceOf[Array[Short]])
      case Manifest.Char => ptr.setChars(arr.asInstanceOf[Array[Char]])
      case _ => ptr.setArray(arr)
    }
    dev.write(queue, 0, dev.getElementCount, ptr, true)
  }

  def release() {
    dev.release()
    ptr.release()
  }

  def length = dev.getElementCount

}

object MemBufPair {
  def apply[T](length: Long, usage: CLMem.Usage = CLMem.Usage.InputOutput)(implicit queue: CLQueue, man: Manifest[T]): MemBufPair[T] = {
    val ptr = Pointer.allocateArray(man.runtimeClass.asInstanceOf[Class[T]], length)
    val buf = queue.getContext.createBuffer(usage, ptr)
    new MemBufPair(buf, ptr)
  }

  def apply[T](usage: CLMem.Usage, length: Long)(implicit queue: CLQueue, man: Manifest[T]): MemBufPair[T] = {
    apply(length, usage)
  }
}