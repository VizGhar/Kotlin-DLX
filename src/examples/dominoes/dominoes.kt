package examples.dominoes

import DLXSolver
import java.util.Scanner

// DLX solver inputs
sealed interface Requirement {
    data class CellTaken(val x: Int, val y: Int): Requirement
    data class DominoPlaced(val domino: Pair<Int, Int>): Requirement
}

data class Action(val domino: Pair<Int, Int>, val fx: Int, val fy: Int, val sx: Int, val sy: Int, val symbol: Char)

// DLX solver implementation
class DominoesSolver(
    private val width: Int,
    private val height: Int,
    requirements: List<Requirement>,
    actions: Map<Action, List<Requirement>>
) : DLXSolver<Requirement, Action>(requirements, actions) {
    override fun processSolution(solution: List<Action>): Boolean {
        println(solution)
        for (y in 0 .. height-1) {
            for (x in 0 .. width -1) {
                print(solution.first { (it.fx == x || it.sx == x) && (it.fy == y || it.sy == y) }.symbol)
            }
            println()
        }
        return true
    }
}

fun createSolver(n: Int, height: Int, width: Int, board: List<List<Int>>) : DominoesSolver {
    val requirements = mutableListOf<Requirement>()
    val actions = mutableMapOf<Action, List<Requirement>>()

    val dominoes = (0..n).map { a -> (a..n).map { b -> a to b } }.flatten()

    for (domino in dominoes) requirements += Requirement.DominoPlaced(domino)
    for (y in 0..height - 1) { for (x in 0..width - 1) requirements += Requirement.CellTaken(x, y) }

    for (domino in dominoes) {
        // 0, 1 -> horizontal placement // 2, 3 -> vertical placement
        for (y in 0..height - 1) { for (x in 0..width - 2) { if(board[y][x] == domino.first && board[y][x + 1] == domino.second) actions[Action(domino, x, y, x + 1, y, '=')] = listOf(Requirement.CellTaken(x, y), Requirement.CellTaken(x + 1, y), Requirement.DominoPlaced(domino)) } }
        for (y in 0..height - 1) { for (x in 1..width - 1) { if(board[y][x] == domino.first && board[y][x - 1] == domino.second) actions[Action(domino, x, y, x - 1, y, '=')] = listOf(Requirement.CellTaken(x - 1, y), Requirement.CellTaken(x, y), Requirement.DominoPlaced(domino)) } }
        for (y in 0..height - 2) { for (x in 0..width - 1) { if(board[y][x] == domino.first && board[y + 1][x] == domino.second) actions[Action(domino, x, y, x, y + 1, '|')] = listOf(Requirement.CellTaken(x, y), Requirement.CellTaken(x, y + 1), Requirement.DominoPlaced(domino)) } }
        for (y in 1..height - 1) { for (x in 0..width - 1) { if(board[y][x] == domino.first && board[y - 1][x] == domino.second) actions[Action(domino, x, y, x, y - 1, '|')] = listOf(Requirement.CellTaken(x, y - 1), Requirement.CellTaken(x, y), Requirement.DominoPlaced(domino)) } }
    }

    return DominoesSolver(width, height, requirements, actions)
}

fun main() {
    val scanner = Scanner(System.`in`)
    val n = scanner.nextInt()
    val h = scanner.nextInt()
    val w = scanner.nextInt()
    scanner.nextLine()
    createSolver(n, h, w, List(h) { scanner.nextLine().map { it.digitToInt() } }).solve()
}
