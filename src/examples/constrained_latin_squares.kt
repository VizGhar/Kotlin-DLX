package examples

import DLXSolver
import examples.ConstrainedLatinSquares.createSolver

private object ConstrainedLatinSquares {

    sealed interface Requirement {
        data class CellCovered(val row: Int, val col: Int) : Requirement
        data class RowCovered(val row: Int, val value: Char) : Requirement
        data class ColumnCovered(val col: Int, val value: Char) : Requirement
    }

    data class Action(val row: Int, val col: Int, val value: Char)

    class LatinSquareSolver(
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>
    ) : DLXSolver<Requirement, Action>(requirements, actions) {

        override fun onFinished(solutions: List<List<Action>>) {
            println(solutions.size)
        }

    }

    fun createSolver(size: Int, input: List<CharArray>): LatinSquareSolver {
        val validValues = List(size) { '1' + it }
        val requirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<Action, List<Requirement>>()
        for (row in 0..<size) {
            for (col in 0..<size) {
                requirements += Requirement.CellCovered(row, col)

                if (input[row][col] in validValues) {
                    val value = input[row][col]
                    actions[Action(row, col, value)] = listOf(
                        Requirement.CellCovered(row, col),
                        Requirement.RowCovered(row, value),
                        Requirement.ColumnCovered(col, value)
                    )
                } else {
                    for (value in validValues) {
                        actions[Action(row, col, value)] = listOf(
                            Requirement.CellCovered(row, col),
                            Requirement.RowCovered(row, value),
                            Requirement.ColumnCovered(col, value)
                        )
                    }
                }
            }
        }

        for (i in 0..<size) {
            for (value in validValues) {
                requirements += Requirement.RowCovered(i, value)
                requirements += Requirement.ColumnCovered(i, value)
            }
        }

        return LatinSquareSolver(requirements, actions)
    }
}

fun solveConstrainedLatinSquares() {
    val size = readln().toInt()
    createSolver(size, List(size){ readln().toCharArray() }).solve()
}