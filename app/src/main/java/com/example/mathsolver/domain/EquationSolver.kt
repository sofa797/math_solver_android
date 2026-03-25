package com.example.mathsolver.domain

class EquationSolver {

    fun solveLinear(equation: String): String {
        val cleaned = equation.replace(" ", "")

        if (!cleaned.contains("=")) {
            return "The equation must contain '='"
        }

        val parts = cleaned.split("=")
        if (parts.size != 2) {
            return "Incorrect equation format"
        }

        val (aLeft, bLeft) = parseSide(parts[0])
        val (aRight, bRight) = parseSide(parts[1])

        val a = aLeft - aRight
        val b = bLeft - bRight

        if (a == 0.0) {
            return if (b == 0.0) {
                "Infinitely many solutions"
            } else {
                "No solutions"
            }
        }

        val x = -b / a
        return "x = $x"
    }

    private fun parseSide(side: String): Pair<Double, Double> {
        var a = 0.0
        var b = 0.0

        val tokens = Regex("([+-]?[^+-]+)").findAll(side)

        for (token in tokens) {
            val value = token.value.trim()
            if (value.isEmpty()) continue

            if (value.contains("x")) {
                val coeff = value.replace("x", "")
                a += when (coeff) {
                    "", "+" -> 1.0
                    "-" -> -1.0
                    else -> coeff.toDoubleOrNull() ?: 0.0
                }
            } else {
                b += value.toDoubleOrNull() ?: 0.0
            }
        }

        return Pair(a, b)
    }
}