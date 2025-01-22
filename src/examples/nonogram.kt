package examples

import DLXCell
import DLXSolver
import kotlin.system.measureTimeMillis

private object Nonogram {

    sealed interface Requirement {
        data class RowFilled(val row: Int) : Requirement
        data class ColFilled(val row: Int) : Requirement
        data class MarkedEmpty(val x: Int, val y: Int, val horizontal: Boolean) : Requirement
        data class MarkedFill(val x: Int, val y: Int, val horizontal: Boolean) : Requirement
    }

    sealed interface Action {
        data class PutLineToRow(val row: Int, val text: String) : Action
        data class PutLineToCol(val col: Int, val text: String) : Action
    }

    class NonogramSolver(
        private val width: Int,
        private val height: Int,
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>,
        optionalRequirements: MutableList<Requirement>
    ) : DLXSolver<Requirement, Action>(requirements, actions, optionalRequirements) {

        override fun processSolution(solution: List<Action>): Boolean {
            val rows = solution.filterIsInstance<Action.PutLineToRow>()

            for (y in 0..<height) {
                println(rows.first { it.row == y}.text)
            }

            return true
        }

        override fun actionSortCriteria(rowHeader: DLXCell, action: Action): Int {
            return when (action) {
                is Action.PutLineToCol -> action.text.count { it == '.' }
                is Action.PutLineToRow -> action.text.count { it == '.' }
            }
        }
    }

    fun generateActions(line: List<Int>, remaining: Int): List<String> {
        if (line.isEmpty()) return listOf(".".repeat(remaining))
        val result = mutableListOf<String>()
        val movableBy = remaining - line.sum() - (line.size - 1)
        for (i in 0..movableBy) {
            val currentEmptyAction = ".".repeat(i)
            val currentFillAction = "#".repeat(line[0])
            val action = currentEmptyAction + currentFillAction
            result += generateActions(line.drop(1), remaining - action.length).map { action + it }
        }
        return result
    }

    fun solve(
        topSide: List<List<Int>>,
        leftSide: List<List<Int>>,
    ) {
        val requirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<Action, List<Requirement>>()
        val optionalRequirements = mutableListOf<Requirement>()

        val width = topSide.size
        val height = leftSide.size

        for (y in leftSide.indices) {
            for (x in topSide.indices) {
                optionalRequirements += Requirement.MarkedEmpty(x, y, false)
                optionalRequirements += Requirement.MarkedEmpty(x, y, true)
                optionalRequirements += Requirement.MarkedFill(x, y, false)
                optionalRequirements += Requirement.MarkedFill(x, y, true)
            }
        }

        for (y in leftSide.indices) { requirements += Requirement.RowFilled(y) }
        for (x in topSide.indices) { requirements += Requirement.ColFilled(x) }

        for (x in topSide.indices) {
            generateActions(topSide[x], height).forEach { action ->
                actions[Action.PutLineToCol(x, action)] = action.mapIndexed { y, c ->
                    if (c == '.') listOf(Requirement.MarkedEmpty(x, y, false), Requirement.MarkedFill(x, y, true))
                    else listOf(Requirement.MarkedFill(x, y, false), Requirement.MarkedEmpty(x, y, true))
                }.flatten() + Requirement.ColFilled(x)
            }
        }

        for (y in leftSide.indices) {
            generateActions(leftSide[y], width).forEach {
                actions[Action.PutLineToRow(y, it)] = it.mapIndexed { x, c ->
                    if (c == '.') listOf(Requirement.MarkedEmpty(x, y, true), Requirement.MarkedFill(x, y, false))
                    else listOf(Requirement.MarkedFill(x, y, true), Requirement.MarkedEmpty(x, y, false))
                }.flatten() + Requirement.RowFilled(y)
            }
        }


        val millis = measureTimeMillis {
            NonogramSolver(width, height, requirements, actions, optionalRequirements).solve()
        }

        println(millis)
    }
}

fun main() {
    val (width, height) = readln().split(" ").map { it.toInt() }
    Nonogram.solve(
        List(width) { readln().split(" ").map { it.toInt() } },
        List(height) { readln().split(" ").map { it.toInt() } },
    )
}