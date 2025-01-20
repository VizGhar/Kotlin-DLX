package examples

import DLXSolver
import java.util.Scanner

private object Suguru {

    sealed interface Requirement {
        data class CellCovered(val x: Int, val y: Int) : Requirement
        data class BoxCovered(val box: Char, val value: Char) : Requirement

        data class CellTaken(val x1: Int, val y1: Int, val x2: Int, val y2: Int, val symbol: Char) : Requirement
    }

    data class Action(val x: Int, val y: Int, val value: Char)

    class SuguruSolver(
        private val width: Int,
        private val height: Int,
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>,
        optionalRequirements: List<Requirement>
    ) : DLXSolver<Requirement, Action>(requirements, actions, optionalRequirements) {

        override fun processSolution(solution: List<Action>): Boolean {
            for (y in 0..<height) {
                for (x in 0..<width) {
                    print(solution.first { it.y == y && it.x == x }.value)
                }
                println()
            }
            return true
        }
    }

    fun createSolver(input: Array<Array<TileSpec>>, boxes: Map<Char, Int>): SuguruSolver {
        val requirements = mutableListOf<Requirement>()
        val optionalRequirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<Action, List<Requirement>>()
        val height = input.size
        val width = input[0].size

        for ((boxId, values) in boxes) {
            for (value in '1'..<'1' + values) {
                requirements += Requirement.BoxCovered(boxId, value)
            }
        }

        for (y in 0..<height) {
            for (x in 0..<width) {
                val (box, value) = input[y][x]
                for (v in if(value != '.') listOf(value) else '1'..<'1' + boxes[box]!!) {
                    val optionals = (maxOf(0, y - 1)..minOf(height-1, y + 1)).map {
                        ny -> (maxOf(0, x - 1)..minOf(width-1, x + 1)).map {
                            nx -> if (nx != x || ny != y) listOf(Requirement.CellTaken(nx, ny, x, y, v), Requirement.CellTaken(x, y, nx, ny, v)) else emptyList()
                        }
                    }.flatten().flatten()
                    optionalRequirements += optionals
                    actions[Action(x, y, v)] = listOf(
                        Requirement.CellCovered(x, y),
                        Requirement.BoxCovered(box, v)
                    ) + optionals
                }

                requirements += Requirement.CellCovered(x, y)
            }
        }

        return SuguruSolver(width, height, requirements, actions, optionalRequirements.distinct())
    }

    data class TileSpec(val name: Char, val value: Char)

    fun adjustBoxSpecs(board: Array<Array<TileSpec>>) : Map<Char, Int> {
        val width = board[0].size
        val height = board.size
        val marked = Array(height) { Array(width) { '.' } }

        var currentSymbol = 'A'

        while (marked.any { it.contains('.') }) {
            val unmarkedY = marked.indexOfFirst { it.contains('.') }
            val unmarkedX = marked[unmarkedY].indexOfFirst { it == '.' }
            val symbol = board[unmarkedY][unmarkedX].name
            val remaining = mutableListOf(unmarkedX to unmarkedY)
            while (remaining.isNotEmpty()) {
                val (x, y) = remaining.removeFirst()
                if (x < 0 || y < 0 || x >= width || y >= height) continue
                if (board[y][x].name != symbol || marked[y][x] == currentSymbol) continue
                marked[y][x] = currentSymbol
                remaining += x - 1 to y
                remaining += x to y - 1
                remaining += x + 1 to y
                remaining += x to y + 1
            }
            currentSymbol++
        }
        for (y in 0..<height) {
            for (x in 0..<width) {
                board[y][x] = TileSpec(marked[y][x], board[y][x].value)
            }
        }
        return board.flatten().map { it.name }.distinct().associateWith { boxName -> board.flatten().count { it.name == boxName } }
    }

    fun solveSuguru() {
        val scanner = Scanner(System.`in`)
        val w = scanner.nextInt()
        val h = scanner.nextInt()
        val board = Array(h) { Array(w) { scanner.next().let { TileSpec(it[0], it[1]) }} }
        val boxSpecs = adjustBoxSpecs(board)
        createSolver(board, boxSpecs).solve()
    }
}
