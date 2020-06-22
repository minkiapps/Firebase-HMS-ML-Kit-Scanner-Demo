package com.minkiapps.scanner.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun View.px(densityPixel : Int) = calc(context, densityPixel.toFloat()).toInt()

fun View.px(densityPixel : Float) = calc(context, densityPixel)

fun Fragment.px(densityPixel : Float) = calc(requireContext(), densityPixel)

fun Context.px(densityPixel : Float) = calc(this, densityPixel)

fun Context.px(densityPixel : Int) = calc(this, densityPixel)

fun Fragment.px(densityPixel : Int) = calc(requireContext(), densityPixel.toFloat()).toInt()

fun AppCompatActivity.px(densityPixel : Int) = calc(this, densityPixel.toFloat()).toInt()

private fun calc(context : Context, dp : Int) : Int{
    return dp * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

private fun calc(context : Context, dp : Float) : Float{
    return dp * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}