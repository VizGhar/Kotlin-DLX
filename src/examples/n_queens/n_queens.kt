package examples.n_queens

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
class NQueensSolver(
    requirements: List<Requirement>,
    actions: Map<Action, List<Requirement>>,
    optionalRequirements: List<Requirement>,
) : DLXSolver<Requirement, Action>(requirements, actions, optionalRequirements) {

    override fun onFinished(solutions: List<List<Action>>) {
        println(solutions.size)
    }
}

fun createSolver(queens: Int) : NQueensSolver {
    val requirements = mutableListOf<Requirement>()
    val actions = mutableMapOf<Action, List<Requirement>>()
    val optionalRequirements = mutableListOf<Requirement>()

    // single queen on single row
    for (y in 0..queens-1) {
        for (x in 0..queens-1) {
            actions[Action(x, y)] = listOf(
                Requirement.RowFilled(y),
                Requirement.ColFilled(x),
                Requirement.DiagonalA(queens - 1 - y + x),
                Requirement.DiagonalB(x + y),
            )
        }
    }

    for (q in 0..queens-1) {
        requirements += Requirement.ColFilled(q)
        requirements += Requirement.RowFilled(q)
    }

    for (d in 0..queens * 2 - 2) {
        optionalRequirements += Requirement.DiagonalA(d)
        optionalRequirements += Requirement.DiagonalB(d)
    }

    return NQueensSolver(requirements, actions, optionalRequirements)
}

// Data parsing

fun main() {
    val queens = readln().toInt()
    createSolver(queens).solve()
}