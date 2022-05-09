package net.saff.heap

interface PriorityHeap<T> {
  fun addUnprioritized(item: T)
  fun top(): T?
  fun isTopKnown(): Boolean
  fun prioritize(chooser: Chooser<T>)
  fun markTopComplete()
  fun done(): List<T>
  val allEntries: List<HeapEntry<T>>
}