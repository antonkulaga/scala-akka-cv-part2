package com.beachape.video

import akka.actor.{ DeadLetterSuppression, Props, ActorSystem, ActorLogging }
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{ Cancel, Request }
import akka.stream.scaladsl.Source
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacv.{ FFmpegFrameGrabber, OpenCVFrameGrabber, FrameGrabber, Frame }
import org.bytedeco.javacv.FrameGrabber.ImageMode

import scala.util.Try

case class WebCamGrabberBuilder(
    deviceId: Int,
    dimensions: Dimensions,
    bitsPerPixel: Int = CV_8U,
    imageMode: ImageMode = ImageMode.COLOR
) extends GrabberBuilder {
  def startGrab(): FrameGrabber = synchronized {
    val imageWidth = dimensions.width
    val imageHeight = dimensions.height
    val g = FrameGrabber.createDefault(deviceId)
    g.setImageWidth(imageWidth)
    g.setImageHeight(imageHeight)
    g.setBitsPerPixel(bitsPerPixel)
    g.setImageMode(imageMode)
    g.start()
    g
  }
}

case class FileGrabberBuilder(file: String, dimensions: Dimensions,
    bitsPerPixel: Int = CV_8U,
    imageMode: ImageMode = ImageMode.COLOR) extends GrabberBuilder {
  override def startGrab(): FrameGrabber = synchronized { //not sure if it is needed at all
    val imageWidth = dimensions.width
    val imageHeight = dimensions.height
    val g = new FFmpegFrameGrabber(file)
    g.setFormat("avi")
    g.setAudioChannels(0)
    g.setImageWidth(418)
    g.setImageHeight(299)
    g.setBitsPerPixel(bitsPerPixel)
    g.setImageMode(imageMode)
    g.start()
    g
  }

}

trait GrabberBuilder {
  def startGrab(): FrameGrabber
}

object GrabberBuilder {

  def fileSource(file: String, dimensions: Dimensions,
    bitsPerPixel: Int = CV_8U,
    imageMode: ImageMode = ImageMode.COLOR)(implicit system: ActorSystem) = {
    val props = Props(
      new GrabberPublisher(
        new FileGrabberBuilder(file, dimensions = dimensions,
          bitsPerPixel = bitsPerPixel,
          imageMode = imageMode)
      )
    )
    val ref = system.actorOf(props)
    val publisher = ActorPublisher[Frame](ref)
    Source.fromPublisher(publisher)
  }

  def webcamSource(
    deviceId: Int,
    dimensions: Dimensions,
    bitsPerPixel: Int = CV_8U,
    imageMode: ImageMode = ImageMode.COLOR
  )(implicit system: ActorSystem) = {
    val props = Props(
      new GrabberPublisher(
        WebCamGrabberBuilder(
          deviceId = deviceId,
          dimensions = dimensions,
          bitsPerPixel = bitsPerPixel,
          imageMode = imageMode
        )
      )
    )
    val ref = system.actorOf(props)
    val publisher = ActorPublisher[Frame](ref)
    Source.fromPublisher(publisher)
  }

}

class GrabberPublisher(builder: GrabberBuilder) extends ActorPublisher[Frame] with ActorLogging {

  private implicit val ec = context.dispatcher

  // Lazy so that nothing happens until the flow begins
  private lazy val grabber: FrameGrabber = builder.startGrab()

  def receive: Receive = {
    case _: Request => emitFrames()
    case Continue => emitFrames()
    case Cancel => onCompleteThenStop()
    case unexpectedMsg => log.warning(s"Unexpected message: $unexpectedMsg")
  }

  private def emitFrames(): Unit = {
    if (totalDemand > 0) {
      Try {
        grabFrame().foreach {
          case f =>
            onNext(f)
        }
      }

      self ! Continue
    } else {
      println("no demand")
    }
    /*
    if (isActive && totalDemand > 0) {
      /*
        Grabbing a frame is a blocking I/O operation, so we don't send too many at once.
       */
      Try {
        grabFrame().foreach {
          case f =>
            onNext(f)
        }
      }
      if (totalDemand > 0) {
        self ! Continue
      }
    }
    */
  }

  private def grabFrame(): Option[Frame] = {
    val frame = grabber.grab()
    println("grab " + frame + " with rate " + grabber.getFrameRate)
    Option(frame)
  }
}

private case object Continue extends DeadLetterSuppression {

}
