object EquationSearch {
    val solutions = mutableListOf<List<String>>()

    val unsolvable = mutableSetOf<Pair<List<Int>, List<Int>>>()
    fun solve(
        remainingRight: List<Int>,
        remainingOccurrences: List<Int>,
        solutionsSoFar: List<String> = emptyList()
    ): Boolean {
        if (unsolvable.contains(remainingRight to remainingOccurrences)) return false
        if (remainingRight.isEmpty() && remainingOccurrences.isEmpty()) {
            solutions += solutionsSoFar; return true
        }
        if (remainingRight.isEmpty()) return true

        val actual = remainingRight.first()

        val alreadySolved = mutableSetOf<Pair<Int, Int>>()
        var anySolved = false
        for (i in 0..remainingOccurrences.size - 2) {
            for (j in i + 1..remainingOccurrences.size - 1) {
                val f = remainingOccurrences[i]
                val s = remainingOccurrences[j]
                if (alreadySolved.contains(f to s)) continue
                alreadySolved += f to s
                if (f + s == actual) {
                    anySolved = solve(
                        remainingRight.drop(1),
                        remainingOccurrences.filterIndexed { index, _ -> index !in listOf(i, j) },
                        solutionsSoFar + "$f + $s = $actual"
                    ) || anySolved
                }
                if (f * s == actual) {
                    anySolved = solve(
                        remainingRight.drop(1),
                        remainingOccurrences.filterIndexed { index, _ -> index !in listOf(i, j) },
                        solutionsSoFar + "$f x $s = $actual"
                    ) || anySolved
                }
            }
        }
        if (!anySolved) {
            unsolvable += remainingRight to remainingOccurrences; return false
        }
        return true
    }

}

fun solveEquationSearch() {
    readln()
    val right = readln().split(" ").map { it.toInt() }
    val occurrences = readln().split(" ").mapIndexed { index, t -> List(t.toInt()) { index + 1 } }.flatten()

    EquationSearch.solve(right, occurrences)

    println(EquationSearch.solutions.size)
    if (EquationSearch.solutions.size == 1) {
        println(EquationSearch.solutions[0].joinToString("\n"))
    }
}