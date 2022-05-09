package net.saff.heap

enum class EntryStatus(val prefixChar: String) {
  UNPRIORITIZED("_"), PRIORITIZED("-"), COMPLETE("x");
}