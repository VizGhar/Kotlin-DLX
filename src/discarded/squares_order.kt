package discarded

fun solve() {
    val h = readln().substringBefore(" ").toInt()
    val squares = readln().toInt()
    val map = List(h) { readln() }

    var squarePositions = ('1'..'0' + squares).map { getSquarePosition(it, map) }

    val solution = mutableListOf<SquareSpecs>()

    while (squarePositions.isNotEmpty()) {
        val t = squarePositions.first { it.coveredBy.isEmpty() }
        solution.add(0, t)
        squarePositions = (squarePositions - t).map { it.copy(coveredBy = it.coveredBy.filter { it != t.id }) }
    }

    for (sp in solution) {
        println("${sp.id} ${sp.size}")
    }
}

data class SquareSpecs(val id: Char, val x: Int, val y: Int, val size: Int, val coveredBy: List<Char>)

fun cover(x: Int, y: Int, size: Int) = (
        (0..<size).map { listOf(
            x + it to y,
            x + it to y + size - 1,
            x to y + it,
            x + size - 1 to y + it)
        }).flatten().distinct()

fun getSquarePosition(id: Char, map: List<String>): SquareSpecs {
    val w = map[0].length
    val h = map.size
    val requiredCover = map.mapIndexed { y, s -> s.mapIndexedNotNull { x, c -> (x to y).takeIf { c == id } } }.flatten()

    for (x in 0..<w) {
        for (y in 0..<h) {
            for (size in 2..minOf(w - x, h - y)) {
                val targets = cover(x, y, size)
                if (requiredCover.all { it in targets } && targets.none { map[it.second][it.first] == '.' }) {
                    return SquareSpecs(id, x, y, size, targets.map { map[it.second][it.first] }.filter { it != id }.distinct())
                }
            }
        }
    }

    throw IllegalStateException("Cannot find unique placement for square")
}
