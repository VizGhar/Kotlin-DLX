package examples

import DLXSolver

private object Piggies {
    sealed interface Requirement {
        data class TilePlaced(val id: Int): Requirement
        data class CellCovered(val x: Int, val y: Int) : Requirement
        data class PiggyCovered(val x: Int, val y: Int) : Requirement
        data object CoverEmptyCellsCalled: Requirement
    }

    sealed interface Action {
        data object CoverEmptyCells : Action
        data class PlaceTile(val shapeId: Int, val shape: List<String>, val offsetX: Int, val offsetY: Int) : Action
    }

    class PiggiesSolver(
        private val originalMap: List<String>,
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>,
        optionalRequirements: List<Requirement>
    ) : DLXSolver<Requirement, Action>(requirements, actions, optionalRequirements) {

        override fun processSolution(solution: List<Action>): Boolean {
            val result = Array(4) { y -> Array(4) { x -> originalMap[y][x] } }
            solution.filterIsInstance<Action.PlaceTile>().forEach { action ->
                action.shape.forEachIndexed { sy, s -> s.forEachIndexed { sx, c ->
                    if (c != '.') result[sy + action.offsetY][sx + action.offsetX] = c
                } }
            }
            println(
                result.joinToString("\n") { it.joinToString("") }
            )
            return false
        }

    }

    data class Tile(val id: Int, val shape: List<String>) {
        fun orientations(): List<Tile> {
            val result = mutableListOf(this)
            var shape = shape.map { it }
            for (i in 0..2) {
                shape = List (shape[0].length) { r -> shape.map { it[r] }.joinToString("").reversed() }
                result += Tile(id, shape)
            }
            return result
        }
    }

    fun createSolver(map: List<String>) : PiggiesSolver {
        val requirements = mutableListOf<Requirement>()
        val optionalRequirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<Action, List<Requirement>>()

        val isDay = map.none { it.contains('W') }

        val trees = mutableListOf<Requirement.CellCovered>()
        val wolf = mutableListOf<Requirement.CellCovered>()
        val pigs = mutableListOf<Requirement.CellCovered>()
        val grass = mutableListOf<Requirement.CellCovered>()

        // cover trees (.) and wolf (W) symbols by using CoverEmptyCells action
        map.forEachIndexed { y, t ->
            t.forEachIndexed { x, s ->
                val requirement = Requirement.CellCovered(x, y)
                when (s) {
                    '.' -> trees += requirement
                    'W' -> wolf += requirement
                    'P' -> pigs += requirement
                    'X' -> grass += requirement
                }
            }
        }

        // cover wolf and trees by default and pigs during day (these spaces won't be used)
        requirements += trees + wolf + pigs + Requirement.CoverEmptyCellsCalled
        optionalRequirements += grass
        actions[Action.CoverEmptyCells] = trees + wolf + Requirement.CoverEmptyCellsCalled + if (isDay) pigs else emptyList()

        // during night require to cover piggies
        if (!isDay) { requirements+= pigs.map { p -> Requirement.PiggyCovered(p.x, p.y) } }

        val tiles = (
            Tile(0, listOf("Hs", "s.")).orientations() +
            Tile(1, listOf("SHS")).orientations() +
            Tile(2, listOf("HBB", "B..")).orientations()
        ).distinct()


        for (tile in tiles) {
            for (offsetY in 0..map.size - tile.shape.size) {
                for (offsetX in 0..map[0].length - tile.shape[0].length) {
                    val piggyX = offsetX + tile.shape.firstNotNullOf { it.indexOfFirst { it == 'H' }.takeIf { it != -1 } }
                    val piggyY = offsetY + tile.shape.indexOfFirst { it.contains('H') }

                    val cellCoverage = tile.shape.mapIndexed { y, s -> s.mapIndexedNotNull { x, c -> if (c == '.') null else Requirement.CellCovered(
                        x + offsetX,
                        y + offsetY
                    )
                    } }.flatten()
                    val piggyCoverage = Requirement.PiggyCovered(piggyX, piggyY)
                    actions[Action.PlaceTile(tile.id, tile.shape, offsetX, offsetY)] =
                        cellCoverage + Requirement.TilePlaced(tile.id) + if (!isDay && map[piggyY][piggyX] == 'P') listOf(piggyCoverage) else emptyList()
                }
            }
        }

        requirements += Requirement.TilePlaced(0)
        requirements += Requirement.TilePlaced(1)
        requirements += Requirement.TilePlaced(2)

        return PiggiesSolver(map, requirements, actions, optionalRequirements)
    }
}

fun solveThreeLittlePiggies() {
    Piggies.createSolver((0..3).map { readln() }).solve()
}