package com.example.notesapp

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Instrumentation runner that swaps the production [Application] for [HiltTestApplication] so
 * `@HiltAndroidTest` suites can inject fakes into the real production graph.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(classLoader: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(classLoader, HiltTestApplication::class.java.name, context)
}
