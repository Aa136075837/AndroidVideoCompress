package com.mac.crashcatch

import java.io.PrintWriter
import java.io.StringWriter

/**
 * @author ex-yangjb001
 * @date 2019/1/9.
 */
class ParseError {
    companion object {
        fun parseThrowable(ex: Throwable): CrashBean {
            val crashBean = CrashBean()
            try {
                var ex = ex
                crashBean.ex = ex
                crashBean.time = System.currentTimeMillis()
                if (ex.cause != null) {
                    ex = ex.cause!!
                }
                crashBean.exceptionMsg = ex.message!!
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                ex.printStackTrace(pw)
                pw.flush()
                if (ex.stackTrace != null && ex.stackTrace.size > 0) {
                    val element = ex.stackTrace[0]
                    crashBean.lineNumber = element.lineNumber.toString()
                    crashBean.className = element.className.toString()
                    crashBean.fileName = element.fileName
                    crashBean.methodName = element.methodName
                    crashBean.exceptionType = ex::class.simpleName.toString()
                }
                crashBean.fullException = sw.toString()
            } catch (e: Throwable) {
                return crashBean
            }
            return crashBean
        }
    }
}