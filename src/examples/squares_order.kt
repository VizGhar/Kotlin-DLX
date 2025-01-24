package examples

import DLXSolver

// https://www.codingame.com/ide/puzzle/squares-order
private object SquaresOrder {

    sealed interface Requirement {
        data class TileTaken(val type: Char, val x: Int, val y: Int): Requirement
    }

    sealed interface Action {
        data class Place(val type: Char, val x: Int, val y: Int, val size: Int, val zIndex: Int)
    }

    private class SquaresOrderSolver(
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>,
        optionalRequirements: List<Requirement>,
    ) : DLXSolver<Requirement, Action>(requirements, actions, optionalRequirements) {

    }

    fun corner(w: Int, h: Int, map: List<String>, x: Int, y: Int): Int {
        val value = map[y][x]
        if (value == '.') return 0
        return when {
            x > 0     && y > 0     && map[y][x + 1] == value && map[y+1][x] == value -> 1
            x < w - 1 && y > 0     && map[y][x - 1] == value && map[y+1][x] == value -> 2
            x > 0     && y < h - 1 && map[y][x + 1] == value && map[y-1][x] == value -> 3
            x < w - 1 && y < h - 1 && map[y][x - 1] == value && map[y-1][x] == value -> 4
            else -> 0
        }
    }

    fun solve(h: Int, w: Int, squares: Int, map: List<String>) {
        val requirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<Action, List<Requirement>>()
        val optionalRequirements = mutableListOf<Requirement>()

        map.forEachIndexed { y, s ->
            s.forEachIndexed { x, c ->
                when (c) {
                    '.' -> {}
                    else -> requirements += Requirement.TileTaken(c, x, y)
                }
            }
        }

        map.forEachIndexed { y, s ->
            s.indices.forEach { x ->
                when (corner(w, h, map, x, y)) {
                    1 -> {}
                    2 -> {}
                    3 -> {}
                    4 -> {}
                    else -> {}
                }
            }
        }

        SquaresOrderSolver(requirements, actions, optionalRequirements)
    }
}

fun main() {
    val (h, w) = readln().split(" ").map { it.toInt() }
    val nb = readln().toInt()
    SquaresOrder.solve(h, w, nb, List(h) { readln() })
}