package examples

import DLXSolver
import examples.MrsKnuth2.createSolver
import examples.MrsKnuth2.parseCalendar

private object MrsKnuth2 {
    sealed interface Requirement {
        data class SlotFilled(val day: String, val hour: Int) : Requirement
        data class StudentScheduled(val name: String) : Requirement
        data class InstrumentOnDay(val instrument: String, val day: String) : Requirement
        data class LoudInstruments(val day: String, val hour: Int, val hour2: Int) : Requirement
        data class Troublesome(val day: String, val hour: Int, val hour2: Int, val name1: String, val name2: String) :
            Requirement
    }

    data class Action(val studentName: String, val instrument: String, val day: String, val timeSlot: Int)

    class MrsKnuth2Solver(
        requirements: List<Requirement>,
        actions: Map<Action, List<Requirement>>,
        optionalRequirements: List<Requirement>,
    ) : DLXSolver<Requirement, Action>(requirements, actions, optionalRequirements) {

        override fun processSolution(solution: List<Action>): Boolean {
            for (time in listOf(-1, 8, 9, 10, 11, 0, 1, 2, 3, 4)) {
                if (time == -1) {
                    println("       Monday        Tuesday       Wednesday       Thursday        Friday"); continue
                }
                if (time == 0) {
                    println("       LUNCH          LUNCH          LUNCH          LUNCH          LUNCH"); continue
                }
                print("$time".padStart(2) + " ")
                println(listOf("M", "Tu", "W", "Th", "F").joinToString(" ") { day ->
                    val text = solution.firstOrNull { it.day == day && it.timeSlot == time }
                        ?.let { "${it.studentName}/${it.instrument}" } ?: "--------------"
                    val spaceStart = (14 - text.length) / 2
                    val spaceEnd = 14 - text.length - spaceStart
                    "${" ".repeat(spaceStart)}$text${" ".repeat(spaceEnd)}"
                }.trimEnd())
            }
            return false
        }
    }

    fun createSolver(
        teacherCalendar: Map<String, List<Int>>,
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
                    actions[Action(student.name, student.instrument, day, timeSlot)] = listOfNotNull(
                        Requirement.StudentScheduled(student.name),
                        Requirement.SlotFilled(day, timeSlot),
                        Requirement.InstrumentOnDay(student.instrument, day),
                        Requirement.LoudInstruments(day, timeSlot, timeSlot + 1)
                            .takeIf { student.instrument in listOf("Trumpet", "Drums", "Trombone") && teacherCalendar[day]?.contains(timeSlot + 1) ?: false },
                        Requirement.LoudInstruments(day, timeSlot - 1, timeSlot)
                            .takeIf { student.instrument in listOf("Trumpet", "Drums", "Trombone") && teacherCalendar[day]?.contains(timeSlot - 1) ?: false },
                        mapOfTroubles[student.name]?.let { Requirement.Troublesome(
                            day,
                            timeSlot,
                            timeSlot + 1,
                            it,
                            student.name
                        ).takeIf { teacherCalendar[day]?.contains(timeSlot + 1) ?: false } },
                        mapOfTroubles[student.name]?.let { Requirement.Troublesome(
                            day,
                            timeSlot - 1,
                            timeSlot,
                            student.name,
                            it
                        ).takeIf { teacherCalendar[day]?.contains(timeSlot - 1) ?: false } },
                    )
                }
            }
        }

        // create requirements
        for (student in studentInfo) {
            requirements += Requirement.StudentScheduled(student.name)
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

    data class StudentInfo(val name: String, val instrument: String, val calendar: Map<String, List<Int>>)

    fun parseCalendar(input: List<String>): Map<String, List<Int>> {
        val schedule = mutableMapOf<String, MutableList<Int>>()
        var day = ""
        for (data in input) {
            data.toIntOrNull()?.let { schedule[day]?.add(it) } ?: run { day = data; schedule[day] = mutableListOf() }
        }
        return schedule.filter { it.value.isNotEmpty() }
    }
}

fun solveMrsKnuth2() {
    val teacherSchedule = parseCalendar(readln().split(" "))
    val studentInfos = (0..<readln().toInt()).map {
        val d = readln().split(" ")
        val (name, instrument) = d.take(2)
        MrsKnuth2.StudentInfo(name, instrument, parseCalendar(d.drop(2)))
    }

    val troublesomes = (0..<readln().toInt()).map { readln().split(" ") }.map { it[0] to it[1] }

    createSolver(teacherSchedule, studentInfos, troublesomes).solve()
}