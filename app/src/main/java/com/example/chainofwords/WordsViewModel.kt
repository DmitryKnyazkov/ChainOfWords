package com.example.chainofwords

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chainofwords.Model.Modes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class WordsViewModel : ViewModel() {


    private val scope = viewModelScope
    private val scope2 = CoroutineScope(Dispatchers.IO)

    private lateinit var model: Model

    private val listModes: List<String> = listOf(
        "questionStart", // AddNewWord Создадим новую цепочку слов.Введите слово
        "inputSecondWord", // AddNewWord Введите следующее слово в цепочку
        "answerStart", // CheckWord Цепочка слов создана.Воспроизведем ее. Введите слово
        "next_word", // CheckWord Вы ответили верно. Вводите следующее слово
        "game_over", // GameOver Вы ответили неверно. Игра закончилась.
        "new_word", // AddNewWord Вы верно воспроизвели всю цепочку слов. Увеличем цепочку. Введите новое слово
        "error" // сообщение Ошибка! Такое слово уже есть в цепочке! Введите слово заново.
    )


    private val state = object {

        var counterViewModel: Int by Delegates.observable(-1) {_,_,_ -> log()}
        val mutableModeFlow = MutableStateFlow(listModes[0])
        var editText: String by Delegates.observable("") { _, _, newValue ->
            changeButtonState(newValue)
            log()
        }



        var counterEnteredWords by Delegates.observable(-2) { _, _, newCounterEnteredWords ->

            scope.launch { analysisAndCreateFlowForView() }

            log()
        }

        fun log() {
            if (this@WordsViewModel::model.isInitialized)
            model.saveModesFromViewModel(counterViewModel, mutableModeFlow.value, editText, counterEnteredWords)
            Log.d("wordsStateViewModel", "counterViewModel: $counterViewModel, mutableModeFlow: ${mutableModeFlow.value}, editText: $editText, counterEnteredWords: $counterEnteredWords")
        }

        init {
            scope2.launch { mutableModeFlow.collect{ log()} }
        }


    }

    fun getAppModes() {
        CoroutineScope(Dispatchers.IO).launch {
            if (model.saveExist()) {
                val listFromDB = model.getAppModes()
                state.counterViewModel = listFromDB[0]?.counterViewModel ?: -2
                state.mutableModeFlow.value = listFromDB[0]?.mutableModeFlow ?: "questionStart"
                state.editText = listFromDB[0]?.editText ?: ""
                state.counterEnteredWords  = listFromDB[0]?.counterEnteredWords ?: -1
            }
            else {
                state.counterViewModel = -2
                state.mutableModeFlow.value = "questionStart"
                state.editText = ""
                state.counterEnteredWords  = -2


            }
        }

    }



//    init {
//        if (!model.saveExist()) {
//            state.counterViewModel = -2
//            state.mutableModeFlow.value = "questionStart"
//            state.editText = ""
//            state.counterEnteredWords  = -1
//        }
//        else {
//            val listFromDB = model.getAppModes()
//            state.counterViewModel = listFromDB[0]?.counterViewModel ?: -2
//            state.mutableModeFlow.value = listFromDB[0]?.mutableModeFlow ?: "questionStart"
//            state.editText = listFromDB[0]?.editText ?: ""
//            state.counterEnteredWords  = listFromDB[0]?.counterEnteredWords ?: -1
//        }
//
//    }

    val modeFlow = state.mutableModeFlow.asStateFlow()

    private val mutableButtonFlow = MutableStateFlow(false)
    val buttonFlow = mutableButtonFlow.asStateFlow()

    private val mutableSizeWordsFlow = MutableStateFlow(0)
    val sizeWordsFlow = mutableSizeWordsFlow.asStateFlow()

    private val mutableRecordFlow = MutableStateFlow<Int?>(null)
    val recordFlow = mutableRecordFlow.asStateFlow()






    //Название функции отражает то, что должен знать View,
    // а не внутренние алгоритмы ViewModel
    suspend fun onShow() {
        //Лучше запустить от имени viewModel
        scope.launch {
            model.modeFlowFromModel.collect {
                Log.d("wordsState", "get new state model")
                if (it == Modes.CheckWord){
                    if (state.mutableModeFlow.value == "new_word"){
                        state.counterEnteredWords = 0
                    }
                }


//                if (state.counterViewModel>=0)
//                    {
//                        state.counterEnteredWords = 0
//                }
//                state.counterViewModel++
                analysisAndCreateFlowForView()
            }
        }
    }

    // Эта функция посылает сигнал View на основании выполнения функции mapMode()
    private suspend fun analysisAndCreateFlowForView() {
        state.mutableModeFlow.emit(
            //При такой реализации проще понять, все ли варианты перебраны
            //так же толоко один вызов emit
            mapMode( model.modeFlowFromModel.value, state.counterEnteredWords)
        )

    }


    private fun mapMode(
        modes: Model.Modes,
        counterEnteredWords: Int
    ) = when (modes) {
        Model.Modes.AddNewWord ->
            when (counterEnteredWords) {
                -2 -> "questionStart" // AddNewWord Создадим новую цепочку слов.Введите слово
                0 -> "new_word" // AddNewWord Вы верно воспроизвели всю цепочку слов. Увеличем цепочку. Введите новое слово
                -1 -> "inputSecondWord" // AddNewWord Введите следующее слово в цепочку
                else -> "new_word" // AddNewWord Вы верно воспроизвели всю цепочку слов. Увеличем цепочку. Введите новое слово
            }

        Model.Modes.CheckWord ->
            when (counterEnteredWords) {
                0 -> "answerStart" // CheckWord Цепочка слов создана.Воспроизведем ее. Введите слово
                else -> "next_word" // CheckWord Вы ответили верно. Вводите следующее слово
            }

        Model.Modes.GameOver -> "game_over"
    }

    // транспортирует из View в Model задачу проверить введенное слово
    fun checkWord(word: String) {

        scope.launch {
            state.counterEnteredWords++
            model.checkWord(word)

        }
    }

    //    собирает информацию о количестве слов в цепочке и передает ее View.
    private fun emitCountWords() = scope.launch { mutableSizeWordsFlow.emit(model.getSizeWords()) }


    //    добавляет через Модел слова в цепочку. и если слово уже есть в цепочке имитет ошибку во View
    fun appNewWord(newWord: String) {
        //Не надо прибавлять заранее
        //counterEnteredWords++
        scope.launch {
            state.counterEnteredWords++
            val resultRecording = model.addNewWord(newWord)

            if (!resultRecording) {
//              Если придет фолс, то имитем "error" ошибку и counterEnteredWords откати назад
                state.counterEnteredWords--
                state.mutableModeFlow.emit("error")
            }
            emitCountWords()
        }
    }

    //    эта функция вызывается когда в режиме ОШИБКА нажалась кнопка. она сообщает View что надо
//    перейти в режим ввода слова
    fun errorToinputSecondWord() {
        viewModelScope.launch {
            state.mutableModeFlow.emit(listModes[1])
        }
    }

    //    вызывается когда в конце игры. имитит View состояние начала игры. обнуляет в Моделе нужные значения.
    fun gameOver() {
        viewModelScope.launch {
            if (model.checkRecord(mutableSizeWordsFlow.value.toInt())) {
                model.addRecord(mutableSizeWordsFlow.value.toInt())
            }
            state.mutableModeFlow.emit(listModes[0])
            state.counterEnteredWords = -2
            state.counterViewModel = -1

            model.restart()

            emitCountWords()
//            emitRecord()

        }
    }

    //    управляет кнопкой во вью.
    fun changeEditText(editText: String) {
        this.state.editText = editText

    }

    private fun changeButtonState(editText: String) {
        val mode = modeFlow.value

        if ((editText.contains(""".*[~?!"№;%:*()+=<> @#$}{'^&+-0123456789].*""".toRegex()) || editText.toString() == "") && mode in arrayOf(
                "questionStart", // AddNewWord Создадим новую цепочку слов.Введите слово
                "inputSecondWord", // AddNewWord Введите следующее слово в цепочку
                "answerStart", // CheckWord Цепочка слов создана.Воспроизведем ее. Введите слово
                "next_word", // CheckWord Вы ответили верно. Вводите следующее слово
                "new_word", // AddNewWord Вы верно воспроизвели всю цепочку слов. Увеличем цепочку. Введите новое слово
            )
        ) {
            viewModelScope.launch {
                mutableButtonFlow.emit(false)
            }
        } else {
            viewModelScope.launch {
                mutableButtonFlow.emit(true)
            }
        }
    }

    //    Вызывается сразу во Вью. в себе несет экземпляр базы данных приложения, который используется
//    для создания экземляра класса RepositoryWordsRoom(db). Этот класс позволяет взаимодействовать
//    с БД. экземляра класса RepositoryWordsRoom(db) используется для создания экземпляра
//    Модели во ВьюМодуле. Экземпляр модели определяется только здесь, т.к. только здесь появляется
//    val repository = RepositoryWordsRoom(db), который передается в Модель.
    fun setDB(db: AppDatabase) {
        val repository = RepositoryWordsRoom(db)
        model = Model(repository)
        viewModelScope.launch {
            model.getRecord().collect { mutableRecordFlow.emit(it) }
        } //mutableRecordFlow = model.getRecord()
    }
}