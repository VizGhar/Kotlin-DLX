package examples

import DLXSolver

private object Tetrasticks {

    val variants = mapOf(
        'F' to listOf("......", "..OO..", ".O....", ".O....", "..OO..", ".O....", ".O....", "......", "......",),
        'H' to listOf("......", "......", ".O....", ".O....", "..OO..", ".O..O.", ".O..O.", "......", "......",),
        'I' to listOf("...", "...", ".O.", ".O.", "...", ".O.", ".O.", "...", ".O.", ".O.", "...", ".O.", ".O.", "...", "..."),
        'J' to listOf("......", "......", "....O.", "....O.", "......", ".O..O.", ".O..O.", "..OO..", "......"),
        'L' to listOf("......", "......", ".O....", ".O....", "......", ".O....", ".O....", "......", ".O....", ".O....", "..OO..", "......"),
        'N' to listOf("......", "......", ".O....", ".O....", "..OO..", "....O.", "....O.", "......", "....O.", "....O.", "......", "......",),
        'O' to listOf("......", "..OO..", ".O..O.", ".O..O.", "..OO..", "......"),
        'P' to listOf("......","..OO..","....O.","....O.","..OO..",".O....",".O....","......","......"),
        'R' to listOf(".........", "..OO.....", "....O....", "....O....", ".....OO..", "....O....", "....O....", ".........", "........."),
        'T' to listOf(".........", "..OO.OO..", "....O....", "....O....", ".........", "....O....", "....O....", ".........", "........."),
        'U' to listOf(".........", ".........", ".O.....O.", ".O.....O.", "..OO.OO..", "........."),
        'V' to listOf(".........", ".........", ".......O.", ".......O.", ".........", ".......O.", ".......O.", "..OO.OO..", "........."),
        'W' to listOf(".........", ".....OO..", "....O....", "....O....", "..OO.....", ".O.......", ".O.......", ".........", "........."),
        'X' to listOf(".........", ".........", "....O....", "....O....", "..OO.OO..", "....O....", "....O....", ".........", "........."),
        'Y' to listOf("......", "......", ".O....", ".O....", "..OO..", ".O....", ".O....", "......", ".O....", ".O....", "......", "......"),
        'Z' to listOf(".........", "..OO.....", "....O....", "....O....", ".........", "....O....", "....O....", ".....OO..", ".........")
    )

    data class TileVariant(val id: Char, val flip: Boolean, val rotate: Int, val tile: List<String>)

    private fun getTileVariants(id: Char): List<TileVariant> {
        val tile = variants[id]!!
        val variants = mutableListOf<TileVariant>()
        for (flip in listOf(false, true)) {
            for (rightRotations in 0..3) {
                variants += TileVariant(id, flip, rightRotations, transformTetrastick(tile, flip, rightRotations))
            }
        }
        return variants.distinctBy { it.tile }
    }

    private fun transformTetrastick(shape: List<String>, flip: Boolean, rightRotations: Int): List<String> {
        var transformed = shape
        if (flip) { transformed = transformed.map { it.reversed() } }
        repeat(rightRotations % 4) { transformed = rotateRight(transformed) }
        return transformed
    }

    private fun rotateRight(shape: List<String>): List<String> {
        val height = shape.size
        val width = shape[0].length
        val rotated = MutableList(width) { StringBuilder(height) }
        for (y in shape.indices) { for (x in shape[y].indices) { rotated[x].insert(0, shape[y][x]) } }
        return rotated.map { it.toString() }
    }

    sealed interface Requirement {
        data class CellTaken(val x: Int, val y: Int) : Requirement
        data class PiecePlaced(val id: Char) : Requirement
        data class Cross(val x: Int, val y: Int) : Requirement
    }

    data class Action(val id: Char, val flip: Boolean, val rotate: Int, val x: Int, val y: Int)

    class TetrasticksSolver(
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>,
        opt: List<Requirement>,
    ) : DLXSolver<Requirement, Action>(requirements, actions, opt) {
        override fun processSolution(solution: List<Action>): Boolean {
            for (action in solution) {
                println("${action.id} ${if (action.flip) 1 else 0} ${action.rotate} ${action.y / 3} ${action.x / 3}")
            }
            return true
        }
    }

    private fun List<String>.chunked(chunkSize: Int = 3): List<List<String>> {
        val windows = mutableListOf<List<String>>()
        for (y in indices step chunkSize) {
            for (x in get(0).indices step chunkSize) {
                windows += (0..<chunkSize).map{ dy -> (0..<chunkSize).map { this[y + dy][x + it] }.joinToString("") }
            }
        }
        return windows
    }

    fun canPlace(board: List<String>, tile: List<String>, x: Int, y: Int) : List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for (ty in tile.indices) {
            for (tx in tile[0].indices) {
                if (y + ty !in board.indices || x + tx !in board[0].indices) { return emptyList() }
                if (tile[ty][tx] == 'O' && board[y + ty][x + tx] != '.') { return emptyList() }
                if (tile[ty][tx] == 'O') result += x + tx to y + ty
            }
        }

        // check intersections
        for (window in board.chunked()) {
            val val1 = window[0][1]
            val val2 = window[1][0]
            if (val1 == '.' || val2 == '.' || val1 == val2) continue
            if (val1 == window[2][1] && val2 == window[1][2]) return emptyList()
        }

        return result
    }

    fun getIntersections(tile: List<String>, offsetX: Int, offsetY: Int): List<Pair<Int, Int>> {
        val results = mutableListOf<Pair<Int, Int>>()
        for(y in 1..<tile.size-1 step 3) {
            for (x in 1..<tile[0].length - 1 step 3) {
                if (tile[y][x - 1] == 'O' && tile[y][x + 1] == 'O') { results += x + offsetX to y + offsetY }
                if (tile[y - 1][x] == 'O' && tile[y + 1][x] == 'O') { results += x + offsetX to y + offsetY }
            }
        }
        return results.distinct()
    }

    fun createSolver(board: List<String>, sticks: List<Char>): TetrasticksSolver {
        val tiles = variants.filter { it.key in sticks }.toList()
        val requirements = mutableListOf<Requirement>()
        val optionals = mutableListOf<Requirement>()
        val actions = mutableMapOf<Action, List<Requirement>>()
        for ((key, _) in tiles) { requirements += Requirement.PiecePlaced(key) }
        for (y in 0..17 step 3) {
            for (x in 0..17 step 3) {
                requirements += listOfNotNull(
                    Requirement.CellTaken(x + 1, y).takeIf { y > 0 && board[y][x + 1] == '.' },
                    Requirement.CellTaken(x, y + 1).takeIf { x > 0 && board[y + 1][x] == '.'},
                    Requirement.CellTaken(x + 2, y + 1).takeIf { x + 2 < 17 && board[y + 1][x + 2] == '.' },
                    Requirement.CellTaken(x + 1, y + 2).takeIf { y + 2 < 17 && board[y + 2][x + 1] == '.' }
                )

                optionals += Requirement.Cross(x + 1, y + 1)

                for ((id, _) in tiles) {
                    for ((_, flip, rotate, variant) in getTileVariants(id)) {
                        val targets = canPlace(board, variant, x, y)
                        if (targets.isEmpty()) continue
                        actions[Action(id, flip, rotate, x, y)] =
                            targets.map { (tx, ty) -> Requirement.CellTaken(tx, ty) } +
                                    Requirement.PiecePlaced(id) +
                                    getIntersections(variant, x, y).map { Requirement.Cross(it.first, it.second) }
                    }
                }
            }
        }

        return TetrasticksSolver(requirements, actions, optionals)
    }

    data class TetrastickDescriptor(val id: Char, val flip: Boolean, val rotate: Int, val x: Int, val y: Int)


    private fun forcePutOnBoard(placed: List<TetrastickDescriptor>, board: Array<Array<Char>>) {
        for ((tileName, flip, rotate, x, y) in placed) {
            val tetrastickDefaultShape = variants[tileName] ?: throw IllegalStateException("Won't happen")
            val tile = transformTetrastick(tetrastickDefaultShape, flip, rotate)
            outer@ for (dy in tile.indices) {
                for (dx in tile[0].indices) {
                    if (tile[dy][dx] == 'O') board[y * 3 + dy][x * 3 + dx] = tileName
                }
            }
        }
    }

    fun solve() {
        readln() // tile count
        val tiles = readln().split(" ").map { it[0] }
        val descriptors = List(readln().toInt()) {
            val (a,b,c,d,e) = readln().split(" ")
            TetrastickDescriptor(a[0], b == "1", c.toInt(), e.toInt(), d.toInt())
        }
        val board = Array(18) { Array(18) { '.' } }
        descriptors.forEach {
            forcePutOnBoard(descriptors, board)
        }

        createSolver(board.map { it.joinToString("") }, tiles).solve()
    }
}

fun main() {
    Tetrasticks.solve()
}