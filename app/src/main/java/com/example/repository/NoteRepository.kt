package com.example.repository

import android.util.Log
import com.example.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.tasks.await

class NoteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val notesCollection
        get() = firestore.collection("notes")

    fun getNotes(): Flow<List<Note>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        val subscription = notesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("NoteRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notes = snapshot.toObjects(Note::class.java)
                        .sortedByDescending { it.updatedAt }
                    trySend(notes)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveNote(note: Note): String {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val noteToSave = note.copy(userId = userId)
        return if (note.id.isEmpty()) {
            val docRef = notesCollection.add(noteToSave).await()
            docRef.id
        } else {
            notesCollection.document(note.id).set(noteToSave).await()
            note.id
        }
    }
    
    suspend fun updateNoteSummary(noteId: String, summary: String) {
        notesCollection.document(noteId).update("summary", summary).await()
    }
}
