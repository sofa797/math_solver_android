package com.example.mathsolver.tests.domain

import com.example.mathsolver.domain.EquationSolver
import org.junit.Assert
import org.junit.Test

class EquationSolverTest {

    private val solver = EquationSolver()

    @Test
    fun testSimpleEquation() {
        val result = solver.solveLinear("2x + 4 = 0")
        Assert.assertEquals("x = -2.0", result)
    }

    @Test
    fun testNoSolution() {
        val result = solver.solveLinear("0x + 1 = 0")
        Assert.assertEquals("No solutions", result)
    }

    @Test
    fun testInfiniteSolutions() {
        val result = solver.solveLinear("0x + 0 = 0")
        Assert.assertEquals("Infinitely many solutions", result)
    }

    @Test
    fun testIncorrectEquationFormat() {
        val result = solver.solveLinear("3x - 9")
        Assert.assertEquals("The equation must contain '='", result)
    }
}