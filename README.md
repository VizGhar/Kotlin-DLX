
# Dancing links

Still work in progress - currently discovering the DLX / AlgorithmX realm

Kotlin implementation of dancing links based on:  
https://www.codingame.com/playgrounds/156252/algorithm-x/introduction

Pure implementation of DLX Solver can be found in [src/dlx_solver.kt](src/dlx_solver.kt)


## DLXSolver
Constructor of `DLXSolver` class accepts following required attributes:

1. `requirements` - `List` of requirements of any type
2. `actions` - `Map` in which key is `Action` and value is list of satisfied `requirements` (or `optionalRequirements`)
3. `optionalRequirements` - `List` similar to `requirements` but these `requirements` are optional and by default empty.

In order to use `DLXSolver` you have to extend it, instantiate it and call its `solve()` function. `solve` function by default finds every solution. Since it may take some time to compute all solutions, you have to override either one of these functions:

1. `processSolution(solution)` function - gives you solutions one by one in `solution` attribute. You may return `true` if solution is processed in order to break the execution of `solve()` function
2. `onFinished(solutions)` function - gives you all `solutions` ever found

`solution` is always `List` of `Actions` that leads to solution of given problem.

## Recommended usage

If you are not interested in reading and/or you know Kotlin, you can simply Check [Paving with bricks implementation](src/paving/paving_with_bricks.kt) to understand the approach I'm about to describe.

### Specify requirements and actions classes
Best option to specify requirements is to create `sealed interface` and extend it using `data classes`, and same goes for actions.

```kt
sealed interface Requirement {  
    data class CellCovered(val x: Int, val y: Int) : Requirement
    data class Requirement2(val value: String) : Requirement
}

sealed interface Action {
    data class PlaceTile(val x: Int, val y: Int, val shape: Shape) : Action
}

```

However if you are using only one type of actions (or requirements) you don't need `sealed interface`. Your Action class might look like this:

```kt
data class PlaceTile(val x: Int, val y: Int, val shape: Shape)
```

### Extend DLXSolver

Now you need to extend DLXSolver and implement either `onFinished` or `processSolution` function.  If you need to know how many solutions there are for given problem, you can simply use this implementation:

```kt
class MySolver(  
    requirements: List<Requirement>,  
    actions: Map<PlaceTile, List<Requirement>>,
    optionalRequirements: List<Requirement>
) : DLXSolver<Requirement, PlaceTile>(requirements, actions, optionalRequirements) {  
    override fun onFinished(solutions: List<List<PlaceTile>>) {  
        println(solutions.size)  
    }  
}
```


### Feed your solver

Instantiate your solver with necessary attributes, like this:

```kt
fun createSolver(...) : MySolver {  
    val requirements = mutableListOf<Requirement>()  
    val actions = mutableMapOf<PlaceTile, List<Requirement>>()  

    // create requirements
    requirements += Requirement.CellCovered(x, y)
    optionalRequirements += Requirement.Requirement2("")

	actions[PlaceTile(x, y, shape)] = listOf(
	    Requirement.CellCovered(...),
	    Requirement.CellCovered(...), 
	    ...
	)
	
    
    return MySolver(requirements, actions, optionalRequirements)  
}
```

### Solve

```kt
fun main() {  
    createSolver(...).solve()  
}
```

## TODOs

- history
- isValidSolution
- more solved puzzles

## Solved puzzles

1. [Mrs. Knuth 1](https://www.codingame.com/contribute/view/94231c8a12567007bde24553f6a9e3de55981)
2. [Sudoku 25x25](https://www.codingame.com/training/expert/25x25-sudoku)
3. [Constrained Latin Squares](https://www.codingame.com/training/medium/constrained-latin-squares)
4. [Literary Alfabet Soupe](https://www.codingame.com/training/medium/literary-alfabet-soupe)
5. [Shikaku Solver](https://www.codingame.com/training/medium/shikaku-solver)
6. [Dominoes solver](https://www.codingame.com/training/hard/dominoes-solver)
7. [Paving with bricks](https://www.codingame.com/training/medium/paving-with-bricks)
8. [Mrs. Knuth 2](https://www.codingame.com/contribute/view/950238e7e8f40105ccd0fd6237bf60c4d25b3)
9. [n Queens](https://www.codingame.com/training/hard/n-queens)
10. [Finish 8 queens](https://www.codingame.com/training/medium/finish-the-eight-queens)
11. [Einstein's Riddle Solver](https://www.codingame.com/training/hard/einsteins-riddle-solver)
12. [Three little piggies](https://www.codingame.com/training/hard/three-little-piggies)
13. [Who Dunnit](https://www.codingame.com/training/hard/who-dunnit)
14. [Mrs. Knuth 3](https://www.codingame.com/contribute/view/959460130d2f9792d933f75838edb639a6dae)
15. 