package examples

import DLXCell
import DLXSolver
import kotlin.math.pow

private object MrsKnuth3 {

    val schedulingScoresMorning = arrayOf(15, 12, 9, 6, 3)
    val schedulingScoresAfternoon = arrayOf(10, 8, 6, 4, 2)
    val hours = mapOf(8 to 8, 9 to 9, 10 to 10, 11 to 11, 1 to 13, 2 to 14, 3 to 15, 4 to 16, 5 to 17)
    val loudInstruments = setOf("Trumpet", "Drums", "Trombone")

    sealed interface Requirement {
        data class SlotFilled(val day: Int, val hour: Int) : Requirement
        data class StudentScheduled(val name: String, val order: Int) : Requirement
        data class InstrumentOnDay(val instrument: String, val day: Int) : Requirement
        data class LoudInstruments(val day: Int, val hour: Int, val hour2: Int) : Requirement
        data class Troublesome(val day: Int, val hour: Int, val hour2: Int, val name1: String, val name2: String) : Requirement
    }

    data class Action(
        val studentName: String,
        val instrument: String,
        val day: Int,
        val timeSlot: Int,
        val lesson: Int
    )

    private data class HistoryItem(val studentName: String, val day: Int, val timeSlot: Int)

    class MrsKnuth2Solver(
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>,
        optionalRequirements: List<Requirement>,
    ) : DLXSolver<Requirement, Action>(requirements, actions, optionalRequirements) {

        override fun processRowSelection(row: DLXCell, action: Action) {
            remember(HistoryItem(action.studentName, action.day, action.timeSlot))
        }

        fun printSolution(solution: List<Action>, scoring: List<Scoring>) {
            for (time in listOf(-1, 8, 9, 10, 11, 0, 1, 2, 3, 4)) {
                if (time == -1) {
                    println("       Monday        Tuesday       Wednesday       Thursday        Friday"); continue
                }
                if (time == 0) {
                    println("       LUNCH          LUNCH          LUNCH          LUNCH          LUNCH"); continue
                }
                print("$time".padStart(2) + " ")
                println((0..4).joinToString(" ") { day ->
                    val text = solution.firstOrNull { it.day == day && it.timeSlot == time }
                        ?.let { "${it.studentName}/${it.instrument}" } ?: "--------------"
                    val spaceStart = (14 - text.length) / 2
                    val spaceEnd = 14 - text.length - spaceStart
                    "${" ".repeat(spaceStart)}$text${" ".repeat(spaceEnd)}"
                }.trimEnd())
            }
            println()
            println("${scoring.joinToString("+") { it.spacesScore.toString() }}=${scoring.sumOf { it.spacesScore }}")
            println("${scoring.joinToString("+") { it.loudInstrumentsScore.toString() }}=${scoring.sumOf { it.loudInstrumentsScore }}")
            println("${scoring.joinToString("+") { it.schedulingScore.toString() }}=${scoring.sumOf { it.schedulingScore }}")
            println("${scoring.joinToString("+") { it.alphabetScore.toString() }}=${scoring.sumOf { it.alphabetScore }}")
            println(scoring.sumOf { it.spacesScore + it.loudInstrumentsScore + it.schedulingScore + it.alphabetScore })
        }

        data class Scoring(
            val spacesScore: Int,
            val loudInstrumentsScore: Int,
            val schedulingScore: Int,
            val alphabetScore: Int
        )

        fun countConsecutiveGroups(numbers: List<Int>): List<Int> {
            if (numbers.isEmpty()) return emptyList()

            val result = mutableListOf<Int>()
            var count = 1

            for (i in 1 until numbers.size) {
                if (numbers[i] == numbers[i - 1] + 1) {
                    count++
                } else {
                    result.add(count)
                    count = 1
                }
            }
            result.add(count)

            return result
        }

        override fun onFinished(solutions: List<List<Action>>) {
            val solutionWithDetails = solutions.map { solution ->

                val scoring = (0..4).map { day ->
                    val thisDay = solution.filter { it.day == day }

                    val hrs = thisDay.map { hours[it.timeSlot]!! - 8 }
                    val spaces = countConsecutiveGroups((0..8).filter { it !in hrs })

                    val thisDaySpaces = spaces.sumOf { 2.0.pow(it) }.toInt()
                    val thisDayLoud = thisDay.sumOf { action -> if (action.timeSlot > 7 && action.instrument in loudInstruments) 50 else 0L }
                    val thisDaySchedule = thisDay.sumOf { action -> if (action.timeSlot > 7) schedulingScoresMorning[action.day] else schedulingScoresAfternoon[action.day] }
                    val thisDayAlphabet = thisDay.sortedBy { hours[it.timeSlot] }.windowed(2).sumOf { if (it.map { it.studentName } == it.map { it.studentName }.sorted()) 15 else 0L }
                    Scoring(thisDaySpaces, thisDayLoud.toInt(), thisDaySchedule, thisDayAlphabet.toInt())
                }

                solution to scoring
            }.maxBy { it.second.sumOf { it.spacesScore + it.loudInstrumentsScore + it.alphabetScore + it.schedulingScore } }

            printSolution(scoring = solutionWithDetails.second, solution = solutionWithDetails.first)
        }
    }

    fun createSolver(
        teacherCalendar: Map<Int, List<Int>>,
        studentInfo: List<StudentInfo>,
        troublesomes: List<Pair<String, String>>
    ): MrsKnuth2Solver {
        val mapOfTroubles = troublesomes.map { listOf(it, it.second to it.first) }.flatten().toMap()

        val requirements = mutableListOf<Requirement>()
        val actions = mutableMapOf<Action, List<Requirement>>()
        val optionalRequirements = mutableListOf<Requirement>()

        // create actions
        for (student in studentInfo) {
            for ((day, calendar) in student.calendar) {
                for (timeSlot in calendar) {
                    for (i in 1..student.times) {
                        actions[Action(student.name, student.instrument, day, timeSlot, i)] = listOfNotNull(
                            Requirement.StudentScheduled(student.name, i),
                            Requirement.SlotFilled(day, timeSlot),
                            Requirement.InstrumentOnDay(student.instrument, day),
                            Requirement.LoudInstruments(day, timeSlot, timeSlot + 1).takeIf { student.instrument in listOf("Trumpet", "Drums", "Trombone") && teacherCalendar[day]?.contains(timeSlot + 1) ?: false },
                            Requirement.LoudInstruments(day, timeSlot - 1, timeSlot).takeIf { student.instrument in listOf("Trumpet", "Drums", "Trombone") && teacherCalendar[day]?.contains(timeSlot - 1) ?: false },
                            mapOfTroubles[student.name]?.let { Requirement.Troublesome(day, timeSlot, timeSlot + 1, it, student.name).takeIf { teacherCalendar[day]?.contains(timeSlot + 1) ?: false } },
                            mapOfTroubles[student.name]?.let { Requirement.Troublesome(day, timeSlot - 1, timeSlot, student.name, it).takeIf { teacherCalendar[day]?.contains(timeSlot - 1) ?: false } },
                        )
                    }
                }
            }
        }

        // create requirements
        for (student in studentInfo) {
            for (i in 1..student.times) {
                requirements += Requirement.StudentScheduled(student.name, i)
            }
        }

        for ((day, calendar) in teacherCalendar) {
            for (timeSlot in calendar) {
                optionalRequirements += Requirement.SlotFilled(day, timeSlot)
                optionalRequirements += Requirement.LoudInstruments(day, timeSlot, timeSlot + 1)

                for (pair in mapOfTroubles) {
                    optionalRequirements += Requirement.Troublesome(day, timeSlot, timeSlot + 1, pair.key, pair.value)
                }
            }
        }

        for (instrument in studentInfo.map { it.instrument }.distinct()) {
            for (day in teacherCalendar.keys) {
                optionalRequirements += Requirement.InstrumentOnDay(instrument, day)
            }
        }

        return MrsKnuth2Solver(requirements, actions, optionalRequirements)
    }

    data class StudentInfo(
        val name: String,
        val instrument: String,
        val times: Int,
        val calendar: Map<Int, List<Int>>
    )

    fun parseCalendar(input: List<String>): Map<Int, List<Int>> {
        val days = mapOf("M" to 0, "Tu" to 1, "W" to 2, "Th" to 3, "F" to 4)
        val schedule = mutableMapOf<Int, MutableList<Int>>()
        var day = ""
        for (data in input) {
            data.toIntOrNull()?.let { schedule[days[day]]?.add(it) } ?: run { day = data; schedule[days[day]!!] = mutableListOf() }
        }
        return schedule.filter { it.value.isNotEmpty() }
    }
}

fun solveMrsKnuth3() {
    val teacherSchedule = MrsKnuth3.parseCalendar(readln().split(" "))
    val studentInfos = (0..<readln().toInt()).map {
        val d = readln().split(" ")
        val (name, instrument, repeats) = d.take(3)
        MrsKnuth3.StudentInfo(name, instrument, repeats.toInt(), MrsKnuth3.parseCalendar(d.drop(3)))
    }

    val troublesomes = (0..<readln().toInt()).map { readln().split(" ") }.map { it[0] to it[1] }
    MrsKnuth3.createSolver(teacherSchedule, studentInfos, troublesomes).solve()
}
