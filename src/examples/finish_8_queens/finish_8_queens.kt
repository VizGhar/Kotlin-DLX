package examples.finish_8_queens

import DLXSolver

// DLX solver inputs
sealed interface Requirement {
    data class RowFilled(val row: Int) : Requirement
    data class ColFilled(val col: Int) : Requirement
    data class DiagonalA(val id: Int): Requirement
    data class DiagonalB(val id: Int): Requirement
}

data class Action(val x: Int, val y: Int)

// DLX solver implementation
class Finish8QueensSolver(
    val board: Array<CharArray>,
    requirements: List<Requirement>,
    actions: Map<Action, List<Requirement>>,
    optionalRequirements: List<Requirement>,
) : DLXSolver<Requirement, Action>(requirements, actions, optionalRequirements) {

    override fun processSolution(solution: List<Action>): Boolean {
        for (y in 0..7) {
            for (x in 0..7) {
                if (board[y][x] == 'Q' || solution.any { it.y == y && it.x == x }) print("Q") else print(".")
            }
            println()
        }
        return true
    }
}

fun createSolver(board: Array<CharArray>) : Finish8QueensSolver {
    val requirements = mutableListOf<Requirement>()
    val actions = mutableMapOf<Action, List<Requirement>>()
    val optionalRequirements = mutableListOf<Requirement>()

    // single queen on single row
    for (y in 0..7) {
        val i = board[y].indexOf('Q')
        if (i != -1) {
            actions[Action(i, y)] = listOf(
                Requirement.RowFilled(y),
                Requirement.ColFilled(i),
                Requirement.DiagonalA(8 - 1 - y + i),
                Requirement.DiagonalB(i + y),
            )
            continue
        }
        for (x in 0..7) {
            actions[Action(x, y)] = listOf(
                Requirement.RowFilled(y),
                Requirement.ColFilled(x),
                Requirement.DiagonalA(8 - 1 - y + x),
                Requirement.DiagonalB(x + y),
            )
        }
    }

    for (q in 0..7) {
        requirements += Requirement.ColFilled(q)
        requirements += Requirement.RowFilled(q)
    }

    for (d in 0..14) {
        optionalRequirements += Requirement.DiagonalA(d)
        optionalRequirements += Requirement.DiagonalB(d)
    }

    return Finish8QueensSolver(board, requirements, actions, optionalRequirements)
}

fun main() {
    createSolver(Array(8) { readln().toCharArray() }).solve()
}