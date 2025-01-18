package examples

import DLXSolver
import examples.Shikaku.createSolver
import java.awt.Rectangle
import java.util.Scanner

private object Shikaku {

    sealed interface Requirement {
        data class Covered(val col: Int, val row: Int) : Requirement
    }

    data class Action(
        val sourceCol: Int,
        val sourceRow: Int,
        val col: Int,
        val row: Int,
        val width: Int,
        val height: Int
    )

    // DLX solver implementation
    class ShikakuSkillBuilderSolver(
        private val width: Int,
        private val height: Int,
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>
    ) : DLXSolver<Requirement, Action>(requirements, actions) {

        override fun onFinished(solutions: List<List<Action>>) {
            println(solutions.size)
            val solutionsReadable = solutions.map { solution ->
                var activeMark = 'A'
                val marks = mutableMapOf<String, Char>()
                val result = mutableListOf<String>()
                for (row in 0 until height) {
                    var rowText = ""
                    for (col in 0 until width) {
                        val a = solution.first { Rectangle(it.col, it.row, it.width, it.height).contains(Rectangle(col, row, 1, 1)) }
                        val key = "${a.sourceCol}/${a.sourceRow}"
                        val mark = marks[key]
                        if (mark == null) {
                            rowText += activeMark
                            marks[key] = activeMark++
                            if (activeMark == '[') activeMark = 'a'
                        } else {
                            rowText += mark
                        }
                    }
                    result += rowText
                }
                result
            }
            println(solutionsReadable.minBy { it.joinToString("") }.joinToString("\n"))
        }
    }

    fun rectangles(area: Int): List<Pair<Int, Int>> {
        val results = mutableListOf<Pair<Int, Int>>()
        for (i in 1..area) {
            if (area % i == 0) {
                results += i to area / i
            }
        }
        return results
    }

    fun createSolver(input: List<List<Int>>): ShikakuSkillBuilderSolver {
        val requirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<Action, List<Requirement>>()

        val boardHeight = input.size
        val boardWidth = input[0].size

        val numbers = mutableListOf<Rectangle>()
        input.forEachIndexed { y, v -> v.forEachIndexed { x, value -> if (value != 0) numbers += Rectangle(x, y, 1, 1) } }

        for (col in 0 until input[0].size) {
            for (row in 0 until input.size) {
                requirements += Requirement.Covered(col, row)
            }
        }

        for (rect in numbers) {
            val number = input[rect.y][rect.x]
            val remainingNumbers = numbers - rect
            for ((w, h) in rectangles(number)) {
                for (targetCol in 0..boardWidth - w) {
                    for (targetRow in 0..boardHeight - h) {
                        // try to place square here
                        val placement = Rectangle(targetCol, targetRow, w, h)
                        if (remainingNumbers.any { placement.contains(it) }) continue
                        if (!placement.contains(rect)) continue
                        val targets = mutableListOf<Pair<Int, Int>>()
                        for (r in placement.y until placement.y + placement.height) {
                            for (c in placement.x until placement.x + placement.width) {
                                targets += c to r
                            }
                        }
                        actions[Action(rect.x, rect.y, targetCol, targetRow, w, h)] = targets.map { (col, row) ->
                            Requirement.Covered(col, row)
                        }
                    }
                }
            }
        }

        return ShikakuSkillBuilderSolver(input[0].size, input.size, requirements, actions)
    }
}

fun solveShikaku() {
    val scanner = Scanner(System.`in`)
    val w = scanner.nextInt()
    val h = scanner.nextInt()
    createSolver(List(h) { List(w) { scanner.nextInt() } }).solve()
}