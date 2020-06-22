package com.minkiapps.scanner.util

fun String.replaceWithinRange(fromIndex: Int, toIndex : Int, toFind : String, replaceWith : String) : String{
    return substring(0, fromIndex) + substring(fromIndex, toIndex).replace(toFind, replaceWith) + substring(toIndex)
}

fun String.replaceWithinRange(fromIndex: Int, toFind : String, replaceWith : String) : String{
    return replaceWithinRange(fromIndex, length, toFind, replaceWith)
}