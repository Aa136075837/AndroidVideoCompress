package com.mac.macdocument

import android.content.Context
import android.widget.Toast

/**
 * @author ex-yangjb001
 * @date 2018/12/13.
 */
fun Context.toast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, msg, duration).show()