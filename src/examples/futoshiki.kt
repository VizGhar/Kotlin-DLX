package examples

import DLXSolver

private object Futoshiki {
    sealed interface Requirement {
        data class NumberPlaced(val x: Int, val y: Int, val number: Int) : Requirement
        data class RowSolved(val y: Int, val number: Int) : Requirement
        data class ColumnSolved(val x: Int, val number: Int) : Requirement
        data class CellCovered(val x: Int, val y: Int) : Requirement
    }

    data class PlaceTile(val x: Int, val y: Int, val number: Int)

    class FutoshikiSolver(
        val board: Array<Array<Int>>,
        requirements: List<Requirement>,
        actions: Map<PlaceTile, List<Requirement>>,
        optionalRequirements: List<Requirement>
    ): DLXSolver<Requirement, PlaceTile>(requirements, actions, optionalRequirements) {

        override fun processSolution(solution: List<PlaceTile>): Boolean {
            val size = board.size
            for (y in 0..<size) {
                for (x in 0 ..< size) {
                    print(solution.first { it.x == x && it.y == y }.number)
                }
                println()
            }
            return super.processSolution(solution)
        }
    }

    private fun createFutoshikiSolver(input: Input): FutoshikiSolver{
        val requirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<PlaceTile, List<Requirement>>()
        val optionalRequirements = mutableListOf<Requirement>()

        val size = input.board.size

        for (y in 0..<size) {
            for (x in 0..<size) {
                requirements += Requirement.RowSolved(y, x + 1)
                requirements += Requirement.ColumnSolved(y, x + 1)
                requirements += Requirement.CellCovered(y, x)
            }
        }

        for (y in 0..<size) {
            for (x in 0..<size) {
                for (number in if(input.board[y][x] == 0) 1..size else listOf(input.board[y][x])) {
                    val rightConnection = input.relations.firstOrNull { it.fromX == x && it.fromY == y && it.toY == y }?.let { c -> if (!c.fromIsHigher) (1..<number).map{Requirement.NumberPlaced(x+1, y, it)} else (number + 1..size).map{Requirement.NumberPlaced(x+1, y, it)}} ?: emptyList()
                    val bottomConnection = input.relations.firstOrNull { it.fromY == y && it.fromX == x && it.toX == x }?.let { c -> if (!c.fromIsHigher) (1..<number).map{Requirement.NumberPlaced(x, y+1, it)} else (number + 1..size).map{Requirement.NumberPlaced(x, y+1, it)}} ?: emptyList()
                    optionalRequirements += rightConnection + bottomConnection + Requirement.NumberPlaced(x, y, number)

                    actions[PlaceTile(x, y, number)] = listOf(
                        Requirement.RowSolved(y, number),
                        Requirement.ColumnSolved(x, number),
                        Requirement.CellCovered(x, y),
                        Requirement.NumberPlaced(x, y, number)
                    ) + rightConnection + bottomConnection
                }
            }
        }

        return FutoshikiSolver(input.board, requirements, actions, optionalRequirements.distinct())
    }

    class Input(val board: Array<Array<Int>>, val relations: List<Relation>)

    data class Relation(val fromX: Int, val fromY: Int, val toX: Int, val toY: Int, val fromIsHigher: Boolean)

    fun parseInput(): Input {
        val size = readln().toInt()
        val input = (1..size).map { readln() }
        val board = Array((size + 1) / 2) { Array((size + 1) / 2) { 0 } }
        val relations = mutableListOf<Relation>()
        for (y in input.indices) {
            for (x in input[y].indices) {
                if (x % 2 == 0 && y % 2 == 0) {
                    board[y / 2][x / 2] = input[y][x].digitToInt()
                } else if (input[y][x] != ' ') {
                    relations += if (x % 2 == 1) {
                        Relation((x - 1) / 2, y / 2, (x + 1) / 2, y / 2, input[y][x] == '>')
                    } else {
                        Relation(x / 2, (y - 1) / 2 , x / 2, (y + 1) /2, input[y][x] == 'v')
                    }
                }
            }
        }
        return Input(board, relations)
    }

    fun solveFutoshiki() {
        createFutoshikiSolver(parseInput()).solve()
    }
}
