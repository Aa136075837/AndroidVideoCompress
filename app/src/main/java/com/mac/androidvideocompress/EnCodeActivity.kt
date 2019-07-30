package com.mac.androidvideocompress

import android.os.Bundle
import android.widget.Toast
import com.mac.androidvideocompress.base.BaseActivity
import kotlinx.android.synthetic.main.activity_en_code.*

class EnCodeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_en_code)
        initEvent()
    }

    private fun initEvent() {
        mEncode.setOnClickListener {
            val trim = mEditText.text.toString().trim()
            if (trim.isEmpty()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
            } else {
                enCodeMd5String(trim)
            }
        }

        mEncodeRight.setOnClickListener {
            val msg = mEditTextRight.text.toString().trim()
            if (msg.isEmpty()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
            } else {
                aes2Encode(msg)
            }
        }

        mDecodeRight.setOnClickListener {
            aes2Decode()
        }
    }

    fun enCodeMd5String(msg: String) {
        mEditText.setText("")
        val mD5_32 = EncryptData.MD5_32(msg)
        mEcTextView.append(mD5_32 + "\n")
    }

    var encryptECB: String = ""
    fun aes2Encode(msg: String) {
        encryptECB = AES2.encryptECB("QWERQWERQWERQWERQWERQWER", msg, AES2.TRANSFORM_ECB_PKCS5PADDING)
        mEcTextViewRight.append(encryptECB + "\n")
    }

    fun aes2Decode() {
        val decryptECB = AES2.decryptECB("QWERQWERQWERQWERQWERQWER", encryptECB, AES2.TRANSFORM_ECB_PKCS5PADDING)
        mEcTextViewRight.append(decryptECB + "\n")
    }
}
