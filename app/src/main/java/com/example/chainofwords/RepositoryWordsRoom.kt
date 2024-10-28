package com.example.chainofwords

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow


// В этом файле описывается БД Room

// В этом классе по сути описывается вид таблицы. анатацией @Entity(tableName = "words") об этом
// сообщается руму. tableName = "words" - дает название таблице.
//@ColumnInfo(name = "word") определяет имя столбцов в таблице. по умолчанию, столбец именнуется по
// названию переменной, но можно и принудительно назвать name = "word".
// @PrimaryKey(autoGenerate = true) мы вынесли из конструктора, чтобы его каждый раз не передавать.
// autoGenerate = true будет автоматически генерировать ID строки.
// каждый экземпляр этого класса является строкой таблицы БД!
@Entity(tableName = "words")
data class Word(
    @ColumnInfo(name = "word")
    val word: String
) {@PrimaryKey(autoGenerate = true)
var uid: Int = 0 }


// Этот интерфейс хронит и определяет методы взаимодействия с БД
@Dao
interface WordsDao {
    // addNewWord
    @Insert
    suspend fun insertAll(vararg words: Word)


    // wordExist
    @Query("SELECT exists (select 1 from words Where word = :word)")
    suspend fun wordExist(word: String): Boolean


    // getSizeWords
    @Query("SELECT count (*) FROM words")
    suspend fun getCount(): Int


    // getWordByIndex
    @Query("SELECT word FROM words order by uid LIMIT 1 offset :index")
    suspend fun getWordByIndex(index: Int): String

    // clearListChainOfWords
    @Query("delete from words")
    suspend fun clear()

}





//Делаю новую таблицу для рекордов
@Entity(tableName = "records")
data class Record(
    @ColumnInfo(name = "record")
    val record: Int

) {@PrimaryKey(autoGenerate = true)
var uid: Int = 0 }


// Этот интерфейс хронит и определяет методы взаимодействия с БД
@Dao
interface RecordsDao {
    // addNewWord
    @Insert
    suspend fun insertAll(vararg record: Record) // изначально было (vararg records: Record)

    @Query("SELECT record < :sizeWords FROM records order by uid desc limit 1 ")
    suspend fun checkRecord(sizeWords: Int): Boolean?

    // getWordByIndex
    @Query("SELECT record FROM records order by uid desc limit 1") //SELECT record FROM records order by uid desc limit 1
    fun getLastRecord(): Flow<Int?>
}


//Делаю новую таблицу для сохранения состояния
@Entity(tableName = "appModes")
data class AppModes(
    @ColumnInfo(name = "counterViewModel")
    val counterViewModel: Int,

    @ColumnInfo(name = "mutableModeFlow")
    val mutableModeFlow: String,

    @ColumnInfo(name = "editText")
    val editText: String,

    @ColumnInfo(name = "counterEnteredWords")
    val counterEnteredWords: Int,


    @ColumnInfo(name = "numberAddingWords")
    val numberAddingWords: Int,

    @ColumnInfo(name = "mutableModeFlowFromModel")
    val mutableModeFlowFromModel: String,

    @ColumnInfo(name = "counterForCheckWord")
    val counterForCheckWord: Int,

) {@PrimaryKey(autoGenerate = true)
var uid: Int = 0 }


@Dao
interface AppModesDao {
//    функция должна вносить в раблицу первую строку
    @Insert
    suspend fun insertAll(vararg appModes: AppModes)

//    функция должна возвращать значения во всех колонках
//    @Query("SELECT * from appModes Where uid = 1")
    @Query("SELECT * FROM appModes ORDER BY uid DESC LIMIT 1")
    fun getAppMode() :List<AppModes?>

//    Функция должна обновлять значения в единственной строке
    @Query("UPDATE appModes SET counterViewModel = :counterViewModel, mutableModeFlow = :mutableModeFlow, " +
            "editText = :editText, counterEnteredWords = :counterEnteredWords," +
            "numberAddingWords = :numberAddingWords, mutableModeFlowFromModel = :mutableModeFlowFromModel, " +
            "counterForCheckWord = :counterForCheckWord Where uid=1")
    suspend fun upDateMode(counterViewModel: Int,
                           mutableModeFlow: String,
                           editText: String,
                           counterEnteredWords: Int,
                           numberAddingWords: Int,
                           mutableModeFlowFromModel: String,
                           counterForCheckWord: Int)

    @Query("SELECT exists (SELECT * FROM appModes ORDER BY uid DESC LIMIT 1)")
    fun saveExist(): Boolean
}










// Этот класс является основной точкой тоступа приложения к БД. как бы собирает в одну кучу таблицу
// БД и методы взаимодействия с ней.
// entities = [Word::class] - здесь информация о таблице,
// abstract fun wordsDao(): WordsDao - здесь информация о метадах.
// Экземпляр этого класса особым образом создается только там где есть контекст, т.е. в активити
@Database(entities = [Word::class, Record::class, AppModes::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordsDao(): WordsDao
    abstract fun recordsDao(): RecordsDao
    abstract fun appModesDao(): AppModesDao
}

// Этот класс позволяет взаимодействовать с базой данных
class RepositoryWordsRoom(private val roomDatabase: AppDatabase): RepositoryWords {

    private val wordsDao = roomDatabase.wordsDao()
    private val recordsDao = roomDatabase.recordsDao()
    private val appModesDao = roomDatabase.appModesDao()

    override suspend fun addNewWord(newWord: String) {
        wordsDao.insertAll(Word(newWord))
    }

    override suspend fun wordExist(newWord: String): Boolean {
        return wordsDao.wordExist(newWord)
    }

    override suspend fun getSizeWords(): Int {
        return wordsDao.getCount()
    }

    override suspend fun getWordByIndex(index: Int): String {
        return wordsDao.getWordByIndex(index)
    }

    override suspend fun clearListChainOfWords() {
        wordsDao.clear()
    }

    override suspend fun checkRecord(sizeWords: Int): Boolean? {
        return recordsDao.checkRecord(sizeWords)
    }


    override suspend fun addRecord(record: Int) {
        recordsDao.insertAll(Record(record.toInt())) // record: Record
    }

    override fun getLastRecord(): Flow<Int?> {
        return recordsDao.getLastRecord()
    }


// Функция записывает первые значения в первую и единственную строку в табдице.
    override suspend fun modesInsertAll(counterViewModel: Int,
                                        mutableModeFlow: String,
                                        editText: String,
                                        counterEnteredWords: Int,
                                        numberAddingWords: Int,
                                        mutableModeFlowFromModel: String,
                                        counterForCheckWord: Int) {
        appModesDao.insertAll(AppModes(counterViewModel,
            mutableModeFlow,
            editText,
            counterEnteredWords,
            numberAddingWords,
            mutableModeFlowFromModel,
            counterForCheckWord))
    }


    //    функция должна возвращать значения во всех колонках
    override  fun getAppModes(): List<AppModes?> {
        return appModesDao.getAppMode()
    }

    //    Функция должна обновлять значения в единственной строке
    override suspend fun upDateMode(counterViewModel: Int, mutableModeFlow: String, editText: String, counterEnteredWords: Int, numberAddingWords: Int, mutableModeFlowFromModel: String, counterForCheckWord: Int){
        appModesDao.upDateMode(counterViewModel,
            mutableModeFlow,
            editText,
            counterEnteredWords,
            numberAddingWords,
            mutableModeFlowFromModel,
            counterForCheckWord)
    }

    override fun saveExist(): Boolean {
        return appModesDao.saveExist()
    }

}