package com.wix.detox.espresso.scroll

import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*
import com.wix.detox.espresso.common.annot.*
import com.wix.detox.espresso.utils.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max

private const val EDGE_FUZZ_FACTOR = 0.083

private fun minMax(minValue: Double, value: Double, maxValue: Double) = max(minValue, min(value, maxValue))

private fun ifNaN(value: Double, fallback: Double) = when {
    value.isNaN() -> fallback
    else -> value
}

typealias CreateSwipeAction = (
        swiper: Swiper,
        startCoordinatesProvider: CoordinatesProvider,
        endCoordinatesProvider: CoordinatesProvider,
        precisionDescriber: PrecisionDescriber
) -> ViewAction

class SwipeHelper(private val createAction: CreateSwipeAction) {

    fun swipeInDirection(
            direction: Int,
            fast: Boolean = true,
            amount: Double = Double.NaN,
            startPositionX: Double = Double.NaN,
            startPositionY: Double = Double.NaN
    ): ViewAction {
        val (edgeMin, edgeMax) = Pair(EDGE_FUZZ_FACTOR, 1.0 - EDGE_FUZZ_FACTOR)
        val defaultNormalizedStartPoint = Vector2D(0.5, edgeMin).rotate(direction, MOTION_DIR_DOWN).normalize()
        val normalizedStartPoint = Vector2D(
                minMax(edgeMin, ifNaN(startPositionX, defaultNormalizedStartPoint.x), edgeMax),
                minMax(edgeMin, ifNaN(startPositionY, defaultNormalizedStartPoint.y), edgeMax)
        )

        val safeAmount = minMax(0.0, ifNaN(amount, 0.75), 1.0)
        val startCoordinatesProvider = buildStartCoordinatesProvider(normalizedStartPoint)
        val endCoordinatesProvider = buildEndCoordinatesProvider(startCoordinatesProvider, direction, safeAmount)
        val swiper = if (fast) Swipe.FAST else Swipe.SLOW

        return this.createAction(swiper, startCoordinatesProvider, endCoordinatesProvider, Press.FINGER)
    }

    private fun buildStartCoordinatesProvider(normalizedStartPoint: Vector2D) = CoordinatesProvider { view ->
        val xy = GeneralLocation.TOP_LEFT.calculateCoordinates(view)
        xy[0] += (normalizedStartPoint.x * view.width).toFloat()
        xy[1] += (normalizedStartPoint.y * view.height).toFloat()
        xy
    }

    private fun buildEndCoordinatesProvider(startCoordinatesProvider: CoordinatesProvider, direction: Int, amount: Double) = CoordinatesProvider { view ->
        val xy = startCoordinatesProvider.calculateCoordinates(view)

        val screenEdge = Vector2D.from(
                view.context.resources.displayMetrics.widthPixels,
                view.context.resources.displayMetrics.heightPixels
        ).rotate(MOTION_DIR_DOWN, direction)

        val swipeEnd = Vector2D.from(xy)
                .add(screenEdge.scale(amount))
                .trimMax(0.0, 0.0)
                .trimMin(abs(screenEdge.x), abs(screenEdge.y))
                .y.toFloat()

        if (isHorizontal(direction)) {
            xy[0] = swipeEnd
        } else {
            xy[1] = swipeEnd
        }

        xy
    }

    companion object {
        @JvmStatic
        val default = SwipeHelper { swiper: Swiper,
                                    startCoordinatesProvider: CoordinatesProvider,
                                    endCoordinatesProvider: CoordinatesProvider,
                                    precisionDescriber: PrecisionDescriber ->
            ViewActions.actionWithAssertions(
                    GeneralSwipeAction(
                            swiper,
                            startCoordinatesProvider,
                            endCoordinatesProvider,
                            precisionDescriber
                    )
            );
        }

        const val edgeFuzzFactor = EDGE_FUZZ_FACTOR.toFloat()
    }
}
