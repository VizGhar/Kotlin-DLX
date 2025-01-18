package examples

import DLXSolver

/**
 * https://www.codingame.com/training/hard/who-dunnit
 */
private object WhoDunnit {

    data class ItemMarked(val x: Int, val y: Int)

    data class MarkForUse(val item: String)

    class Solver(
        private val suspects: List<String>,
        private val onCriminalFound: (String) -> Unit,
        requirements: List<ItemMarked>,
        actions: Map<MarkForUse, List<ItemMarked>>
    ) : DLXSolver<ItemMarked, MarkForUse>(requirements, actions) {

        override fun processSolution(solution: List<MarkForUse>): Boolean {
            onCriminalFound(solution.first { it.item in suspects }.item)
            return true
        }

    }

    fun solve(
        suspects: List<String>,
        choices: List<List<String>>,
        onCriminalFound: (String) -> Unit
    ) {
        val requirements = mutableListOf<ItemMarked>()
        val actions = mutableMapOf<MarkForUse, List<ItemMarked>>()

        choices.indices.forEach { y -> choices[y].indices.forEach { x -> requirements += ItemMarked(x, y) } }

        for (choice in choices.flatten().distinct()) {
            actions[MarkForUse(choice)] = choices.indices.mapNotNull { y ->
                (0..<choices[y].size).map { x -> ItemMarked(x, y) }.takeIf { choices[y].contains(choice) }
            }.flatten()
        }

        Solver(suspects, onCriminalFound, requirements, actions).solve()
    }

}

fun solveWhoDunnit() {
    val l = readln().split(" ")[0].toInt()
    val all = (1..l).map { readln().split(", ") }
    WhoDunnit.solve(all.first(), all) { println(it) }
}