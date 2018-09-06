package com.maidan.android.client.controllers

import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView

class SnapHelperOneByOne: LinearSnapHelper() {
    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager?, velocityX: Int, velocityY: Int): Int {
//        return super.findTargetSnapPosition(layoutManager, velocityX, velocityY)

        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return RecyclerView.NO_POSITION
        }

        val currentView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION

        val currentPosition = layoutManager.getPosition(currentView)

        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }
        return currentPosition
    }
}