package net.saff.befuzz

actual class GoodDataException actual constructor(adventureLog: String, cause: Throwable) :
  RuntimeException("${cause.message}\n$adventureLog", cause) {

  override fun getStackTrace(): Array<StackTraceElement> {
    val causeHere = cause
    return if (causeHere != null) {
      causeHere.stackTrace
    } else {
      super.getStackTrace()
    }
  }
}
