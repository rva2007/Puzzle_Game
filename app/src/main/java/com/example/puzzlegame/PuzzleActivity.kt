package com.example.puzzlegame

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import kotlin.random.Random


class PuzzleActivity : AppCompatActivity() {
    var pieces: ArrayList<PuzzlePiece>? = null
    var mCurrentPhotoPath: String? = null
    var mCurrentPhotoUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        supportActionBar?.hide()

        val layout = findViewById<RelativeLayout>(R.id.layout)

        val imageView = findViewById<ImageView>(R.id.imageView)

        val intent = intent
        val assetName = intent.getStringExtra("assetName")
        mCurrentPhotoPath = intent.getStringExtra("mCurrentPhotoPath")
        mCurrentPhotoUri = intent.getStringExtra("mCurrentPhotoUri")

        //run image related code after the view was laid out to have all dimensions calculated
        imageView.post {
            if (assetName != null) {
                Log.d("MyLog", "in PuzzleActivity " + assetName)
                setPicFromAsset(assetName, imageView)

            } else if (mCurrentPhotoPath != null) {
                setPicFromPfotoPath(mCurrentPhotoPath!!, imageView)
//
//                imageView.setImageURI(null)
//                imageView.setImageURI(Uri.parse(mCurrentPhotoPath))



            } else if (mCurrentPhotoUri != null) {
                imageView.setImageURI(Uri.parse(mCurrentPhotoUri))
            }
            pieces = splitImage(imageView)

            Log.d("MyLog", "in splitImage " + pieces)

            val touchListener = TouchListener(this@PuzzleActivity)

            //shuffle pieces order
            pieces?.shuffle()
            for (piece in pieces!!) {
                piece.setOnTouchListener(touchListener)
                layout.addView(piece)

                //randomize position on the bottom of screen
                val lParams = piece.layoutParams as RelativeLayout.LayoutParams
                lParams.leftMargin = Random.nextInt(
                    layout.width - piece.pieceWidth
                )
                lParams.topMargin = layout.height - piece.pieceHeight

                piece.layoutParams = lParams
            }
        }

    }

    private fun setPicFromAsset(assetName: String, imageView: ImageView?) {

        val targetW = imageView!!.width
        val targetH = imageView.height
        val assetManager = assets
        Log.d("MyLog", "in setPicFromAsset " + "targetW: " + targetW.toString() +", targetH: "+ targetH.toString())

        try {

            val inputStream = assetManager.open("img/$assetName")

            //get the dimensions of the bitmap
            val bmOption = BitmapFactory.Options()

            val bitmap = BitmapFactory.decodeStream(
                inputStream, Rect(-1, -1, -1, -1), bmOption
            )

            val photoW = bmOption.outWidth
            val photoH = bmOption.outHeight
            Log.d("MyLog", "in setPicFromAsset " + "photoW: " + photoW.toString() +", photoH: "+ photoH.toString())


            //determine how much to scale down the image
            val scaleFactor = Math.max(photoW / targetW, photoH / targetH)
            Log.d("MyLog", "in setPicFromAsset " + "scaleFactor: " + scaleFactor.toString())


            //decode the image file into a Bitmap sized to fill the view
            bmOption.inJustDecodeBounds = false
            bmOption.inSampleSize = scaleFactor
            bmOption.inPurgeable = true


            imageView.setImageBitmap(bitmap)
            Log.d("MyLog","in imageView.setImageBitmap " + bitmap.toString())

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this@PuzzleActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun splitImage(imageView: ImageView?): ArrayList<PuzzlePiece> {

        val piecesNumber = 12
        val rows = 4
        val columns = 3
        val imageView = findViewById<ImageView>(R.id.imageView)
            val pieces = ArrayList<PuzzlePiece>(piecesNumber)

        //get the scaled bitmap of the source image
        val drawable = imageView!!.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val dimensions = getBitmapPositionInsideImageView(imageView)

        val scaledBitmapLeft = dimensions[0]
        val scaledBitmapTop = dimensions[1]
        val scaledBitmapWidth = dimensions[2]
        val scaledBitmapHeight = dimensions[3]

        val croppedImageWidth = scaledBitmapWidth - 2 * Math.abs(scaledBitmapLeft)
        val croppedImageHeight = scaledBitmapHeight - 2 * Math.abs(scaledBitmapTop)

        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap, scaledBitmapWidth, scaledBitmapHeight, true
        )
        val croppedBitmap = Bitmap.createBitmap(
            scaledBitmap, Math.abs(scaledBitmapLeft),
            Math.abs(scaledBitmapTop),
            croppedImageWidth, croppedImageHeight
        )

        //calculate the width and the height of the pieces
        val pieceWidth = croppedImageWidth / columns
        val pieceHeight = croppedImageHeight / rows

        //create each bitmap piece and add it to the result array
        var yCoord = 0
        for (row in 0 until rows) {
            var xCoord = 0
            for (column in 0 until columns) {
                //calculate offset for each piece
                var offsetX = 0
                var offsetY = 0
                if (column > 0) {
                    offsetX = pieceWidth / 3
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3
                }
                val pieceBitmap = Bitmap.createBitmap(
                    croppedBitmap,
                    xCoord - offsetX,
                    yCoord - offsetY,
                    pieceWidth + offsetX,
                    pieceHeight + offsetY
                )

                val piece = PuzzlePiece(applicationContext)
                piece.setImageBitmap(pieceBitmap)
                piece.xCoord = xCoord - offsetX + imageView.left
                piece.yCoord = yCoord - offsetY + imageView.top

                piece.pieceWidth = pieceWidth + offsetX
                piece.pieceHeight = pieceHeight + offsetY

                //this bitmap will hold our final puzzle piece image
                val puzzlePiece = Bitmap.createBitmap(
                    pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888
                )

                //draw path
                val bumpSize = pieceHeight / 4
                val canvas = Canvas(puzzlePiece)
                val path = android.graphics.Path()
                path.moveTo(offsetX.toFloat(), offsetY.toFloat())

                if (row == 0) {
                    //top side piece
                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        offsetY.toFloat()
                    )
                } else {
                    //top bump
                    path.lineTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 3).toFloat(),
                        offsetY.toFloat()
                    )
                    path.cubicTo(
                        ((offsetX + (pieceBitmap.width - offsetX) / 6).toFloat()),
                        (offsetY - bumpSize).toFloat(),
                        ((offsetX + (pieceBitmap.width - offsetX) / 6 * 5)).toFloat(),
                        (offsetY - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 3 * 2).toFloat(),
                        offsetY.toFloat()
                    )

                    path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
                }
                if (column == columns - 1) {
                    //right side piece
                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                } else {
                    //right bump
                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3).toFloat()
                    )
                    path.cubicTo(
                        (pieceBitmap.width - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6).toFloat(),
                        (pieceBitmap.width - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6 * 5).toFloat(),
                        pieceBitmap.width.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3 * 2).toFloat()
                    )

                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                }
                if (row == rows - 1) {
                    //bottom side piece
                    path.lineTo(
                        offsetX.toFloat(), pieceBitmap.height.toFloat()
                    )
                } else {
                    //bottom bump
                    path.lineTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 3 * 2).toFloat(),
                        pieceBitmap.height.toFloat()
                    )

                    path.cubicTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 6 * 5).toFloat(),
                        (pieceBitmap.height - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 6).toFloat(),
                        (pieceBitmap.height - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 3).toFloat(),
                        pieceBitmap.height.toFloat()
                    )

                    path.lineTo(
                        offsetX.toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                }
                if (column == 0) {
                    //left side piece
                    path.close()
                } else {
                    //left bump
                    path.lineTo(
                        offsetX.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3 * 2).toFloat(),
                    )
                    path.cubicTo(
                        (offsetX - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6 * 5).toFloat(),
                        (offsetX - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6).toFloat(),
                        offsetX.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3).toFloat()
                    )

                    path.close()
                }

                //mask the piece
                val paint = Paint()
                paint.color = -0x10000000
                paint.style = Paint.Style.FILL
                canvas.drawPath(path, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(pieceBitmap, 0f, 0f, paint)

                //draw a white border
                var border = Paint()
                border.color = -0x7f000001
                border.style = Paint.Style.STROKE
                border.strokeWidth = 8.0f
                canvas.drawPath(path, border)

                //draw a black border
                border = Paint()
                border.color = -0x80000000
                border.style = Paint.Style.STROKE
                border.strokeWidth = 3.0f
                canvas.drawPath(path, border)

                //set the resulting bitmap to the piece
                piece.setImageBitmap(puzzlePiece)
                pieces.add(piece)
                xCoord += pieceWidth
            }
            yCoord += pieceHeight
        }
        return pieces
    }

    fun checkGameOver() {
        if (isGameOver) {
            AlertDialog.Builder(this@PuzzleActivity)
                .setTitle("You Won!!!")
                .setIcon(R.drawable.ic_celebration)
                .setMessage("You won!\nDo you want to play a new game?")
                .setPositiveButton("Yes") { dialog, _ ->
                    finish()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    finish()
                    dialog.dismiss()
                    onDestroy()
                }
                .create()
                .show()
        }
    }

    private val isGameOver: Boolean
        get() {
            for (piece in pieces!!) {
                if (piece.canMove) {
                    return false
                }
            }
            return true
        }

    private fun getBitmapPositionInsideImageView(imageView: ImageView?): IntArray {

        val ret = IntArray(4)
        if (imageView == null || imageView.drawable == null) {
            return ret
        }

        //get image dimensions
        //get image matrix values and place them in an array
        val f = FloatArray(9)

        imageView.imageMatrix.getValues(f)

        //extract the scale values using the constants(if aspect ratio maintained scaleX == scaleY)
        val scaleX = f[Matrix.MSCALE_X]
        val scaleY = f[Matrix.MSCALE_Y]

        //get the drawable (could also get the bitmap the drawable and getWidth / getHeight)
        val d = imageView.drawable

        val origW = d.intrinsicWidth
        val origH = d.intrinsicHeight

        //calculate the actual dimensions
        val actW = Math.round(origW * scaleX)
        val actH = Math.round(origH * scaleY)

        ret[2] = actW
        ret[3] = actH

        //get image position
        // we assume that the image is centered into ImageView
        val imageViewW = imageView.width
        val imageViewH = imageView.height

        val top = (imageViewH - actH) / 2
        val left = (imageViewW - actW) / 2

        ret[0] = top
        ret[1] = left



        return ret
    }

    private fun setPicFromPfotoPath(mCurrentPhotoPath: String, imageView: ImageView?) {

        //get the dimensions of the view
        val targetW = imageView!!.width
        val targetH = imageView.height

        //get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()

        bmOptions.inJustDecodeBounds = true
        var bitmap= BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)

        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        //determine how much to scale down the image
        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

        //decode the image file into a Bitmap to fill the view
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPurgeable = true
//         bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)

        var rotatedBitmap = bitmap

        //rotete bitmap if needed
        try {
            val ei = ExifInterface(mCurrentPhotoPath)
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    rotatedBitmap = rotateImage(bitmap, 90f)
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    rotatedBitmap = rotateImage(bitmap, 180f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    rotatedBitmap = rotateImage(bitmap, 270f)
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this@PuzzleActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }

        imageView.setImageBitmap(rotatedBitmap)

    }

    override fun onDestroy() {
        super.onDestroy()
        MainActivity().imageFile?.deleteOnExit()
    }

    companion object {
        fun rotateImage(source: Bitmap, angle: Float): Bitmap {

            val matrix = Matrix()
            matrix.postRotate(angle)

            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }
    }
}