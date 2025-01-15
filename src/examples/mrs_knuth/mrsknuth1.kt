package examples.mrs_knuth

import DLXSolver

// DLX solver inputs
sealed interface Requirement {
    data class SlotFilled(val day: String, val hour: Int) : Requirement
    data class StudentScheduled(val name: String) : Requirement
    data class InstrumentOnDay(val instrument: String, val day: String) : Requirement
}

data class Action(val studentName: String, val instrument: String, val day: String, val timeSlot: Int)

// DLX solver implementation
class MrsKnuth1Solver(
    requirements: List<Requirement>,
    actions: Map<Action, List<Requirement>>
) : DLXSolver<Requirement, Action>(requirements, actions) {

    override fun processSolution(solution: MutableList<Action>) : Boolean {
        for (time in listOf(-1, 8, 9, 10, 11, 0, 1, 2, 3, 4)) {
            if (time == -1) { println("       Monday        Tuesday       Wednesday       Thursday        Friday"); continue }
            if (time == 0) { println("       LUNCH          LUNCH          LUNCH          LUNCH          LUNCH"); continue }
            print("$time".padStart(2) + " ")
            println(listOf("M", "Tu", "W", "Th", "F").joinToString(" ") { day ->
                val text = solution.firstOrNull { it.day == day && it.timeSlot == time}?.let {"${it.studentName}/${it.instrument}"} ?: "--------------"
                val spaceStart = (14 - text.length) / 2
                val spaceEnd = 14 - text.length - spaceStart
                "${" ".repeat(spaceStart)}$text${" ".repeat(spaceEnd)}"
            }.trimEnd())
        }
        return false
    }
}

fun createSolver(teacherCalendar: Calendar, studentInfo: List<StudentInfo>) : MrsKnuth1Solver {
    val requirements = mutableListOf<Requirement>()
    val actions = mutableMapOf<Action, List<Requirement>>()

    // create actions
    for(student in studentInfo) {
        for ((day, calendar) in student.calendar) {
            for (timeSlot in calendar) {
                actions[Action(student.name, student.instrument, day, timeSlot)] = listOf(
                    Requirement.StudentScheduled(student.name),
                    Requirement.SlotFilled(day, timeSlot),
                    Requirement.InstrumentOnDay(student.instrument, day),
                )
            }
        }
    }

    // create requirements
    for(student in studentInfo) {
        requirements += Requirement.StudentScheduled(student.name)
    }

    for ((day, calendar) in teacherCalendar) {
        for (timeSlot in calendar) {
            requirements += Requirement.SlotFilled(day, timeSlot)
        }
    }

    for (instrument in studentInfo.map { it.instrument }.distinct()) {
        for (day in teacherCalendar.keys) {
            requirements += Requirement.InstrumentOnDay(instrument, day)
        }
    }

    return MrsKnuth1Solver(requirements, actions)
}

// Data parsing

typealias Calendar = Map<String, List<Int>>

data class StudentInfo(val name: String, val instrument: String, val calendar: Calendar)

fun parseCalendar(input: List<String>): Map<String, List<Int>> {
    val schedule = mutableMapOf<String, MutableList<Int>>()
    var day = ""
    for (data in input) {
        data.toIntOrNull()?.let { schedule[day]?.add(it) } ?: run { day = data; schedule[day] = mutableListOf() }
    }
    return schedule.filter { it.value.isNotEmpty() }
}

fun main() {
    val teacherSchedule = parseCalendar(readln().split(" "))
    val studentInfos = (0 until readln().toInt()).map {
        val d = readln().split(" ")
        val (name, instrument) = d.take(2)
        StudentInfo(name, instrument, parseCalendar(d.drop(2)))
    }

    createSolver(teacherSchedule, studentInfos).solve()
}