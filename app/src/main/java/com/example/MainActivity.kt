package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NotesListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val notesViewModel: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NeuracetNotesApp(authViewModel, notesViewModel)
                }
            }
        }
    }
}

@Composable
fun NeuracetNotesApp(authViewModel: AuthViewModel, notesViewModel: NotesViewModel) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val navController = rememberNavController()

    if (isLoggedIn) {
        NavHost(navController = navController, startDestination = "notesList") {
            composable("notesList") {
                NotesListScreen(
                    notesViewModel = notesViewModel,
                    authViewModel = authViewModel,
                    onNoteClick = { noteId ->
                        if (noteId != null) {
                            navController.navigate("noteEditor/$noteId")
                        } else {
                            navController.navigate("noteEditor/new")
                        }
                    }
                )
            }
            composable(
                route = "noteEditor/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("noteId")
                val noteId = if (id == "new") null else id
                NoteEditorScreen(
                    notesViewModel = notesViewModel,
                    noteId = noteId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    } else {
        LoginScreen(authViewModel = authViewModel)
    }
}
