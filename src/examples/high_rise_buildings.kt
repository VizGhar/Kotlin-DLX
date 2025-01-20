package examples

import DLXCell
import DLXSolver

private object HighRiseBuildings {

    sealed interface Requirement {
        data class CellTaken(val x: Int, val y: Int) : Requirement
        data class RowContains(val y: Int, val value: Int) : Requirement
        data class ColumnContains(val x: Int, val value: Int) : Requirement
    }

    data class PutBuilding(val x: Int, val y: Int, val value: Int)

    class HighRiseBuildingsSolver(
        val n: Int,
        val northRestrictions: List<Int>,
        val westRestrictions: List<Int>,
        val eastRestrictions: List<Int>,
        val southRestrictions: List<Int>,
        requirements: List<Requirement>,
        actions: Map<PutBuilding, List<Requirement>>
    ) : DLXSolver<Requirement, PutBuilding>(requirements, actions) {

        override fun processSolution(solution: List<PutBuilding>): Boolean {
            for (y in 0..<n) {
                println(
                    (0..<n).joinToString(" ") { x -> solution.first { it.x == x && it.y == y }.value.toString() }
                )
            }
            return true
        }

        fun status(values: List<Int>) : Int {
            var height = 0
            var count = 0
            for (i in 0..<n) {
                val value = values[i]
                if (value > height) { height = value; count++ }
            }
            return count
        }

        private val actualState = Array(n) { Array(n) { 0 } }

        override fun processRowSelection(row: DLXCell, action: PutBuilding) {
            actualState[action.y][action.x] = action.value

            val horizontalContent = actualState[action.y].asList()
            val verticalContent = actualState.map { it[action.x] }
            val horizontalFilled = horizontalContent.none { it == 0 }
            val verticalFilled = verticalContent.none { it == 0 }
            if (horizontalFilled && status(horizontalContent) != westRestrictions[action.y]) markAsInvalid()
            if (horizontalFilled && status(horizontalContent.reversed()) != eastRestrictions[action.y]) markAsInvalid()
            if (verticalFilled && status(verticalContent) != northRestrictions[action.x]) markAsInvalid()
            if (verticalFilled && status(verticalContent.reversed()) != southRestrictions[action.x]) markAsInvalid()
        }

        override fun processRowDeselection(row: DLXCell, action: PutBuilding) {
            actualState[action.y][action.x] = 0
        }
    }

    fun solveHighRiseBuildings() {
        val n = readln().toInt()
        val north = readln().split(" ").map { it.toInt() }
        val west = readln().split(" ").map { it.toInt() }
        val east = readln().split(" ").map { it.toInt() }
        val south = readln().split(" ").map { it.toInt() }
        val map = (1..n).map { readln().split(" ").map { it.toInt() } }

        val requirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<PutBuilding, List<Requirement>>()

        for (y in 0..<n) { for (x in 0..<n) { requirements += Requirement.CellTaken(x, y) } }
        for (y in 0..<n) { for (value in 1..n) { requirements += Requirement.RowContains(y, value) } }
        for (x in 0..<n) { for (value in 1..n) { requirements += Requirement.ColumnContains(x, value) } }

        for (y in 0..<n) {
            for (x in 0..<n) {
                for (value in if (map[y][x] == 0) 1..n else map[y][x]..map[y][x]) {
                    actions[PutBuilding(x, y, value)] = listOf(
                        Requirement.CellTaken(x, y),
                        Requirement.RowContains(y, value),
                        Requirement.ColumnContains(x, value)
                    )
                }
            }
        }

        HighRiseBuildingsSolver(n, north, west, east, south, requirements, actions).solve()
    }
}
