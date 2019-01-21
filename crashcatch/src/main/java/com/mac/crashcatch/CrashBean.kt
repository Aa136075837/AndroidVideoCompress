package com.mac.crashcatch

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

/**
 * @author ex-yangjb001
 * @date 2019/1/9.
 */
class CrashBean() :Parcelable{
    lateinit var ex: Throwable

    lateinit var packageName: String

    lateinit var exceptionMsg: String
    lateinit var className: String
    lateinit var fileName: String
    lateinit var methodName: String
    lateinit var lineNumber: String
    lateinit var exceptionType: String
    lateinit var fullException: String
    var time: Long = 0L
    val device: Device = Device()

    constructor(parcel: Parcel) : this() {
        packageName = parcel.readString()
        exceptionMsg = parcel.readString()
        className = parcel.readString()
        fileName = parcel.readString()
        methodName = parcel.readString()
        lineNumber = parcel.readString()
        exceptionType = parcel.readString()
        fullException = parcel.readString()
        time = parcel.readLong()
    }

    class Device() : Parcelable {
        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.writeString(model)
            dest?.writeString(brand)
            dest?.writeString(androidVersion)
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun toString(): String {
            return "Device(model='$model', brand='$brand', androidVersion='$androidVersion')"
        }

        private val model: String = Build.MODEL
        private val brand = Build.BRAND
        private val androidVersion = Build.VERSION.SDK_INT.toString()

        constructor(parcel: Parcel) : this()

        companion object CREATOR : Parcelable.Creator<Device> {
            override fun createFromParcel(parcel: Parcel): Device {
                return Device(parcel)
            }

            override fun newArray(size: Int): Array<Device?> {
                return arrayOfNulls(size)
            }
        }

    }

    override fun toString(): String {
        return "CrashBean(ex=$ex, packageName='$packageName', exceptionMsg='$exceptionMsg', className='$className', fileName='$fileName', methodName='$methodName', lineNumber='$lineNumber', exceptionType='$exceptionType', fullException='$fullException', time=$time, device=$device)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(exceptionMsg)
        parcel.writeString(className)
        parcel.writeString(fileName)
        parcel.writeString(methodName)
        parcel.writeString(lineNumber)
        parcel.writeString(exceptionType)
        parcel.writeString(fullException)
        parcel.writeLong(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CrashBean> {
        override fun createFromParcel(parcel: Parcel): CrashBean {
            return CrashBean(parcel)
        }

        override fun newArray(size: Int): Array<CrashBean?> {
            return arrayOfNulls(size)
        }
    }


}