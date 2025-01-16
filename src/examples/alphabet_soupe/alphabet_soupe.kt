package examples.alphabet_soupe

import DLXSolver

data class LanugageSpec(
    val inclusions: CharArray,
    val exclusions: CharArray
)

val defaultSet = CharArray(26) { 'a' + it }

val languageSpecs = mapOf(
    "Danish" to LanugageSpec(inclusions = charArrayOf('æ', 'å', 'ø'), exclusions = charArrayOf('q', 'z')),
    "English" to LanugageSpec(inclusions = charArrayOf(), exclusions = charArrayOf()),
    "Estonian" to LanugageSpec(inclusions = charArrayOf('š', 'ž', 'õ', 'ä','ö','ü'), exclusions = charArrayOf('c', 'f', 'q', 'w', 'x', 'y')),
    "Finnish" to LanugageSpec(inclusions = charArrayOf('ä', 'ö'), exclusions = charArrayOf('b', 'f', 'q', 'w', 'x')),
    "French" to LanugageSpec(inclusions = charArrayOf('ç', 'œ', 'ë', 'ï', 'ü', 'à', 'è', 'ù', 'â', 'ê', 'î', 'ô', 'û', 'é'), exclusions = charArrayOf()),
    "German" to LanugageSpec(inclusions = charArrayOf('ß', 'ä', 'ö', 'ü'), exclusions = charArrayOf()),
    "Irish" to LanugageSpec(inclusions = charArrayOf('á', 'é', 'í', 'ó', 'ú'), exclusions = charArrayOf('j', 'k', 'q', 'v', 'w', 'x', 'y', 'z')),
    "Italian" to LanugageSpec(inclusions = charArrayOf('à', 'è', 'ì', 'ò', 'ù', 'é'), exclusions = charArrayOf('j', 'k', 'w', 'x', 'y')),
    "Portuguese" to LanugageSpec(inclusions = charArrayOf('ç', 'ã', 'õ', 'à', 'â', 'ê', 'ô', 'á', 'é', 'í', 'ó', 'ú'), exclusions = charArrayOf('k', 'w')),
    "Spanish" to LanugageSpec(inclusions = charArrayOf('ñ', 'ü', 'á', 'é', 'í', 'ó', 'ú'), exclusions = charArrayOf('k', 'w')),
    "Swedish" to LanugageSpec(inclusions = charArrayOf('å', 'ä', 'ö'), exclusions = charArrayOf('q', 'w')),
    "Turkish" to LanugageSpec(inclusions = charArrayOf('ğ', 'ç', 'ş', 'İ', 'ı', 'ö', 'ü'), exclusions = charArrayOf('q', 'w', 'x')),
    "Welsh" to LanugageSpec(inclusions = charArrayOf('ŵ', 'ŷ', 'â', 'ê', 'î', 'ô', 'û'), exclusions = charArrayOf('j', 'k', 'q', 'v', 'x', 'z')),
)

// DLX solver inputs
sealed interface Requirement {
    data class LanguageCovered(val language: String) : Requirement
    data class LineCovered(val line: Int) : Requirement
}

data class Action(val language: String, val lineId: Int)

// DLX solver implementation
class AlphabetSoupeSolver(
    requirements: List<Requirement>,
    actions: Map<Action, List<Requirement>>
) : DLXSolver<Requirement, Action>(requirements, actions) {

    override fun processSolution(solution: List<Action>): Boolean {
        for (i in 0..12) {
            println(solution.first { it.lineId == i }.language)
        }
        return true
    }
}

fun createSolver(input: List<String>) : AlphabetSoupeSolver {
    val requirements = mutableListOf<Requirement>()
    val actions = mutableMapOf<Action, List<Requirement>>()

    for (language in languageSpecs.keys) { requirements += Requirement.LanguageCovered(language) }
    for (i in input.indices) { requirements += Requirement.LineCovered(i) }

    for (i in 0 until input.size) {
        val line = input[i]
        val chars = line.lowercase().filter { it.isLetterOrDigit() }.toCharArray().distinct()
        val possibleLanguages = languageSpecs.filter { languageSpec ->
            val includes = chars.all { it in defaultSet + languageSpec.value.inclusions }
            val excludes = chars.none { it in languageSpec.value.exclusions }
            includes && excludes
        }.keys

        if (possibleLanguages.isEmpty()) {
            throw IllegalArgumentException("not recognized one of ${(chars - defaultSet).joinToString(",") {"\"$it\""}}")
        }

        for (language in possibleLanguages) {
            actions[Action(language, i)] = listOf(
                Requirement.LanguageCovered(language),
                Requirement.LineCovered(i),
            )
        }
    }

    return AlphabetSoupeSolver(requirements, actions)
}

fun main() {
    createSolver(List(13){ readln().lowercase() }).solve()
}