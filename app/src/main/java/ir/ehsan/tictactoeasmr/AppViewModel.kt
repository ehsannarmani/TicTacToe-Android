package ir.ehsan.tictactoeasmr

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel:ViewModel() {
    private val _gameLevel:MutableStateFlow<Int> = MutableStateFlow(3)
    val gameLevel = _gameLevel.asStateFlow()

    private val _gameTurn = MutableStateFlow(0)
    val gameTurn = _gameTurn.asStateFlow()

    val listOfMovements = mutableStateListOf(*initialMovements.toTypedArray())

    private val initialMovements:List<Movement> get() = MutableList(gameLevel.value*gameLevel.value){
        Movement()
    }

    fun resetGame(){
        listOfMovements.clear()
        _gameTurn.update { 0 }
        listOfMovements.addAll(initialMovements)
    }

    fun checkWin(index:Int,onWin:(Int?)->Unit){
        val fromLeft = (index%_gameLevel.value)
        val fromRight = _gameLevel.value - ((index%_gameLevel.value)+1)

        val listOfRowIndexes = mutableListOf<Int>()
        for (i in 1..fromLeft){
            val newIndex = index-i
            listOfRowIndexes.add(newIndex)
        }

        listOfRowIndexes.add(index)

        for (i in 1..fromRight){
            val newIndex = index + i
            listOfRowIndexes.add(newIndex)
        }

        val listOfRowTurns = mutableListOf<Int?>()
        listOfRowIndexes.forEach {
            listOfRowTurns.add(listOfMovements[it].turn)
        }

        if (listOfRowTurns.count{ it == null} <=0){
            if (listOfRowTurns.distinct().count() == 1 && listOfRowTurns.count() >1){
                // win
                onWin(listOfRowTurns.first())
            }
        }

        val listOfVerticalIndexes = mutableListOf(index)
        for (i in 1.._gameLevel.value){
            val newIndexTop = index - (_gameLevel.value * i)
            if (newIndexTop < 0){
                break;
            }else{
                listOfVerticalIndexes.add(newIndexTop)
            }
        }

        for (i in 1.._gameLevel.value){
            val newIndexBottom = index + (_gameLevel.value*i)
            if (newIndexBottom > listOfMovements.size-1){
                break
            }else{
                listOfVerticalIndexes.add(newIndexBottom)
            }
        }

        val listOfVerticallyTurns = mutableListOf<Int?>()

        listOfVerticalIndexes.forEach {
            listOfVerticallyTurns.add(listOfMovements[it].turn)
        }

        if (listOfVerticallyTurns.count{ it == null } <=0){
            if (listOfVerticallyTurns.distinct().count() == 1 && listOfVerticallyTurns.count() >1){
                val winTurn = listOfVerticallyTurns.first()
                onWin(winTurn)
            }
        }

        val listOfBevelIndexes = mutableListOf<Int>()
        listOfBevelIndexes.add(index)
        for (i in 1.._gameLevel.value){
            val newIndexBottom = index+((_gameLevel.value+1)*i)
            if (newIndexBottom <= listOfMovements.size-1){
                listOfBevelIndexes.add(newIndexBottom)
            }else{
                break
            }
        }

        val listOfBevelTurns = mutableListOf<Int?>()
        listOfBevelIndexes.forEach {
            listOfBevelTurns.add(listOfMovements[it].turn)
        }

        if (listOfVerticallyTurns.filterNotNull().count() >= _gameLevel.value && listOfVerticallyTurns.count { it == null } <=0){
            if (listOfBevelTurns.distinct().count() ==1 && listOfBevelTurns.count() >1){
                onWin(listOfBevelTurns.first())
            }
        }

        val listOfBevelIndexes2 = mutableListOf<Int>()
        for (i in 1.._gameLevel.value){
            listOfBevelIndexes2.add(((_gameLevel.value-1)*i))
        }
        val listOfBevelTurns2 = mutableListOf<Int?>()
        listOfBevelIndexes2.forEach {
            listOfBevelTurns2.add(listOfMovements[it].turn)
        }

        if (listOfBevelTurns2.filterNotNull().count() >= _gameLevel.value && listOfBevelTurns2.count { it == null } <=0){
            if (listOfBevelTurns2.distinct().count() == 1 && listOfBevelTurns2.count() >1){
                onWin(listOfBevelTurns2.first())
            }
        }


    }

    fun newMovement(index:Int,turn: Int? = _gameTurn.value){
        if (!listOfMovements[index].filled){
            listOfMovements[index] = listOfMovements[index].copy(
                filled = true,
                turn = turn
            )
            changeTurn()
        }
    }
    fun randomMovement(){
        viewModelScope.launch {
            delay(500)
            val index = getFreeIndex()
            if (index != null){
                newMovement(index, turn = if (_gameTurn.value == 1) 0 else 1)
            }
        }
    }
    fun getFreeIndex():Int?{
        val indexesList = listOfMovements.mapIndexed { index, movement ->
            if (!movement.filled) return@mapIndexed index else return@mapIndexed null
        }.filterNotNull()
        return if (indexesList.isEmpty()){
            null
        }else{
            indexesList.random()
        }
    }

    fun changeTurn(){
        _gameTurn.update {
            if (it == 1) 0 else 1
        }
    }

    fun changeGameLevel(newLevel:Int){
        _gameLevel.update { newLevel }
        listOfMovements.clear()
        listOfMovements.addAll(initialMovements)
    }

}


data class Movement(
    val filled:Boolean = false,
    val turn:Int? = null
)