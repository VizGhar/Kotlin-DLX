package examples.sudoku_solver

import DLXSolver

const val sudokuSize = 25
const val boxWidth = 5
val validValues = List(sudokuSize) { 'A' + it }

// DLX solver inputs
sealed interface Requirement {
    data class CellCovered(val row: Int, val col: Int) : Requirement
    data class BoxCovered(val box: Int, val value: Char) : Requirement
    data class RowCovered(val row: Int, val value: Char) : Requirement
    data class ColumnCovered(val col: Int, val value: Char) : Requirement
}

data class Action(val row: Int, val col: Int, val value: Char)

// DLX solver implementation
class SudokuSolver(
    requirements: List<Requirement>,
    actions: Map<Action, List<Requirement>>
) : DLXSolver<Requirement, Action>(requirements, actions) {

    override fun processSolution(solution: List<Action>): Boolean {
        for (row in 0 until sudokuSize) {
            for (col in 0 until sudokuSize) {
                print(solution.first { it.col == col && it.row == row }.value)
            }
            println()
        }
        return true
    }
}

fun createSolver(input: List<CharArray>) : SudokuSolver {
    val requirements = mutableListOf<Requirement>()
    val actions = mutableMapOf<Action, List<Requirement>>()
    for (row in 0 until sudokuSize) {
        for (col in 0 until sudokuSize) {
            requirements += Requirement.CellCovered(row, col)

            val box = row / boxWidth * boxWidth + col / boxWidth
            if (input[row][col] in validValues) {
                val value = input[row][col]
                actions[Action(row, col, value)] = listOf(
                    Requirement.CellCovered(row, col),
                    Requirement.BoxCovered(box, value),
                    Requirement.RowCovered(row, value),
                    Requirement.ColumnCovered(col, value)
                )
            } else {
                for (value in validValues) {
                    actions[Action(row, col, value)] = listOf(
                        Requirement.CellCovered(row, col),
                        Requirement.BoxCovered(box, value),
                        Requirement.RowCovered(row, value),
                        Requirement.ColumnCovered(col, value)
                    )
                }
            }
        }
    }

    for (i in 0 until sudokuSize) {
        for (value in validValues) {
            requirements += Requirement.RowCovered(i, value)
            requirements += Requirement.ColumnCovered(i, value)
            requirements += Requirement.BoxCovered(i, value)
        }
    }

    return SudokuSolver(requirements, actions)
}

fun main() {
    createSolver(List(sudokuSize){ readln().toCharArray() }).solve()
}