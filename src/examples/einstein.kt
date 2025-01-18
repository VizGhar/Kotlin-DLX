package examples

import DLXSolver
import examples.EinsteinsRiddle.createSolver
import java.util.*

private object EinsteinsRiddle {

    sealed interface Requirement {
        data class CharacteristicPlaced(val name: String) : Requirement
        data class CellCovered(val x: Int, val y: Int) : Requirement
        data class Connection(val a: String, val b: String, val column: Int) : Requirement
    }

    data class Action(val characteristic: String, val x: Int, val y: Int)

    class EinsteinsSolver(
        private val width: Int,
        private val height: Int,
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>,
        optionalRequirements: List<Requirement>
    ) : DLXSolver<Requirement, Action>(requirements, actions, optionalRequirements) {

        override fun processSolution(solution: List<Action>): Boolean {
            for (y in 0..<height) {
                println((0..<width).joinToString(" ") { x -> solution.first { it.x == x && it.y == y }.characteristic })
            }
            return true
        }
    }

    sealed interface Relation {
        data class Con(val a: String, val b: String) : Relation
        data class Dis(val a: String, val b: String) : Relation
    }

    fun createSolver(characteristics: List<List<String>>, relations: List<Relation>) : EinsteinsSolver {
        val requirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<Action, List<Requirement>>()
        val optionalRequirements = mutableListOf<Requirement>()

        // map relations for mutual exclusivity only
        val relationsAdjusted = relations.map {
            if (it is Relation.Con) {
                val (a, b) = it
                val bLine = characteristics.indexOfFirst { it.contains(b) }
                val characteristicsToAvoidB = characteristics[bLine].filter { it != b }
                val relationsB = characteristicsToAvoidB.map { c -> listOf(Relation.Dis(a, c), Relation.Dis(c, a)) }.flatten()

                val aLine = characteristics.indexOfFirst { it.contains(a) }
                val characteristicsToAvoidA = characteristics[aLine].filter { it != a }
                val relationsA = characteristicsToAvoidA.map { c -> listOf(Relation.Dis(b, c), Relation.Dis(c, b)) }.flatten()
                relationsA + relationsB
            } else {
                (it as Relation.Dis).let{ listOf(it, Relation.Dis(it.b, it.a)) }
            }
        }.flatten().distinct()

        // mandatory requirement - all cells of grid must be covered
        for (y in 0..<characteristics.size) {
            for (x in 0..<characteristics[0].size) {
                requirements += Requirement.CellCovered(x, y)
                requirements += Requirement.CharacteristicPlaced(characteristics[y][x])
            }
        }

        // Actions for first row (names) is always in given order
        // placing "name" on given column immediately satisfies connection between "name" and ! characteristic on given col
        // is also satisfies name
        for (nameId in characteristics[0].indices) {
            val name = characteristics[0][nameId]

            val diss = relationsAdjusted.filter { it.a == name }

            actions[Action(name, nameId, 0)] = listOf(
                Requirement.CellCovered(nameId, 0),
                Requirement.CharacteristicPlaced(name)
            ) + diss.map { (a, b) ->
                listOf(Requirement.Connection(a, b, nameId), Requirement.Connection(b, a, nameId))
            }.flatten()
        }

        // other rows (characteristics) can be on any x position but only on y
        for (y in 1..<characteristics.size) {
            val characteristicsInLine = characteristics[y]
            for (characteristic in characteristicsInLine) {

                val diss = relationsAdjusted.filter { it.a == characteristic }

                // invoke actions
                for (x in 0..<characteristics[y].size) {

                    actions[Action(characteristic, x, y)] = listOf(
                        Requirement.CellCovered(x, y),
                        Requirement.CharacteristicPlaced(characteristic)
                    ) + diss.map { (a, b) ->
                        listOf(Requirement.Connection(a, b, x), Requirement.Connection(b, a, x))
                    }.flatten()
                }
            }
        }

        for (a in characteristics.flatten()) {
            val lineToIgnore = characteristics.indexOfFirst { it.contains(a) }
            val c = characteristics.filterIndexed { y, _ -> y != lineToIgnore }.flatten()
            for (b in c) {
                for (x in 0..<characteristics[0].size) {
                    optionalRequirements += Requirement.Connection(a, b, x)
                }
            }
        }

        return EinsteinsSolver(characteristics[0].size, characteristics.size, requirements, actions, optionalRequirements)
    }
}

fun solveEinsteinsRiddle() {
    val input = Scanner(System.`in`)
    val nbCharacteristics = input.nextInt()
    val nbPeople = input.nextInt()

    val characteristics = List(nbCharacteristics) { List(nbPeople) { input.next() }.sorted() }
    val nbLinks = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }
    val relations = (0 until nbLinks).map {
        val (a, s, b) = input.nextLine().split(" ")
        if (s == "&") EinsteinsRiddle.Relation.Con(a, b) else EinsteinsRiddle.Relation.Dis(a, b)
    }

    createSolver(characteristics, relations).solve()
}