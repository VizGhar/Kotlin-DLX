package examples

import DLXCell
import DLXSolver

private object Dumbells {

    enum class Orientation {
        VERTICAL, HORIZONTAL
    }

    sealed interface Requirement {
        data class SpaceMarked(val x: Int, val y: Int): Requirement
        data class DumbellUsed(val id: Int): Requirement
    }

    data class PlaceDumbell(val id: Int, val x: Int, val y: Int, val orientation: Orientation) {
        val takenFields get() = if (orientation == Orientation.VERTICAL) listOf(x to y - 1, x to y, x to y + 1) else listOf(x - 1 to y, x to y, x + 1 to y)
    }

    class Solver(
        val width: Int,
        val height: Int,
        requirements: List<Requirement>,
        actions: Map<PlaceDumbell, List<Requirement>>,
        optionalRequirements: List<Requirement>
    ) : DLXSolver<Requirement, PlaceDumbell>(requirements, actions, optionalRequirements) {

        override fun processSolution(solution: List<PlaceDumbell>): Boolean {
            for (y in 0..<height) {
                for (x in 0..<width) {
                    val action = solution.firstOrNull { it.takenFields.contains(x to y) }
                    when {
                        action == null -> print('.')
                        action.x to action.y != x to y -> print('o')
                        action.orientation == Orientation.HORIZONTAL -> print('-')
                        action.orientation == Orientation.VERTICAL -> print('|')
                    }
                }
                println()
            }
            return super.processSolution(solution)
        }

        data class HistoryItem(val x: Int, val y: Int, val orientation: Orientation)

        override fun processRowSelection(row: DLXCell, action: PlaceDumbell) {
            remember(HistoryItem(action.x, action.y, action.orientation))
        }

    }

    fun solve(
        dumbellsCount: Int,
        map: List<CharArray>
    ) {
        val requirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<PlaceDumbell, List<Requirement>>()
        val optionalRequirements = mutableListOf<Requirement>()

        val width = map[0].size
        val height = map.size

        for (id in 1..dumbellsCount) {
            requirements += Requirement.DumbellUsed(id)
        }

        for (y in 0..<height) {
            for (x in 0..<width) {
                val req = Requirement.SpaceMarked(x, y)
                if (map[y][x] == 'o') { requirements += req } else { optionalRequirements += req }

                val xSide = x == 0 || x == width - 1
                val ySide = y == 0 || y == height - 1
                if (xSide && ySide || map[y][x] == 'o') continue    // cant place dumbell here

                for (id in 1..dumbellsCount) {
                    if (!xSide) {
                        actions[PlaceDumbell(id, x, y, Orientation.HORIZONTAL)] = listOf(
                            Requirement.SpaceMarked(x - 1, y),
                            Requirement.SpaceMarked(x, y),
                            Requirement.SpaceMarked(x + 1, y),
                            Requirement.DumbellUsed(id)
                        )
                    }
                    if (!ySide) {
                        actions[PlaceDumbell(id, x, y, Orientation.VERTICAL)] = listOf(
                            Requirement.SpaceMarked(x, y - 1),
                            Requirement.SpaceMarked(x, y),
                            Requirement.SpaceMarked(x, y + 1),
                            Requirement.DumbellUsed(id)
                        )
                    }
                }
            }
        }

        Solver(width, height, requirements, actions, optionalRequirements).solve()
    }

    fun solveDumbells() {
        solve(readln().toInt(), (1..readln().split(" ")[0].toInt()).map { readln().toCharArray() })
    }
}
