package com.farsheel.mypos.util

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager
import java.text.NumberFormat
import java.util.*


class Util {
    companion object {
        @JvmStatic
        fun currencyLocale(value: Double): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            return formatter.format(value)
        }


        fun hideKeyboard(activity: Activity) {
            val imm =
                activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            if (activity.currentFocus != null) {
                imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
            }
        }
    }
}