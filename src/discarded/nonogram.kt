package discarded

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
        data class PutHorizontal(val row: Int, val text: String) : Action
        data class PutVertical(val col: Int, val text: String) : Action
    }

    class NonogramSolver(
        private val width: Int,
        private val height: Int,
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>,
        optionalRequirements: MutableList<Requirement>
    ) : DLXSolver<Requirement, Action>(requirements, actions, optionalRequirements) {

        fun counter(text: String): List<Int> {
            val result = mutableListOf<Int>()
            var counter = 0
            for (c in text) {
                if (c == '.') counter++
                else {
                    if (counter != 0) result += counter
                    counter = 0
                }
            }
            if (counter != 0) result += counter
            return result.ifEmpty { listOf(0) }
        }

        override fun processSolution(solution: List<Action>): Boolean {
            val cols = solution.filterIsInstance<Action.PutVertical>()
            val rows = solution.filterIsInstance<Action.PutHorizontal>()

            for (y in 0..<height) { System.err.println(rows.first { it.row == y }.text) }
            for (x in 0..<width) { println(counter(cols.first { it.col == x }.text).joinToString(" ")) }
            for (y in 0..<height) { println(counter(rows.first { it.row == y }.text).joinToString(" ")) }

            return true
        }

    }

    fun generateActions(line: List<Int>, remaining: Int): List<String> {
        if (line.isEmpty()) return listOf(".".repeat(remaining))
        val result = mutableListOf<String>()
        val movableBy = remaining - line.sum() - (line.size - 1)
        for (i in 0..movableBy) {
            val currentEmptyAction = ".".repeat(i)
            val currentFillAction = "#".repeat(line[0])
            val afterAction = if (line.size > 1) "." else ""
            val action = currentEmptyAction + currentFillAction + afterAction
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

        val forcedConfig = Array(height) { Array(width) { '-' } }

        var verticalActions = topSide.indices.map { x -> generateActions(topSide[x], height).map { action -> Action.PutVertical(x, action) } }
        var horizontalActions = leftSide.indices.map { y -> generateActions(leftSide[y], width).map { action -> Action.PutHorizontal(y, action) } }

        while (true) {
            var changed = false
            for (x in verticalActions.indices) {
                (0..<height).forEach { y ->
                    if (verticalActions[x].all { it.text[y] == '#' } && forcedConfig[y][x] != '#') { forcedConfig[y][x] = '#'; changed = true }
                    if (verticalActions[x].all { it.text[y] == '.' } && forcedConfig[y][x] != '.') { forcedConfig[y][x] = '.'; changed = true }
                }
            }
            for (y in horizontalActions.indices) {
                (0..<width).forEach { x ->
                    if (horizontalActions[y].all { it.text[x] == '#' } && forcedConfig[y][x] != '#') { forcedConfig[y][x] = '#'; changed = true }
                    if (horizontalActions[y].all { it.text[x] == '.' } && forcedConfig[y][x] != '.') { forcedConfig[y][x] = '.'; changed = true }
                }
            }
            if (!changed) break
            verticalActions = verticalActions.mapIndexed { x, vert -> vert.filter { (0..<height).all { y -> forcedConfig[y][x] == '-' || it.text[y] == forcedConfig[y][x] }  } }
            horizontalActions = horizontalActions.mapIndexed { y, hori -> hori.filter { (0..<width).all { x -> forcedConfig[y][x] == '-' || it.text[x] == forcedConfig[y][x] }  } }
        }

        for (x in verticalActions.indices) {
            for(act in verticalActions[x]) {
                actions[act] = act.text.mapIndexed { y, c ->
                    if (c == '.') listOf(Requirement.MarkedEmpty(x, y, false), Requirement.MarkedFill(x, y, true))
                    else listOf(Requirement.MarkedFill(x, y, false), Requirement.MarkedEmpty(x, y, true))
                }.flatten() + Requirement.ColFilled(x)
            }
        }

        for (y in horizontalActions.indices) {
            for(act in horizontalActions[y]) {
                actions[act] = act.text.mapIndexed { x, c ->
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

    fun solve() {
        val (width, height) = readln().split(" ").map { it.toInt() }
        solve(
            List(width) { readln().split(" ").map { it.toInt() } },
            List(height) { readln().split(" ").map { it.toInt() } },
        )
    }
}