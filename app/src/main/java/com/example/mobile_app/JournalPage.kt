package com.example.mobile_app
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager

class JournalPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JournalScreen()
        }
    }
}

@Composable
fun JournalScreen() {
    var newNote by remember { mutableStateOf("") }
    var previousNotes by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        previousNotes = fetchNotesFromDatabase()
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
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.White,
                cursorColor = Color.White
            )
        )

        Button(
            onClick = {
                saveNoteToDatabase(newNote)
                newNote = ""
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

fun fetchNotesFromDatabase(): List<String> {
    return try {
        val connection = getDatabaseConnection()
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT note FROM journal")
        val notes = mutableListOf<String>()
        while (resultSet.next()) {
            notes.add(resultSet.getString("note"))
        }
        notes
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun saveNoteToDatabase(note: String) {
    try {
        val connection = getDatabaseConnection()
        val statement = connection.prepareStatement("INSERT INTO journal (note) VALUES (?)")
        statement.setString(1, note)
        statement.executeUpdate()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getDatabaseConnection(): Connection {
    val url = "url/ourdbname"
    val user = "ourusername"
    val password = "ourpassword"
    return DriverManager.getConnection(url, user, password)
}
