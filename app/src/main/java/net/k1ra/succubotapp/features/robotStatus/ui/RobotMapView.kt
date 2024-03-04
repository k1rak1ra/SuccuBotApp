package net.k1ra.succubotapp.features.robotStatus.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.jpountz.lz4.LZ4Factory
import net.k1ra.succubotapp.R
import java.lang.Float.min
import kotlin.math.abs


class RobotMapView(
    ctx: Context,
    attributeSet: AttributeSet?,
    i: Int
) : View(ctx, attributeSet, i) {
    constructor(ctx: Context) : this(ctx, null, 0)
    constructor(ctx: Context, attributeSet: AttributeSet) : this(ctx, attributeSet, 0)

    val className = "RobotMapView"

    private var mapRobotX = 0
    private var mapRobotY = 0

    private var chargerX = 0
    private var chargerY = 0

    private var pathX = 0
    private var pathY = 0
    private val pathHistory = Path()

    private var mX = 0
    private var mY = 0

    private var pathLaserData: ByteArray? = null
    private var laserMapData: ByteArray = ByteArray(0)

    private var mapPixelData = IntArray(0)

    private val pathHistoryPaint = Paint()
    private var currentRobotLocationPaint = Paint()

    private var doInitialScaling = true

    private var chargerBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.charger)
    private var mapBitmap: Bitmap? = null

    private var motionEventPointerCountOver1 = false

    private var lastActionDownX = 0
    private var lastActionDownY = 0

    private var actionMoveDestX = 0
    private var actionMoveDestY = 0

    private var currentlyScaling = false
    private val scalingMatrix = Matrix()

    private var parsingPathData = false
    private var parsingMapData = false

    var onDoneParsingMapData = Runnable {  }
    var onDoneParsingPathData = Runnable {  }

    private val scaleGestureDetector = ScaleGestureDetector(context, object : OnScaleGestureListener {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            val scaleFactor = scaleGestureDetector.scaleFactor

            Log.d(className, "onScale:$scaleFactor")

            scalingMatrix.postScale(
                scaleFactor,
                scaleFactor,
                scaleGestureDetector.focusX,
                scaleGestureDetector.focusY
            )
            this@RobotMapView.postInvalidate()
            return true
        }

        override fun onScaleBegin(scaleGestureDetector: ScaleGestureDetector): Boolean {
            currentlyScaling = true
            return true
        }

        override fun onScaleEnd(scaleGestureDetector: ScaleGestureDetector) {
            currentlyScaling = false
        }
    })

    init {
        pathHistoryPaint.color = context.resources.getColor(R.color.pathHistory)
        pathHistoryPaint.isAntiAlias = true
        pathHistoryPaint.strokeWidth = 2.0f
        pathHistoryPaint.style = Paint.Style.STROKE
        pathHistoryPaint.setPathEffect(CornerPathEffect(1.0f))

        currentRobotLocationPaint.color = context.resources.getColor(R.color.green)
        currentRobotLocationPaint.isAntiAlias = true
        currentRobotLocationPaint.strokeWidth = 4.0f
        currentRobotLocationPaint.style = Paint.Style.FILL_AND_STROKE

        mX = context.resources.displayMetrics.widthPixels / 2
        mY = context.resources.getDimensionPixelSize(R.dimen.robot_map_view_height) / 2

        postInvalidate()

        Log.d(className, "init")
    }

    private fun decompressAndRenderMapBytes(
        compressedMapBytes: ByteArray,
        mapWidth: Int,
        mapHeight: Int
    ) {
        val length = compressedMapBytes.size - 24
        val bArr = ByteArray(length)

        for (i in 0 until length) {
            bArr[i] = compressedMapBytes[i + 24]
        }

        renderMap(
            mapWidth,
            mapHeight,
            LZ4Factory.safeInstance().safeDecompressor().decompress(bArr, 1000000)
        )
    }

    private fun plotPathHistory(size: Int, bArr: ByteArray) {
        if (!(mapRobotX == 0 || mapRobotY == 0)) {
            pathHistory.reset()

            for (index in 0 until size) {
                val i4 = index * 4
                val i5 = (bArr[i4 + 1 + 13].toInt() and 255) + (bArr[i4 + 13].toInt() shl 8 and 65280)
                val i6 = (bArr[i4 + 3 + 13].toInt() and 255) + (bArr[i4 + 2 + 13].toInt() shl 8 and 65280)

                val pathX = mX + if (i5 > 32767)
                    (i5 - 65536) / 10
                else
                    i5 / 10

                val pathY = if (i6 > 32767) {
                    mY - (i6 - 65536) / 10
                } else {
                    mY - i6 / 10
                }

                if (index == 0) {
                    this.pathHistory.moveTo(pathX.toFloat(), pathY.toFloat())
                } else {
                    this.pathHistory.lineTo(pathX.toFloat(), pathY.toFloat())
                }

                if (index == size - 1) {
                    this.pathX = pathX
                    this.pathY = pathY
                }

                Log.v(className, "path-x:$pathX y:$pathY mMapOx:$mapRobotX mMapOy:$mapRobotY index:$index")
            }
        }
    }

    private fun assignMapPixelColors(pixel: Int, index: Int) {
        when (pixel) {
            0 -> mapPixelData[index] = context.resources.getColor(R.color.overlay11)
            1 -> mapPixelData[index] = context.resources.getColor(R.color.white)
            else -> mapPixelData[index] = context.resources.getColor(R.color.background)
        }
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d(className, "onDraw start")

        canvas.save()
        canvas.concat(scalingMatrix)

        //Draw map
        mapBitmap?.let { canvas.drawBitmap(it, (mX - mapRobotX).toFloat(), (mY - mapRobotY).toFloat(), pathHistoryPaint) }

        Log.d(className, "onDraw mMapOx:$mapRobotX mMapOy:$mapRobotY mChargeX：$chargerX mChargeY：$chargerY")

        //Draw path history
        canvas.save()
        canvas.drawPath(pathHistory, pathHistoryPaint)
        canvas.restore()

        //Draw charger location
        if (mapRobotY > 0 && mapRobotX > 0)
            drawBitmapOnCanvasWithScale(
                canvas,
                chargerBitmap,
                mX - chargerX + mapRobotX,
                mY - chargerY + mapRobotY - (scaleByDisplayDensity(5) - 2)/2,
                scaleByDisplayDensity(3),
                scaleByDisplayDensity(5))

        //Draw robot current location if map is available
        mapBitmap?.let { canvas.drawCircle((pathX - 2).toFloat(), (pathY + 2).toFloat(), 3.0f, currentRobotLocationPaint) }

        Log.d(className, "onDraw done")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val x = motionEvent.x.toInt()
        val y = motionEvent.y.toInt()
        scaleGestureDetector.onTouchEvent(motionEvent)

        if (motionEvent.pointerCount > 1) {
            motionEventPointerCountOver1 = true
        }

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                lastActionDownX = x
                lastActionDownY = y
                motionEventPointerCountOver1 = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!currentlyScaling) {
                    val abs = abs(x - lastActionDownX)
                    val abs2 = abs(y - lastActionDownY)
                    if (abs > 10 && abs2 > 10 && !motionEventPointerCountOver1) {
                        scalingMatrix.postTranslate((x - actionMoveDestX).toFloat(), (y - actionMoveDestY).toFloat())
                        postInvalidate()
                    }
                }

                actionMoveDestX = x
                actionMoveDestY = y
            }
        }

        return true
    }

    private fun renderMap(mapWidth: Int, mapHeight: Int, mapBytes: ByteArray) {
        Log.d(className, "renderMap-start-length:" + mapBytes.size)

        mapPixelData = IntArray(mapBytes.size * 4)

        for (i in mapBytes.indices) {
            val i2 = i * 4
            assignMapPixelColors(mapBytes[i].toInt() and 192 shr 6, i2)
            assignMapPixelColors(mapBytes[i].toInt() and 48 shr 4, i2 + 1)
            assignMapPixelColors(mapBytes[i].toInt() and 12 shr 2, i2 + 2)
            assignMapPixelColors(mapBytes[i].toInt() and 3, i2 + 3)
        }

        mapBitmap = Bitmap.createBitmap(mapPixelData, mapWidth, mapHeight, Bitmap.Config.ARGB_8888)

        Log.d(className, "renderMap-end-length:" + mapBytes.size)

        if (doInitialScaling) {
            val maxXScale = 3.2f * context.resources.displayMetrics.widthPixels.toFloat() / mapWidth
            val maxYScale = 3.2f * context.resources.getDimensionPixelSize(R.dimen.robot_map_view_height).toFloat() / mapHeight

            val scale = min(maxXScale,maxYScale)

            scalingMatrix.postScale(
                scale,
                scale,
                mX.toFloat(),
                mY.toFloat())

            var minLeftSpace = 10000
            var minRightSpace = 10000
            var topSpace = 0
            var bottomSpace = 0
            var passedBody = false

            for (line in 0 until mapHeight/4) {
                var leftSpace = 0
                var rightSpace = 0
                var hadLastLeftSpace = false
                var hadLastContent: Boolean

                for (place in 0 until mapWidth / 4) {
                    val byte = mapBytes[mapHeight * line + place].toInt()

                    if (byte != -1)
                        hadLastLeftSpace = true

                    if (!hadLastLeftSpace)
                        leftSpace++


                    hadLastContent = hadLastLeftSpace && byte == -1

                    if (hadLastContent)
                        rightSpace++
                    else
                        rightSpace = 0
                }

                if (rightSpace != 0) {
                    minRightSpace = minRightSpace.coerceAtMost(rightSpace)
                    passedBody = true
                } else {
                    if (passedBody)
                        bottomSpace++
                    else
                        topSpace++
                }

                minLeftSpace = minLeftSpace.coerceAtMost(leftSpace)
            }

            val xSkew = (minRightSpace - minLeftSpace) * scale * 2
            val ySkew = (bottomSpace - topSpace) * scale * 2

            scalingMatrix.postTranslate(xSkew, ySkew)

            doInitialScaling = false
        }
    }

    fun processCompressedMapBytes(compressedMapBytes: ByteArray) = CoroutineScope(Dispatchers.IO).launch {
        if (parsingMapData)
            return@launch
        parsingMapData = true

        laserMapData = compressedMapBytes
        val version = compressedMapBytes[0]
        val mapId = (compressedMapBytes[2].toInt() and 255) + (compressedMapBytes[1].toInt() shl 8 and 65280)
        val type = compressedMapBytes[3]
        val mapWidth = (compressedMapBytes[5].toInt() and 255) + (compressedMapBytes[4].toInt() shl 8 and 65280)
        val mapHeight = (compressedMapBytes[7].toInt() and 255) + (compressedMapBytes[6].toInt() shl 8 and 65280)

        mapRobotX = ((compressedMapBytes[9].toInt() and 255) + (compressedMapBytes[8].toInt() shl 8 and 65280)) / 10
        mapRobotY = ((compressedMapBytes[11].toInt() and 255) + (compressedMapBytes[10].toInt() shl 8 and 65280)) / 10
        chargerX = ((compressedMapBytes[15].toInt() and 255) + (compressedMapBytes[14].toInt() shl 8 and 65280)) / 10
        chargerY = ((compressedMapBytes[17].toInt() and 255) + (compressedMapBytes[16].toInt() shl 8 and 65280)) / 10

        val len = (compressedMapBytes[21].toInt() and 255) +
                (compressedMapBytes[20].toInt() shl 8 and 65280) +
                (compressedMapBytes[19].toInt() shl 16 and 16711680) +
                (compressedMapBytes[18].toInt() shl 24 and -16777216)

        val lz4len = (compressedMapBytes[22].toInt() and 255) + (compressedMapBytes[23].toInt() shl 8 and 65280)

        Log.d(className, "len:$len lz4len:$lz4len mMapOx:$mapRobotX mMapOy:$mapRobotY mChargeX：$chargerX mChargeY：$chargerY")
        Log.d(className, "version: ${version.toInt()} map_id:$mapId type:${type.toInt()} map_width:$mapWidth map_height:$mapHeight")

        decompressAndRenderMapBytes(compressedMapBytes, mapWidth, mapHeight)

        postInvalidate()
        Handler(Looper.getMainLooper()).post {
            parsingMapData = false
            onDoneParsingMapData.run()
        }
    }

    fun parsePathData(pathData: ByteArray) = CoroutineScope(Dispatchers.IO).launch {
        if (parsingPathData)
            return@launch
        parsingPathData = true

        pathLaserData = pathData
        val count = (pathData[8].toInt() and 255) +
                (pathData[7].toInt() shl 8 and 65280) +
                (pathData[6].toInt() shl 16 and 16711680) +
                (pathData[5].toInt() shl 24 and -16777216)

        Log.d(className, "path-bytes:${pathData.size} count:$count")

        if (pathData.size != count * 4 + 13)
            Log.d(className, "path-bytes-err")

        plotPathHistory(count, pathData)

        Handler(Looper.getMainLooper()).post {
            parsingPathData = false
            onDoneParsingPathData.run()
        }
        postInvalidate()
    }

    private fun drawBitmapOnCanvasWithScale(canvas: Canvas, bitmap: Bitmap, left: Int, top: Int, width: Int, height: Int) {
        val rect = Rect()
        rect.left = left
        rect.top = top
        rect.right = left + width
        rect.bottom = top + height
        canvas.drawBitmap(bitmap, null as Rect?, rect, null as Paint?)
    }

    private fun scaleByDisplayDensity(i: Int): Int {
        return (i * context.resources.displayMetrics.density + 0.5f).toInt()
    }
}