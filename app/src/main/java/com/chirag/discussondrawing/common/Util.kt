package com.chirag.discussondrawing.common

import android.content.Context
import android.net.ConnectivityManager

class Util {
    companion object {
        fun connectionAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (connectivityManager.activeNetworkInfo != null) {
                connectivityManager.activeNetworkInfo!!.isAvailable
            } else {
                false
            }
        }

        fun getTimeAgo(time: Long): String {
            val SECOND_MILLIS = 1000
            val MINUTE_MILLIS = 60 * SECOND_MILLIS
            val HOUR_MILLIS = 60 * MINUTE_MILLIS
            val DAY_MILLIS = 24 * HOUR_MILLIS

            //time *= 1000;
            val now = System.currentTimeMillis()
            if (time > now || time <= 0) {
                return ""
            }
            val diff = now - time
            return when {
                diff < MINUTE_MILLIS -> {
                    "Just Now"
                }
                diff < 2 * MINUTE_MILLIS -> {
                    "a minute ago"
                }
                diff < 59 * MINUTE_MILLIS -> {
                    "${diff / MINUTE_MILLIS}  min ago"
                }
                diff < 90 * MINUTE_MILLIS -> {
                    "an hour ago"
                }
                diff < 24 * HOUR_MILLIS -> {
                    "${diff / HOUR_MILLIS} hr ago"
                }
                diff < 48 * HOUR_MILLIS -> {
                    "Yesterday"
                }
                else -> {
                    "${diff / DAY_MILLIS} days ago"
                }
            }
        }
    }


}