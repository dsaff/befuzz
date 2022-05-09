package net.saff.befuzz

actual class GoodDataException actual constructor(adventureLog: String, cause: Throwable) :
  RuntimeException("${cause.message}\n$adventureLog", cause)
