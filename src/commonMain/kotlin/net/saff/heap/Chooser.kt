package net.saff.heap

fun interface Chooser<T> {
  fun choose(left: T, right: T): Choice
}