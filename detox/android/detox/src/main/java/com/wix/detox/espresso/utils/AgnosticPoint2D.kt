package com.wix.detox.espresso.utils

import com.wix.detox.common.DetoxErrors
import com.wix.detox.espresso.common.annot.*

data class FloatingPoint(val x: Float, val y: Float)

data class AgnosticPoint2D(val primary: Double, val secondary: Double) {
    fun toFloatingPoint(direction: Int): FloatingPoint {
        val primaryF = primary.toFloat()
        val secondaryF = secondary.toFloat()

        return when {
            isHorizontal(direction) -> FloatingPoint(primaryF, secondaryF)
            isVertical(direction) -> FloatingPoint(secondaryF, primaryF)
            else -> throw DetoxErrors.DetoxIllegalArgumentException("Unsupported swipe direction: $direction")
        }
    }

    companion object {
        fun fromXY(x: Float, y: Float, direction: Int): AgnosticPoint2D {
            return fromXY(x.toDouble(), y.toDouble(), direction)
        }

        fun fromXY(x: Double, y: Double, direction: Int): AgnosticPoint2D {
            return when {
                isHorizontal(direction) -> AgnosticPoint2D(x, y)
                isVertical(direction) -> AgnosticPoint2D(y, x)
                else -> throw DetoxErrors.DetoxIllegalArgumentException("Unsupported swipe direction: $direction")
            }
        }
    }
}