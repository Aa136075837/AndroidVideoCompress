package com.mac.androidvideocompress

import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_coroutines.*
import kotlinx.coroutines.*
import java.lang.Thread.sleep

class CoroutinesActivity : AppCompatActivity() {
    private val TAG = "Coroutines"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutines)
        initEvent()
    }

    private fun initEvent() {
        mStart.setOnClickListener {
            createCoroutine()
        }

        mRunBlocking.setOnClickListener {
            createThread()
        }
        mMain.setOnClickListener {
            main()
        }
        mJob.setOnClickListener {
            runBlocking {
                launch {
                    delay(1000)
                    Log.i(TAG, "WORLD")
                }
                Log.i(TAG, "HELLO,")
            }
        }
    }

    fun createCoroutine() {
        runBlocking {
            repeat(100_000) {
                launch {
                    delay(1000)
                    Log.i(TAG, ".")
                }
            }
        }
    }

    fun createThread() {
        for (i: Int in 0..100_000) {
            Thread {
                sleep(1000)
                Log.i(TAG, ",")
            }.start()
        }
    }

    fun first() = runBlocking {
        launch {
            asdf()
        }

        coroutineScope {
            launch {
                delay(5000)
                Log.i(TAG, "Task from nested launch")
            }

            delay(1000)
            Log.i(TAG, "Task from coroutine scope")
        }
        Log.i(TAG, "Coroutine scope is over")
    }

    private suspend fun asdf() {
        delay(2000)
        Log.i(TAG, "Task from runBlocking")
    }

    fun runBlock() {
        GlobalScope.launch {
            // 在后台启动一个新的协程并继续
            delay(1000L)
            mTextView.append("Blocking!")
        }
        mTextView.append("run,") // 主线程中的代码会立即执行
        runBlocking {
            // 但是这个表达式阻塞了主线程
            delay(2000L)  // ……我们延迟 2 秒来保证 JVM 的存活
        }
    }

    fun main() = runBlocking<Unit> {
        GlobalScope.launch {
            delay(100000L)
            Log.i(TAG, "Main2")
        }
        Log.i(TAG, "Main1,")
        delay(2000L)
    }


}
