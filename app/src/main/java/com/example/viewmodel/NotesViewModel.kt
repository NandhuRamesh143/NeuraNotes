package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.model.Content
import com.example.model.GenerateContentRequest
import com.example.model.GenerationConfig
import com.example.model.Note
import com.example.model.Part
import com.example.network.RetrofitClient
import com.example.repository.NoteRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel : ViewModel() {
    private val repository = NoteRepository()
    private val okHttpClient = OkHttpClient()

    val notes: StateFlow<List<Note>> = repository.getNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    private val _isSummarizing = MutableStateFlow<String?>(null)
    val isSummarizing: StateFlow<String?> = _isSummarizing

    fun saveNote(note: Note, onComplete: () -> Unit) {
        viewModelScope.launch {
            val updatedNote = note.copy(updatedAt = Timestamp.now())
            val noteId = repository.saveNote(updatedNote)
            
            // Generate summary in the background
            generateSummary(noteId, updatedNote.content)
            onComplete()
        }
    }
    
    private fun generateSummary(noteId: String, content: String) {
        viewModelScope.launch {
            _isSummarizing.value = noteId
            try {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = "Provide a detailed and comprehensive summary of the following note. Extract all key action items, important dates, and decisions made. Make sure the summary captures the full context of the meeting.\n\n$content")
                            )
                        )
                    ),
                    systemInstruction = Content(
                        parts = listOf(Part(text = "You are an AI assistant that summarizes notes for the Neuracet AI club. Return ONLY the summary, no pleasantries."))
                    ),
                    generationConfig = GenerationConfig(temperature = 0.7f)
                )
                val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                val summaryText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (summaryText != null) {
                    repository.updateNoteSummary(noteId, summaryText)
                }
            } catch (e: Exception) {
                // In a production app, handle the error or show a Toast
                e.printStackTrace()
            } finally {
                if (_isSummarizing.value == noteId) {
                    _isSummarizing.value = null
                }
            }
        }
    }
    
    fun sendToDiscord(noteId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val note = notes.value.find { it.id == noteId }
                if (note == null) {
                    onError("Note not found locally")
                    return@launch
                }

                val displayName = FirebaseAuth.getInstance().currentUser?.displayName ?: "A Neuracet Member"

                val json = JSONObject().apply {
                    put("title", note.title)
                    put("summary", note.summary)
                    put("content", note.content)
                    put("displayName", displayName)
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://neuracet-discord-bot.vercel.app/send-to-discord")
                    .post(requestBody)
                    .build()

                withContext(Dispatchers.IO) {
                    val response = okHttpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        throw Exception("Server returned ${response.code}")
                    }
                }
                
                // Optional: Update Firestore to mark as sent if needed, but not strictly required for this demo
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to send to Discord")
            }
        }
    }
}
