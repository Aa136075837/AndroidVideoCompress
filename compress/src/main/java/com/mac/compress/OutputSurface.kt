package com.mac.compress

import android.annotation.TargetApi
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.*

/**
 * @author ex-yangjb001
 * @date 2018/12/6.
 */
@TargetApi(16)
class OutputSurface : SurfaceTexture.OnFrameAvailableListener {
    constructor(resultWidth: Int, resultHeight: Int, rotateRender: Int)
    constructor()

    private val EGL_OPENGL_ES2_BIT = 4
    private val EGL_CONTEXT_CLIENT_VERSION = 0x3098
    private var mEGL: EGL10? = null
    private var mEGLDisplay: EGLDisplay? = null
    private var mEGLContext: EGLContext? = null
    private var mEGLSurface: EGLSurface? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mSurface: Surface? = null
    private val mFrameSyncObject = Object()
    private var mFrameAvailable: Boolean = false
    private var mTextureRender: TextureRenderer? = null
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var rotateRender = 0
    private lateinit var mPixelBuf: ByteBuffer

    fun OutputSurface(width: Int, height: Int, rotate: Int) {
        if (width <= 0 || height <= 0) {
            throw IllegalArgumentException()
        }
        mWidth = width
        mHeight = height
        rotateRender = rotate
        mPixelBuf = ByteBuffer.allocateDirect(mWidth * mHeight * 4)
        mPixelBuf.order(ByteOrder.LITTLE_ENDIAN)
        eglSetup(width, height)
        makeCurrent()
        setup()
    }

    fun OutputSurface() {
        setup()
    }

    private fun setup() {
        mTextureRender = TextureRenderer(rotateRender)
        mTextureRender!!.surfaceCreated()
        mSurfaceTexture = SurfaceTexture(mTextureRender!!.getTextureId())
        mSurfaceTexture!!.setOnFrameAvailableListener(this)
        mSurface = Surface(mSurfaceTexture)
    }

    private fun eglSetup(width: Int, height: Int) {
        mEGL = EGLContext.getEGL() as EGL10
        mEGLDisplay = mEGL!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

        if (mEGLDisplay === EGL10.EGL_NO_DISPLAY) {
            throw RuntimeException("unable to get EGL10 display")
        }

        if (!mEGL!!.eglInitialize(mEGLDisplay, null)) {
            mEGLDisplay = null
            throw RuntimeException("unable to initialize EGL10")
        }

        val attribList = intArrayOf(EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_ALPHA_SIZE, 8, EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT, EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE)
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!mEGL!!.eglChooseConfig(mEGLDisplay, attribList, configs, configs.size, numConfigs)) {
            throw RuntimeException("unable to find RGB888+pbuffer EGL config")
        }
        val attrib_list = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
        mEGLContext = mEGL!!.eglCreateContext(mEGLDisplay, configs[0], EGL10.EGL_NO_CONTEXT, attrib_list)
        checkEglError("eglCreateContext")
        if (mEGLContext == null) {
            throw RuntimeException("null context")
        }
        val surfaceAttribs = intArrayOf(EGL10.EGL_WIDTH, width, EGL10.EGL_HEIGHT, height, EGL10.EGL_NONE)
        mEGLSurface = mEGL!!.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs)
        checkEglError("eglCreatePbufferSurface")
        if (mEGLSurface == null) {
            throw RuntimeException("surface was null")
        }
    }

    fun release() {
        if (mEGL != null) {
            if (mEGL!!.eglGetCurrentContext() == mEGLContext) {
                mEGL!!.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
            }
            mEGL!!.eglDestroySurface(mEGLDisplay, mEGLSurface)
            mEGL!!.eglDestroyContext(mEGLDisplay, mEGLContext)
        }
        mSurface!!.release()
        mEGLDisplay = null
        mEGLContext = null
        mEGLSurface = null
        mEGL = null
        mTextureRender = null
        mSurface = null
        mSurfaceTexture = null
    }

    fun makeCurrent() {
        if (mEGL == null) {
            throw RuntimeException("not configured for makeCurrent")
        }
        checkEglError("before makeCurrent")
        if (!mEGL!!.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            throw RuntimeException("eglMakeCurrent failed")
        }
    }

    fun getSurface(): Surface? {
        return mSurface
    }

    fun changeFragmentShader(fragmentShader: String) {
        mTextureRender!!.changeFragmentShader(fragmentShader)
    }

    fun awaitNewImage() {
        val TIMEOUT_MS = 5000
        synchronized(mFrameSyncObject) {
            while (!mFrameAvailable) {
                try {
                    mFrameSyncObject.wait(TIMEOUT_MS.toLong())
                    if (!mFrameAvailable) {
                        throw RuntimeException("Surface frame wait timed out")
                    }
                } catch (ie: InterruptedException) {
                    throw RuntimeException(ie)
                }

            }
            mFrameAvailable = false
        }
        mTextureRender!!.checkGlError("before updateTexImage")
        mSurfaceTexture!!.updateTexImage()
    }

    fun drawImage(invert: Boolean) {
        mTextureRender!!.drawFrame(this!!.mSurfaceTexture!!, invert)
    }

    override fun onFrameAvailable(st: SurfaceTexture) {
        synchronized(mFrameSyncObject) {
            if (mFrameAvailable) {
                throw RuntimeException("mFrameAvailable already set, frame could be dropped")
            }
            mFrameAvailable = true
            mFrameSyncObject.notifyAll()
        }
    }

    fun getFrame(): ByteBuffer {
        mPixelBuf.rewind()
        GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mPixelBuf)
        return mPixelBuf
    }

    private fun checkEglError(msg: String) {
        if (mEGL!!.eglGetError() != EGL10.EGL_SUCCESS) {
            throw RuntimeException("EGL error encountered (see log)")
        }
    }
}