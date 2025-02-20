private object Polyominos {

    private enum class Tile(val variants: List<List<String>>) {
        A(listOf(listOf("A..", "AAA"), listOf(".A", ".A", "AA"), listOf("AAA", "..A"), listOf("AA", "A.", "A."), listOf("AA", ".A", ".A"), listOf("AAA", "A.."), listOf("A.", "A.", "AA"), listOf("..A", "AAA"))),
        B(listOf(listOf("B.B", "BBB"), listOf("BB", ".B", "BB"), listOf("BBB", "B.B"), listOf("BB", "B.", "BB"))),
        C(listOf(listOf(".C.", "CCC", ".C."))),
        D(listOf(listOf(".D.", "DDD"), listOf(".D", "DD", ".D"), listOf("DDD", ".D."), listOf("D.", "DD", "D."))),
        E(listOf(listOf(".E.", ".E.", "EEE"), listOf("..E", "EEE", "..E"), listOf("EEE", ".E.", ".E."), listOf("E..", "EEE", "E.."))),
        F(listOf(listOf("FFFF"), listOf("F", "F", "F", "F"))),
        G(listOf(listOf("GG.", ".GG", "..G"), listOf(".GG", "GG.", "G.."), listOf("G..", "GG.", ".GG"), listOf("..G", ".GG", "GG."))),
        H(listOf(listOf(".H", "HH"), listOf("HH", ".H"), listOf("HH", "H."), listOf("H.", "HH"))),
        I(listOf(listOf("II", ".I", "II", "I."), listOf("III.", "I.II"), listOf(".I", "II", "I.", "II"), listOf("II.I", ".III"), listOf("I.II", "III."), listOf("I.", "II", ".I", "II"), listOf(".III", "II.I"), listOf("II", "I.", "II", ".I"))),
        J(listOf(listOf("JJJ", "..J", "..J"), listOf("JJJ", "J..", "J.."), listOf("J..", "J..", "JJJ"), listOf("..J", "..J", "JJJ"))),
        K(listOf(listOf("KK", "KK"))),
        L(listOf(listOf(".LL", ".L.", "LL."), listOf("L..", "LLL", "..L"), listOf("..L", "LLL", "L.."), listOf("LL.", ".L.", ".LL"))),
        M(listOf(listOf(".MM", "MM."), listOf("M.", "MM", ".M"), listOf(".M", "MM", "M."), listOf("MM.", ".MM"))),
        N(listOf(listOf("NNN", "NN."), listOf("N.", "NN", "NN"), listOf(".NN", "NNN"), listOf("NN", "NN", ".N"), listOf("NN", "NN", "N."), listOf("NN.", "NNN"), listOf(".N", "NN", "NN"), listOf("NNN", ".NN")))
    }

    var foundSolution: List<Action> = emptyList()

    sealed interface Requirement {
        data class CellTaken(val x: Int, val y: Int) : Requirement
        data class DominoPlaced(val id: Char) : Requirement
    }

    data class Action(val polyomino: List<String>, val x: Int, val y: Int, val id: Char)

    class PolyominosSolver(
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>
    ) : DLXSolver<Requirement, Action>(requirements, actions) {
        override fun processSolution(solution: List<Action>): Boolean {
            foundSolution = solution
            return true
        }
    }

    fun canPlace(board: List<String>, tile: List<String>, x: Int, y: Int) : List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for (dy in tile.indices) {
            for (dx in tile[0].indices) {
                if (x + dx !in board[0].indices || y+dy !in board.indices) return emptyList()
                if (tile[dy][dx] == '.') continue
                if (board[dy + y][dx + x] != 'O') return emptyList()
                result += dx + x to dy + y
            }
        }
        return result
    }

    fun createSolver(height: Int, width: Int, board: List<String>, remaining: String): PolyominosSolver {
        val tiles = Tile.values().filter { it.name[0] in remaining }
        val requirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<Action, List<Requirement>>()
        for (tile in tiles) { requirements += Requirement.DominoPlaced(tile.name[0]) }

        for (y in 0..<height) {
            for (x in 0..<width) {
                if (board[y][x] == 'O') { requirements += Requirement.CellTaken(x, y) }
                for (tile in tiles) {
                    val tileId = tile.name[0]
                    for (tileVariant in tile.variants) {
                        val targets = canPlace(board, tileVariant, x, y)
                        if (targets.isNotEmpty()) {
                            actions[Action(tileVariant, x, y, tileId)] =
                                targets.map {
                                    (tx, ty) -> Requirement.CellTaken(tx, ty)
                                } + Requirement.DominoPlaced(tileId)
                        }
                    }
                }
            }
        }

        return PolyominosSolver(requirements, actions)
    }

    fun solve() {
        while (true) {
            val remaining = readln()
            val current = readln()
            val (h, w) = readln().split(" ").map{ it.toInt() }
            if (foundSolution.isEmpty()) {
                createSolver(h, w, List(h) { readln() }, remaining).solve()
            } else {
                repeat(h) { readln() }
            }

            val action = foundSolution.first { it.id == current[0] }
            val result = mutableListOf<Pair<Int,Int>>()
            for (y in 0..<action.polyomino.size) {
                for (x in 0..<action.polyomino[0].length) {
                    if (action.polyomino[y][x] != '.') result += x + action.x to y + action.y
                }
            }
            println(result.joinToString(" ") { "(${it.second},${it.first})"})
        }
    }
}