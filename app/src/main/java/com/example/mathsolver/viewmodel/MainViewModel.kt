package com.example.mathsolver.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mathsolver.domain.EquationSolver

class MainViewModel : ViewModel() {
    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result

    private val solver = EquationSolver()

    fun solveEquation(equation: String) {
        _result.value = solver.solveLinear(equation)
    }
}