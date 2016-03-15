package com.beachape.transform

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc
import org.bytedeco.javacpp.opencv_imgproc._

/**
 * Created by Lloyd on 2/21/16.
 */
object WithGrey {

  /**
   * Simple transformer method that produces a [[WithGrey]]
   */
  def build(orig: Mat): WithGrey = {
    val grey = toGreyScale(orig)
    WithGrey(orig = orig, grey = grey)
  }

  /**
   * Returns a greyscale Mat for a given mat
   *
   * @param mat OpenCV matrix to convert to greyscale
   */
  private def toGreyScale(mat: Mat): Mat = {
    if (mat.channels() == 1) {
      mat // just hand back the matrix as is; it is already grey
    } else {
      val greyMat = {
        val (rows, cols) = (mat.rows(), mat.cols())
        new Mat(rows, cols, CV_8U)
      }
      opencv_imgproc.cvtColor(mat, greyMat, COLOR_BGR2GRAY, 1)
      greyMat
    }
  }

}

/**
 * Original Matrix with a Grey image. Useful because almost all analysis processing requires a greyscale image instead of
 * a colour image.
 *
 * The constructor is private to make sure we don't mix up the two references
 *
 * Passing [[WithGrey]] images along with the original saves us from having to process to grey scale over and over again.
 */
final case class WithGrey private (orig: Mat, grey: Mat)
