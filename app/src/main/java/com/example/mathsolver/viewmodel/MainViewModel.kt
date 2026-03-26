package com.example.mathsolver.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _input = MutableLiveData("")
    val input: LiveData<String> = _input

    fun setInput(value: String) {
        _input.value = value
    }

    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result

    fun setResult(value: String) {
        _result.postValue(value)
    }
}