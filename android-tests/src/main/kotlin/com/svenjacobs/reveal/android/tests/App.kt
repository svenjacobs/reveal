package com.svenjacobs.reveal.android.tests

import android.app.Application
import android.util.Log
import com.svenjacobs.reveal.common.internal.log.Logger

class App : Application() {

	override fun onCreate() {
		super.onCreate()

		Logger.adapter = Logger.Adapter { message, tag -> Log.d(tag, message) }
	}
}
