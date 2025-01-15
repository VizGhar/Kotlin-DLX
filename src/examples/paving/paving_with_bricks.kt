package examples.paving

import DLXSolver

// DLX solver inputs
sealed interface Requirement {
    data class CellCovered(val x: Int, val y: Int) : Requirement
}

data class Action(val x1: Int, val y1: Int, val x2: Int, val y2: Int)

// DLX solver implementation
class PavingWithBricksSolver(
    requirements: List<Requirement>,
    actions: Map<Action, List<Requirement>>
) : DLXSolver<Requirement, Action>(requirements, actions) {

    override fun onFinished(solutions: List<List<Action>>) {
        println(solutions.size)
    }
}

fun createSolver(height: Int, width: Int) : PavingWithBricksSolver {
    val requirements = mutableListOf<Requirement>()
    val actions = mutableMapOf<Action, List<Requirement>>()

    for (y in 0 until height) { for (x in 0 until width) requirements += Requirement.CellCovered(x,y) }

    for (y in 0 until height) {
        for (x in 0 until width) {
            if (x + 1 < width) actions[Action(x, y, x + 1, y)] = listOf(Requirement.CellCovered(x, y), Requirement.CellCovered(x + 1, y))
            if (y + 1 < height) actions[Action(x, y, x, y + 1)] = listOf(Requirement.CellCovered(x, y), Requirement.CellCovered(x, y + 1))
        }
    }
    return PavingWithBricksSolver(requirements, actions)
}

fun main() {
    createSolver(readln().toInt(), readln().toInt()).solve()
}