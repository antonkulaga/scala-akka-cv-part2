package com.beachape.transform

import java.util.function.Supplier

import com.beachape.video.Dimensions
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc
import org.bytedeco.javacpp.opencv_imgproc._
import org.bytedeco.javacv.{ Java2DFrameConverter, Frame, OpenCVFrameConverter }

/**
 * Created by Lloyd on 2/15/16.
 */

/**
 * Holds conversion and transformation methods for media types
 */
object MediaConversion {

  // Each thread gets its own greyMat for safety
  private val frameToMatConverter = ThreadLocal.withInitial(new Supplier[OpenCVFrameConverter.ToMat] {
    def get(): OpenCVFrameConverter.ToMat = new OpenCVFrameConverter.ToMat
  })

  private val clahe = ThreadLocal.withInitial(new Supplier[CLAHE] {
    def get(): CLAHE = opencv_imgproc.createCLAHE()
  })

  /**
   * Returns an OpenCV Mat for a given JavaCV frame
   */
  def toMat(frame: Frame): Mat = frameToMatConverter.get().convert(frame)

  /**
   * Returns a JavaCV Frame for a given OpenCV Mat
   */
  def toFrame(mat: Mat): Frame = frameToMatConverter.get().convert(mat)

  /**
   * Clone the given OpenCV matrix and return an equalised version (CLAHE (Contrast Limited Adaptive Histogram Equalization))
   * of the matrix
   */
  def equalise(mat: Mat): Mat = {
    val clone = mat.clone()
    clahe.get().apply(mat, clone)
    clone
  }

}