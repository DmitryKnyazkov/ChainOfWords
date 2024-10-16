package com.example.chainofwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class WordsViewModel : ViewModel() {



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


    private val mutableModeFlow = MutableStateFlow(listModes[0])
    val modeFlow = mutableModeFlow.asStateFlow()

    private val mutableButtonFlow = MutableStateFlow(false)
    val buttonFlow = mutableButtonFlow.asStateFlow()

    private val mutableSizeWordsFlow = MutableStateFlow(0)
    val sizeWordsFlow = mutableSizeWordsFlow.asStateFlow()

    private val mutableRecordFlow = MutableStateFlow("0")
    val recordFlow = mutableRecordFlow.asStateFlow()

    private val scope = viewModelScope

    //При изменении свойства запускаем изменение mode
    private var counterEnteredWords by Delegates.observable(0) {
            _, _, _ -> scope.launch { analysisAndCreateFlowForView() }
    }





    //Два источника одного значения - оставим одно
    //private var modelMode: Model.Modes = Model.Modes.AddNewWord


    //Название функции отражает то, что должен знать View,
    // а не внутренние алгоритмы ViewModel
    suspend fun onShow() {
        //Лучше запустить от имени viewModel
        scope.launch {
            model.modeFlowFromModel.collect {
                counterEnteredWords = -1
//                analysisAndCreateFlowForView()
            }
        }
    }

// Эта функция посылает сигнал View на основании выполнения функции mapMode()
    private suspend fun analysisAndCreateFlowForView() {
        mutableModeFlow.emit(
            //При такой реализации проще понять, все ли варианты перебраны
            //так же толоко один вызов emit
            mapMode(model.modeFlowFromModel.value,
                counterEnteredWords)
        )

//        if (nowInFlowModeFlowFromModel == Model.Modes.CheckWord && nowInFlowModeCounterForCheck_WordFromModel>0){}
    }

// Эта функция сопоставляет текущее сосотояние и счетчик введеных слов и формирует сигнал в виде
//    определенной строки. она может быть частью функции analysisAndCreateFlowForView()
    private fun mapMode(
        modes: Model.Modes,
        counterEnteredWords: Int
    ) = when (modes) {
        Model.Modes.AddNewWord ->
            when (counterEnteredWords) {
                0 -> "questionStart"
                1 -> "inputSecondWord"
                else -> "new_word"
            }

        Model.Modes.CheckWord ->
            when (counterEnteredWords) {
                0 -> "answerStart"
                else -> "next_word"
            }

        Model.Modes.GameOver -> "game_over"
    }

// транспортирует из View в Model задачу проверить введенное слово
    fun checkWord(word: String) {
        counterEnteredWords++
        scope.launch {
            model.checkWord(word)
        }
    }

//    собирает информацию о количестве слов в цепочке и передает ее View.
    private fun emitCountWords() = scope.launch { mutableSizeWordsFlow.emit(model.getSizeWords()) }

    fun emitRecord() = scope.launch { mutableRecordFlow.emit(model.getRecord()) }

//    добавляет через Модел слова в цепочку. и если слово уже есть в цепочке имитет ошибку во View
    fun appNewWord(newWord: String) {
        //Не надо прибавлять заранее
        //counterEnteredWords++
        scope.launch {
            val resultRecording = model.addNewWord(newWord)

            if (!resultRecording) {
//              Если придет фолс, то имитем "error" ошибку и counterEnteredWords откати назад
//                counterEnteredWords--
                mutableModeFlow.emit("error")
            } else {
                counterEnteredWords++
                emitCountWords()
            }

        }
    }

//    эта функция вызывается когда в режиме ОШИБКА нажалась кнопка. она сообщает View что надо
//    перейти в режим ввода слова
    fun errorToinputSecondWord() {
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[1])
        }
    }

//    вызывается когда в конце игры. имитит View состояние начала игры. обнуляет в Моделе нужные значения.
    fun gameOver() {
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[0])
            counterEnteredWords = -1
            //Это явно костыль
            //modelMode = Model.Modes.AddNewWord
            model.restart()
            model.checkRecord()
            emitCountWords()
            emitRecord()
        }
    }

//    управляет кнопкой во вью.
    fun openButton(editText: String, mode: String) {

        if ((editText.contains(""".*[~?!"№;%:*()+=<> @#$}{'^&+-0123456789].*""".toRegex()) ||
            editText.toString() == "")
            && mode in arrayOf(
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
    }
}