package com.example.chainofwords

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

// Это скорее вспомогательный абстрактный класс, чтобы не забыть какие функции нужно реализовать
// в классе который будет управлять БД
interface RepositoryWords {
    suspend fun addNewWord(newWord: String)
    suspend fun wordExist(newWord: String): Boolean
    suspend fun getSizeWords(): Int
    suspend fun getWordByIndex(index: Int): String
    suspend fun clearListChainOfWords()


    suspend fun checkRecord(sizeWords: Int): Boolean?
    suspend fun addRecord(record: Int)
    fun getLastRecord(): Flow<Int?>


    suspend fun modesInsertAll( counterViewModel: Int,
                                mutableModeFlow: String,
                                editText: String,
                                counterEnteredWords: Int,
                                numberAddingWords: Int,
                                mutableModeFlowFromModel: String,
                                counterForCheckWord: Int)

    suspend fun upDateMode(counterViewModel: Int,
                           mutableModeFlow: String,
                           editText: String,
                           counterEnteredWords: Int,
                           numberAddingWords: Int,
                           mutableModeFlowFromModel: String,
                           counterForCheckWord: Int)

    fun getAppModes(): List<AppModes?>

    fun saveExist(): Boolean
}



// Этот класс определяет и отправляет состаяния во VM. в конструкторе private val repositoryWords
// определяет взаимодействие с какой БД или репозиторием он будет работать.
class Model(private val repositoryWords: RepositoryWords ) {

    enum class Modes{
        AddNewWord, CheckWord, GameOver
    }
    val scope = CoroutineScope(Dispatchers.IO)

    private val state = object {

        init {
            CoroutineScope(Dispatchers.IO).launch { mutableModeFlowFromModel.collect{ log()} }
        }

        // numberAddingWords контролирует количество вводимых слов. в начале игры это 2 слова,
        var numberAddingWords: Int by Delegates.observable(2) {_, _, _ -> log()}
        //  по этому потоку передается состояие. либо "режим добавления нового слова", либо "режим сравнивания слов с цепочкой"
        val mutableModeFlowFromModel = MutableStateFlow(Modes.AddNewWord)
        //   В этой переменной текущий номер слова в цепи для сравнения или количество слов даваемых в начале игры
        var counterForCheckWord: Int by Delegates.observable(0) {_, _, _ -> log()}

        fun log() {
            Log.d("wordsStateModel", "numberAddingWords: $numberAddingWords, mode: ${mutableModeFlowFromModel.value}, counterForCheckWord: $counterForCheckWord" )
        }
    }







    val modeFlowFromModel = state.mutableModeFlowFromModel.asStateFlow()

//    val repositoryRecords = RepositoryRecords()


    suspend fun addNewWord(newWord: String): Boolean {

//        Здесь происходит некая проверка, утверждение о том, во соответствующее фло отправляется
        //        именно определенные состояния. если во фло отправится что-то другое, то приложение упадет.
        assert(
            state.mutableModeFlowFromModel.value in arrayOf(
                Modes.AddNewWord,
                Modes.GameOver
            )
        )
//      Здесь проверяем есть ли такое слово в списке. если уже есть, то функция вернет false
        if (repositoryWords.wordExist(newWord)) {
            return false
        } else {
            repositoryWords.addNewWord(newWord)
//          numberAddingWords контролирует количество вводимых слов. в начале игры это 2 слова,
//          когда увеличивается цепочка, то это 1 слово. слова вводятся до того как numberAddingWords
//          станет нулем. и произойдет имит нового состояния.
            state.numberAddingWords --
            if (state.numberAddingWords == 0) {state.mutableModeFlowFromModel.emit(Modes.CheckWord)}
            return true
        }

    }

//Эта функция проверяет правильноть ответа и определяет все ли мы проверили слова из списка
//    имитит нужный сигнал для VM
    suspend fun checkWord(word: String) {

        assert(
            state.mutableModeFlowFromModel.value in arrayOf(
                Modes.CheckWord
            )
        )
//      Проверяем пришедшее слово (word) соответствует ли оно нужному слову из сохранненых
        if (word == repositoryWords.getWordByIndex(state.counterForCheckWord)) {
//          Если да то добавляем к счетчику единицу. Для того чтобы в следующей раз сравнивать
//          следующее слово по списку
            state.counterForCheckWord += 1

//          Проверяем все ли мы проверили слова из списка. Если каунтер равен колчеству слов в списке,
//          значит мы все слова перебрали и список нужно увеличивать.
            if (state.counterForCheckWord == repositoryWords.getSizeWords()) {
//              Посылаем во VM сигнал, что пора присылать новое слово для списка
                state.mutableModeFlowFromModel.emit(Modes.AddNewWord)
//              Обнуляем каунтер для последующих проверок слов начиная с первого в списке
                state.counterForCheckWord = 0
//              numberAddingWords = 1 для того чтобы функции add_new_word сообщть, что добавить нужно
//              только одно слово к списку. add_new_word в 65 строке вычтет 1 и в следующей строке
//              поймет что слова для ввода закончились
                state.numberAddingWords = 1
            }
        } else {
//          Если было введено неправильное слово, то передаем VM конец игры
            state.mutableModeFlowFromModel.emit(Modes.GameOver)
        }


    }

    suspend fun restart(){
        state.counterForCheckWord = 0
        repositoryWords.clearListChainOfWords()
        state.numberAddingWords = 2
        state.mutableModeFlowFromModel.emit(Modes.AddNewWord)
    }

    suspend fun getSizeWords(): Int {
        return repositoryWords.getSizeWords()
    }


    suspend fun checkRecord(sizeWords: Int): Boolean {
        return repositoryWords.checkRecord(sizeWords) ?: true
    }

    suspend fun addRecord(record: Int) {
        repositoryWords.addRecord(record)
    }

    fun getRecord(): Flow<Int?> {
        return repositoryWords.getLastRecord()
    }


    suspend fun modesInsertAll(counterViewModel: Int,
                               mutableModeFlow: String,
                               editText: String,
                               counterEnteredWords: Int,
                               numberAddingWords: Int,
                               mutableModeFlowFromModel: String,
                               counterForCheckWord: Int) {
        repositoryWords.modesInsertAll(counterViewModel,
            mutableModeFlow,
            editText,
            counterEnteredWords,
            numberAddingWords,
            mutableModeFlowFromModel,
            counterForCheckWord)
    }

    suspend fun upDateMode(counterViewModel: Int,
                   mutableModeFlow: String,
                   editText: String,
                   counterEnteredWords: Int,
                   numberAddingWords: Int,
                   mutableModeFlowFromModel: String,
                   counterForCheckWord: Int) {
        repositoryWords.upDateMode(counterViewModel,
            mutableModeFlow,
            editText,
            counterEnteredWords,
            numberAddingWords,
            mutableModeFlowFromModel,
            counterForCheckWord)
    }

    fun getAppModes(): List<AppModes?> {
        val listFromDB = repositoryWords.getAppModes()
        if (listFromDB.isEmpty()) {
            state.numberAddingWords =  2
            state.mutableModeFlowFromModel.value =  Modes.AddNewWord
            state.counterForCheckWord = 0
        }
        else {
            Log.d("wordsState", "${listFromDB[0]}" )
            state.numberAddingWords = listFromDB[0]?.numberAddingWords ?: 2
            state.mutableModeFlowFromModel.value = when (listFromDB[0]?.mutableModeFlowFromModel) {
                "AddNewWord" -> Modes.AddNewWord
                "CheckWord" -> Modes.CheckWord
                "GameOver"-> Modes.GameOver
                else -> {Modes.GameOver}
            }
            state.counterForCheckWord = listFromDB[0]?.counterForCheckWord ?: 0
        }

        return listFromDB
    }

    fun saveModesFromViewModel(counterViewModel: Int, mutableModeFlow: String, editText: String, counterEnteredWords: Int){
        scope.launch {
//            var listModelDB = getAppModes()
//            if (saveExist()) {
            upDateMode(counterViewModel,
                mutableModeFlow,
                editText,
                counterEnteredWords,
                state.numberAddingWords,
                state.mutableModeFlowFromModel.value.toString(),
                state.counterForCheckWord)
//            }
//            else {
//                modesInsertAll(counterViewModel,
//                    mutableModeFlow,
//                    editText,
//                    counterEnteredWords,
//                    state.numberAddingWords,
//                    state.mutableModeFlowFromModel.value.toString(),
//                    state.counterForCheckWord)
//            }
        }
    }

    fun saveExist(): Boolean{
        return repositoryWords.saveExist()
    }

}


    ////Этот класс использовался, когда цепочка слов хранилась в списке, т.е. по сути в этом классе.
//// когда появилась БД то он уже не используется.
//class RepositoryWordsList(): RepositoryWords {
//
//    private val listChainOfWords = mutableListOf<String>()
//
//    override suspend fun addNewWord(newWord: String) {
//        listChainOfWords.add(newWord)
//    }
//
//    override suspend fun wordExist(newWord: String): Boolean {
//        return newWord in listChainOfWords
//    }
//
//    override suspend fun getSizeWords(): Int {
//        return listChainOfWords.size
//    }
//
//    override suspend fun getWordByIndex(index: Int): String {
//        return listChainOfWords[index]
//    }
//
//    override suspend fun clearListChainOfWords() {
//        listChainOfWords.clear()
//    }
//}

//class RepositoryRecords() {
//    var record = 0
//}


