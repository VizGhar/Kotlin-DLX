// TODO implement history
// TODO implement isValidSolution

abstract class DLXSolver<in R : Any, A : Any>(
    requirements: List<R>,
    actions: Map<A, List<R>>,
    optionalRequirements: List<R> = emptyList()
) {

    private val solution: MutableList<A> = mutableListOf()
    private val allSolutions: MutableList<List<A>> = mutableListOf()
    private val matrixRoot = DLXCell(Unit)

    private val optionalRequirements = optionalRequirements.toSet()
    private val columnHeaders = (requirements + optionalRequirements).associateWith { requirement -> DLXCell(requirement) }
    private val rowHeaders = actions.keys.associateWith { action -> DLXCell(action) }

    init {
        for (columnHeader in columnHeaders) {
            matrixRoot.attachHorizontal(columnHeader.value)
        }

        for ((action, satisfiedRequirements) in actions) {
            val previousCell = rowHeaders[action] ?: throw IllegalStateException()
            for (requirement in satisfiedRequirements) {
                val cHeader = columnHeaders[requirement]
                val rHeader = previousCell
                val nextCell = DLXCell("${cHeader?.content}/${rHeader.content}").apply {
                    columnHeader = columnHeaders[requirement] ?: throw IllegalStateException("Action - $action expects not specified requirement - $requirement")
                    rowHeader = previousCell
                    columnHeader.attachVertical(this)
                    columnHeader.size++
                }
                previousCell.attachHorizontal(nextCell)
            }
        }
    }

    private var breaking = false
    private var counter = 0

    fun solve() {
        counter++
        var bestColumn = matrixRoot
        var bestValue = Int.MAX_VALUE

        var n = matrixRoot.nextX

        while (n != matrixRoot) {
            if (n.content !in optionalRequirements) {
                val value = requirementSortCriteria(n)
                if (bestColumn == matrixRoot || value < bestValue) {
                    bestColumn = n
                    bestValue = value
                }
                n = n.nextX
            } else {
                n = matrixRoot
            }
        }

        if (bestColumn == matrixRoot) {
            if (processSolution(solution)) { breaking = true }
            allSolutions += solution.map { it }
        } else {
            val actions = mutableListOf<DLXCell>()
            n = bestColumn.nextY
            while (n != bestColumn) {
                actions += n
                n = n.nextY
            }

            for (node in actions.sortedBy { actionSortCriteria(it) }) {
                select(node)
                solve()
                if (breaking) {
                    checkFinish()
                    return
                }
                deselect(node)
            }
        }
        checkFinish()
    }

    private fun checkFinish() {
        counter--
        if (counter == 0) onFinished(allSolutions)
    }

    private fun select(cell: DLXCell) {
        cell.select()
        solution.add(cell.rowHeader.content as? A ?: throw IllegalStateException())
        processRowSelection(cell.rowHeader)
    }

    private fun deselect(cell: DLXCell) {
        cell.unselect()
        solution.removeLast()
        processRowDeselection(cell.rowHeader)
    }

    open fun actionSortCriteria(rowHeader: DLXCell) = 0
    open fun requirementSortCriteria(columnHeader: DLXCell) = columnHeader.size
    open fun processRowSelection(row: DLXCell) {}
    open fun processRowDeselection(row: DLXCell) {}

    open fun processSolution(solution: List<A>): Boolean = false
    open fun onFinished(solutions: List<List<A>>) {}

}

class DLXCell(val content: Any) {

    var prevX: DLXCell = this
    var nextX: DLXCell = this
    var prevY: DLXCell = this
    var nextY: DLXCell = this

    var size: Int = 0
    var columnHeader = this
    var rowHeader = this

    fun removeX() {
        prevX.nextX = nextX
        nextX.prevX = prevX
    }

    fun removeY() {
        prevY.nextY = nextY
        nextY.prevY = prevY
    }

    fun restoreX() {
        prevX.nextX = this
        nextX.prevX = this
    }

    fun restoreY() {
        prevY.nextY = this
        nextY.prevY = this
    }

    fun attachHorizontal(other: DLXCell) {
        val n = prevX
        other.prevX = n
        n.nextX = other
        prevX = other
        other.nextX = this
    }

    fun attachVertical(other: DLXCell) {
        val n = prevY
        other.prevY = n
        n.nextY = other
        prevY = other
        other.nextY = this
    }

    fun removeColumn() {
        removeX()
        var node = nextY
        while (node != this) {
            node.removeRow()
            node = node.nextY
        }
    }

    fun restoreColumn() {
        var node = prevY
        while (node != this) {
            node.restoreRow()
            node = node.prevY
        }
        restoreX()
    }

    fun removeRow() {
        var node = nextX
        while (node != this) {
            node.columnHeader.size--
            node.removeY()
            node = node.nextX
        }
    }

    fun restoreRow() {
        var node = prevX
        while(node != this) {
            node.columnHeader.size++
            node.restoreY()
            node = node.prevX
        }
    }

    fun select() {
        var node = this
        do {
            node.removeY()
            node.columnHeader.removeColumn()
            node = node.nextX
        } while (node != this)
    }

    fun unselect() {
        var node = prevX
        while(node != this) {
            node.columnHeader.restoreColumn()
            node.restoreY()
            node = node.prevX
        }
        node.columnHeader.restoreColumn()
        node.restoreY()
    }
}