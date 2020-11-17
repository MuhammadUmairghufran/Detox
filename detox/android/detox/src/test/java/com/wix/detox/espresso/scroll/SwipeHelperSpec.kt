package com.wix.detox.espresso.scroll

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.wix.detox.espresso.common.annot.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

object SwipeHelperSpec: Spek({
    describe("SwipeHelper") {
        val view = mock<View>()

        val viewX = 1000
        val viewY = 2000
        val viewWidth = 2000
        val viewHeight = 1000
        val screenWidth = 4000
        val screenHeight = 5000

        fun viewLeft() = viewX + viewWidth * 0f
        fun viewCenter() = viewX + viewWidth * 0.5f
        fun viewRight() = viewX + viewWidth * 1f
        fun viewTop() = viewY + viewHeight * 0f
        fun viewMiddle() = viewY + viewHeight * 0.5f
        fun viewBottom() = viewY + viewHeight * 1f
        fun viewFuzzH() = viewWidth * SwipeHelper.edgeFuzzFactor
        fun viewFuzzV() = viewHeight * SwipeHelper.edgeFuzzFactor

        fun screenTop() = 0f
        fun screenLeft() = 0f
        fun screenBottom() = screenTop() + screenHeight
        fun screenRight() = screenLeft() + screenWidth

        beforeGroup {
            val mockDisplayMetrics = mock<DisplayMetrics>()
            mockDisplayMetrics.widthPixels = screenWidth
            mockDisplayMetrics.heightPixels = screenHeight

            val mockResources = mock<Resources>()
            whenever(mockResources.displayMetrics).then { mockDisplayMetrics }

            val mockContext = mock<Context>()
            whenever(mockContext.resources).then { mockResources }
            whenever(view.context).then { mockContext }
            whenever(view.width).then { viewWidth }
            whenever(view.height).then { viewHeight }
            whenever(view.getLocationOnScreen(IntArray(2))).then {
                val arg0 = it.arguments[0]
                val xy = arg0 as IntArray
                xy[0] = viewX
                xy[1] = viewY
                xy
            }
        }

        lateinit var action: ViewAction
        lateinit var swiper: Swiper
        lateinit var startCoordinatesProvider: CoordinatesProvider
        lateinit var endCoordinatesProvider: CoordinatesProvider
        lateinit var precisionDescriber:  PrecisionDescriber

        val swipeHelper = SwipeHelper { _swiper: Swiper,
                                        _startCoordinatesProvider: CoordinatesProvider,
                                        _endCoordinatesProvider: CoordinatesProvider,
                                        _precisionDescriber: PrecisionDescriber ->
            swiper = _swiper
            startCoordinatesProvider = _startCoordinatesProvider
            endCoordinatesProvider = _endCoordinatesProvider
            precisionDescriber = _precisionDescriber
            action = mock()
            action
        }

        fun toPoint(arr: FloatArray) = Pair(arr[0], arr[1])
        fun getStartPoint() = toPoint(startCoordinatesProvider.calculateCoordinates(view))
        fun getEndPoint() = toPoint(endCoordinatesProvider.calculateCoordinates(view))

        it("should return action of fast swipe up") {
            val result = swipeHelper.swipeInDirection(MOTION_DIR_UP, true, 1.0)

            assertEquals(action, result)
            assertEquals(Swipe.FAST, swiper)
            assertEquals(Press.FINGER, precisionDescriber)
            assertEquals(Pair(viewCenter(), viewBottom() - viewFuzzV()), getStartPoint())
            assertEquals(Pair(viewCenter(), screenTop()), getEndPoint())
        }

        it("should return action of fast swipe down") {
            val result = swipeHelper.swipeInDirection(MOTION_DIR_DOWN, true, 1.0)

            assertEquals(action, result)
            assertEquals(Swipe.FAST, swiper)
            assertEquals(Press.FINGER, precisionDescriber)
            assertEquals(Pair(viewCenter(), viewTop() + viewFuzzV()), getStartPoint())
            assertEquals(Pair(viewCenter(), screenBottom()), getEndPoint())
        }

        it("should return action of fast swipe left") {
            val result = swipeHelper.swipeInDirection(MOTION_DIR_LEFT, true, 1.0)

            assertEquals(action, result)
            assertEquals(Swipe.FAST, swiper)
            assertEquals(Press.FINGER, precisionDescriber)
            assertEquals(Pair(viewRight() - viewFuzzH(), viewMiddle()), getStartPoint())
            assertEquals(Pair(screenLeft(), viewMiddle()), getEndPoint())
        }

        it("should return action of fast swipe right") {
            val result = swipeHelper.swipeInDirection(MOTION_DIR_RIGHT, true, 1.0)

            assertEquals(action, result)
            assertEquals(Swipe.FAST, swiper)
            assertEquals(Press.FINGER, precisionDescriber)
            assertEquals(Pair(viewLeft() + viewFuzzH(), viewMiddle()), getStartPoint())
            assertEquals(Pair(screenRight(), viewMiddle()), getEndPoint())
        }
    }
})
