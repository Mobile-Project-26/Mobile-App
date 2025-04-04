package com.example.mobile_app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class JournalPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JournalScreen(this)
        }
    }
}

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "JournalDatabase.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "journal"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOTE = "note"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
                $COLUMN_NOTE TEXT
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertNote(note: String) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(COLUMN_NOTE, note)
            }
            db.insert(TABLE_NAME, null, values)
        }
    }

    fun getAllNotes(): List<String> {
        val notes = mutableListOf<String>()
        readableDatabase.use { db ->
            val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_NOTE), null, null, null, null, null)
            cursor.use {
                while (it.moveToNext()) {
                    notes.add(it.getString(0))
                }
            }
        }
        return notes
    }
}

@Composable
fun JournalScreen(context: Context) {
    val dbHelper = remember { DatabaseHelper(context) }
    var newNote by remember { mutableStateOf("") }
    var previousNotes by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(Unit) {
        previousNotes = dbHelper.getAllNotes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4D6A4E))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What are you thinking about today?",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        OutlinedTextField(
            value = newNote,
            onValueChange = { newNote = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Write a new note...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.LightGray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Button(
            onClick = {
                dbHelper.insertNote(newNote)
                newNote = ""
                previousNotes = dbHelper.getAllNotes()  // Refresh notes
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Save Note")
        }

        Text(
            text = "Previous notes",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        previousNotes.forEach { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.elevatedCardElevation()
            ) {
                Text(
                    text = note,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
