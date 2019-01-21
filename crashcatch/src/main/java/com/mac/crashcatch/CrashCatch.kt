package com.mac.crashcatch

/**
 * @author ex-yangjb001
 * @date 2019/1/9.
 */
object CrashCatch : Thread.UncaughtExceptionHandler {
    fun init() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val crashBean = ParseError.parseThrowable(e!!)
        handCrash(crashBean)
    }

    private fun handCrash(crashBean: CrashBean) {

    }

}