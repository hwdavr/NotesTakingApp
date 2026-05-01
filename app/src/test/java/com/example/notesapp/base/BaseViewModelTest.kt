package com.example.notesapp.base

import com.example.notesapp.MainDispatcherRule
import org.junit.Rule

abstract class BaseViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
}
