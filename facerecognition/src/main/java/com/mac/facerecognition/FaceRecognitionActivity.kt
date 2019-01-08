package com.mac.facerecognition

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.params.Face
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.mac.facerecognition.utils.*
import com.mac.facerecognition.utils.RectUtil.rectFToRect
import kotlinx.android.synthetic.main.activity_face_recognition.*
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore

class FaceRecognitionActivity : AppCompatActivity(), ImageSaver.OnImageSaveListener {
    override fun onImageSaved(savedFile: File?) {
        runOnUiThread {
            Glide.with(this).load(savedFile).into(imageFace)
        }
    }

    var handler: Handler? = null
    val REQUEST_CAMERA_PERMISSION = 0x4564
    internal var mCameraOpenCloseLock = Semaphore(1)
    var mCameraDevice: CameraDevice? = null
    lateinit var mCameraId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_recognition)
        mFaceFaces.isOpaque = false
        CameraPermissionCompat.checkCameraPermission(this) {

        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mCameraOpenCloseLock.acquire()
            if (null != mCaptureSession) {
                mCaptureSession?.close()
                mCaptureSession = null
            }
            if (null != mCameraDevice) {
                mCameraDevice?.close()
                mCameraDevice = null
            }
            if (null != mImageReader) {
                mImageReader?.close()
                mImageReader = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
        }
    }

    private fun startBackgroundThread() {
        val handlerThread = HandlerThread("MacCamera")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        if (mFaceTexture.isAvailable) {
            openCamera(mFaceTexture.width, mFaceTexture.height)
        } else {
            mFaceTexture.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                    configureTransform(width, height)
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                    return true
                }

                override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                    openCamera(width, height)
                }
            }
        }
    }

    private fun openCamera(width: Int, height: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION)
            }
            return
        }
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        val manager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        manager.openCamera(mCameraId, mStateCallback, handler)
    }

    private val mStateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release()
            mCameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
            finish()
        }

    }

    lateinit var mPreviewRequestBuilder: CaptureRequest.Builder
    internal var mCaptureSession: CameraCaptureSession? = null
    internal lateinit var mPreviewRequest: CaptureRequest

    private fun createCameraPreviewSession() {
        try {
            val texture = mFaceTexture.surfaceTexture

            // We configure the size of default buffer to be the size of camera preview we want.
            texture?.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight())

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder.addTarget(surface)

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice!!.createCaptureSession(Arrays.asList(surface, mImageReader?.surface),
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession
                            try {

                                //set FaceDetection
                                mPreviewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE)

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build()
                                mCaptureSession!!.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, handler)

                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }

                        }

                        override fun onConfigureFailed(
                                cameraCaptureSession: CameraCaptureSession) {
                            Toast.makeText(this@FaceRecognitionActivity, "Failed", Toast.LENGTH_SHORT).show()
                        }
                    }, handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private val STATE_PREVIEW = 0
    private val STATE_PICTURE_TAKEN = 4
    private var mState = this.STATE_PREVIEW
    private var mFaces: Array<Face>? = null

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (mState) {
                STATE_PREVIEW -> {
                    mFaces = result.get(CaptureResult.STATISTICS_FACES)
                    val canvas = mFaceFaces.lockCanvas()
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    if (mFaces != null && mFaces!!.isNotEmpty()) {
                        val customFaces = computeFacesFromCameraCoordinates(mFaces!!)
                        for (customFace in customFaces) {
                            takePicture()
                        }
                    }
                    unlockCanvasAndPost(canvas)
                }
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession,
                                         request: CaptureRequest,
                                         partialResult: CaptureResult) {
            process(partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult) {
            process(result)
        }

    }

    private fun unlockCanvasAndPost(canvas: Canvas?) {
        mFaceFaces.unlockCanvasAndPost(canvas)
    }

    private fun takePicture() {
        mState = STATE_PICTURE_TAKEN
        captureStillPicture()
    }

    private fun captureStillPicture() {
        try {
            val activity = this
            if (null == activity || null == mCameraDevice) {
                return
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(mImageReader!!.getSurface())

            // Orientation
            val rotation = activity.windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))

            val CaptureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(session: CameraCaptureSession,
                                                request: CaptureRequest,
                                                result: TotalCaptureResult) {

                    unlockFocus()
                }
            }

            mCaptureSession!!.stopRepeating()
            mCaptureSession!!.capture(captureBuilder.build(), CaptureCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun unlockFocus() {
        try {
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW
            mCaptureSession!!.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    internal var ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private fun getOrientation(rotation: Int): Int {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360
    }

    private var mCameraRect: Rect? = null

    private fun computeFacesFromCameraCoordinates(faces: Array<Face>): Array<CustomFace?> {
        val mappedFacesList = arrayOfNulls<CustomFace>(faces.size)

        mCameraRect = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)

        for (i in faces.indices) {

            val mappedRect = RectF()

            val mCameraToPreviewMatrix = Matrix()

            mCameraToPreviewMatrix.mapRect(mappedRect, RectUtil.rectToRectF(faces[i].bounds))


            val auxRect = Rect(rectFToRect(mappedRect))


            val cameraSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

            val x = (mCameraRect!!.bottom - mCameraRect!!.top) / getmRealWidth()
            val y = (mCameraRect!!.right - mCameraRect!!.left) / getmRealHeight()

            if (mFacing == CameraCharacteristics.LENS_FACING_BACK) {
                when (cameraSensorOrientation) {
                    90 -> {
                        mappedRect.left = (mCameraRect!!.bottom - auxRect.bottom) / x
                        mappedRect.top = auxRect.left / y
                        mappedRect.right = (mCameraRect!!.bottom - auxRect.top) / x
                        mappedRect.bottom = auxRect.right / y
                    }
                    270 -> {
                        mappedRect.left = auxRect.top / x
                        mappedRect.top = (mCameraRect!!.right - auxRect.right) / y
                        mappedRect.right = auxRect.bottom / x
                        mappedRect.bottom = (mCameraRect!!.right - auxRect.left) / y
                    }
                }
            } else if (mFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                when (cameraSensorOrientation) {
                    270 -> {
                        mappedRect.left = (mCameraRect!!.bottom - auxRect.top) / x
                        mappedRect.top = (mCameraRect!!.right - auxRect.right) / y
                        mappedRect.right = (mCameraRect!!.bottom - auxRect.bottom) / x
                        mappedRect.bottom = (mCameraRect!!.right - auxRect.left) / y
                    }
                }
            } else {
                throw IllegalArgumentException("not support this camera!")
            }


            mappedFacesList[i] = CustomFace(rectFToRect(mappedRect))
        }

        return mappedFacesList

    }

    private fun getmRealHeight(): Float {
        return mFaceFaces.getmRealHeight().toFloat()
    }

    private fun getmRealWidth(): Float {
        return mFaceFaces.getmRealWidth().toFloat()
    }

    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val cameraSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        handler?.post(ImageSaver(reader.acquireNextImage(),
                cameraSensorOrientation, mCameraRect, cacheDir,this))
    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val activity = this
        if (mFaceTexture == null || null == mPreviewSize || null == activity) {
            return
        }
        val rotation = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, mPreviewSize.height.toFloat(), mPreviewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                    viewHeight.toFloat() / mPreviewSize.height,
                    viewWidth.toFloat() / mPreviewSize.width)
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        mFaceTexture.setTransform(matrix)
        mFaceFaces.setTransform(matrix)
    }

    private val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1080
    private lateinit var mCharacteristics: CameraCharacteristics
    private var mFacing: Int? = CameraCharacteristics.LENS_FACING_FRONT
    private var mImageReader: ImageReader? = null
    private var mSensorOrientation = 0
    private lateinit var mPreviewSize: Size

    private fun setUpCameraOutputs(width: Int, height: Int) {
        val activity = this
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            for (cameraId in manager.cameraIdList) {
                mCharacteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val facing = mCharacteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing != mFacing) {
                    continue
                }

                val map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        ?: continue

                // For still image captures, we use the largest available size.
                val largest = Collections.max(
                        Arrays.asList<Size>(*map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea())
                mImageReader = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, /*maxImages*/2)
                mImageReader?.setOnImageAvailableListener(mOnImageAvailableListener, handler)

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                val displayRotation = activity.windowManager.defaultDisplay.rotation

                mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                var swappedDimensions = false
                when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true
                    }
                    Surface.ROTATION_90, Surface.ROTATION_270 -> if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true
                    }
                }//                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);

                val displaySize = Point()
                activity.windowManager.defaultDisplay.getSize(displaySize)
                var rotatedPreviewWidth = width
                var rotatedPreviewHeight = height
                var maxPreviewWidth = displaySize.x
                var maxPreviewHeight = displaySize.y

                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxPreviewWidth = displaySize.y
                    maxPreviewHeight = displaySize.x
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest)

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                val orientation = getResources().getConfiguration().orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight())
                } else {
                    setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth())
                }

                mCameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
        }

    }

    private fun setAspectRatio(width: Int, height: Int) {
        mFaceTexture.setAspectRatio(width, height)
        mFaceFaces.setAspectRatio(width, height)
    }

    private fun chooseOptimalSize(choices: Array<Size>, textureViewWidth: Int,
                                  textureViewHeight: Int, maxWidth: Int, maxHeight: Int, largest: Size): Size {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough = ArrayList<Size>()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough = ArrayList<Size>()
        val w = largest.width
        val h = largest.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight &&
                    option.height == option.width * h / w) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        return if (bigEnough.size > 0) {
            Collections.min(bigEnough, CompareSizesByArea())
        } else if (notBigEnough.size > 0) {
            Collections.max(notBigEnough, CompareSizesByArea())
        } else {

            choices[0]
        }
    }
}
