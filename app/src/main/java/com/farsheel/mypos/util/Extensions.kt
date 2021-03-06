package com.farsheel.mypos.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor


fun Fragment.hideKeyboard(context: Context) {
    val windowToken = view?.rootView?.windowToken
    windowToken?.let {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}

val appEventProcessor: PublishProcessor<AppEvent> = PublishProcessor.create()
val appEventFlowable = appEventProcessor as Flowable<AppEvent>