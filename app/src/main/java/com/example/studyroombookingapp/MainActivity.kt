package com.example.studyroombookingapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyroombookingapp.ui.theme.StudyRoomBookingAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val roomName: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            StudyRoomBookingAppTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val auth = FirebaseAuth.getInstance()
    var currentScreen by remember { mutableStateOf(if (auth.currentUser != null) "list" else "login") }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = { currentScreen = "list" },
            onGoToRegister = { currentScreen = "register" }
        )
        "register" -> RegisterScreen(
            onRegisterSuccess = { currentScreen = "list" },
            onGoToLogin = { currentScreen = "login" }
        )
        "list" -> ReservationListScreen(
            onLogout = {
                auth.signOut()
                currentScreen = "login"
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onGoToRegister: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Study Room Booking", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Sign in to continue", fontSize = 16.sp, color = Color.Gray)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it; error = "" },
                label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; error = "" },
                label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, visualTransformation = PasswordVisualTransformation()
            )

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        error = "Please fill in all fields"
                        return@Button
                    }
                    loading = true
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { loading = false; onLoginSuccess() }
                        .addOnFailureListener { loading = false; error = it.message ?: "Login failed" }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !loading
            ) { Text(if (loading) "Signing in..." else "Sign In") }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onGoToRegister) {
                Text("Don't have an account? Register")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onGoToLogin: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it; error = "" },
                label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; error = "" },
                label = { Text("Password (min 6 characters)") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it; error = "" },
                label = { Text("Confirm Password") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, visualTransformation = PasswordVisualTransformation()
            )

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        error = "Please fill in all fields"; return@Button
                    }
                    if (password.length < 6) { error = "Password must be at least 6 characters"; return@Button }
                    if (password != confirmPassword) { error = "Passwords do not match"; return@Button }
                    loading = true
                    auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { loading = false; onRegisterSuccess() }
                        .addOnFailureListener { loading = false; error = it.message ?: "Registration failed" }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !loading
            ) { Text(if (loading) "Creating account..." else "Register") }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onGoToLogin) { Text("Already have an account? Sign In") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationListScreen(onLogout: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var reservations by remember { mutableStateOf(listOf<Reservation>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReservation by remember { mutableStateOf<Reservation?>(null) }
    var deleteReservation by remember { mutableStateOf<Reservation?>(null) }
    var filterStatus by remember { mutableStateOf("All") }
    val context = LocalContext.current

    DisposableEffect(userId) {
        val listener: ListenerRegistration = db.collection("reservations")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                reservations = snapshot?.documents?.map { doc ->
                    Reservation(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        roomName = doc.getString("roomName") ?: "",
                        date = doc.getString("date") ?: "",
                        startTime = doc.getString("startTime") ?: "",
                        endTime = doc.getString("endTime") ?: "",
                        status = doc.getString("status") ?: "active",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } ?: emptyList()
            }
        onDispose { listener.remove() }
    }

    val filtered = when (filterStatus) {
        "Active" -> reservations.filter { it.status == "active" }
        "Cancelled" -> reservations.filter { it.status == "cancelled" }
        else -> reservations
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reservations") },
                actions = {
                    TextButton(onClick = onLogout) { Text("Logout", color = MaterialTheme.colorScheme.error) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add reservation")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Active", "Cancelled").forEach { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reservations yet.\nTap + to add one.", textAlign = TextAlign.Center, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { res ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(res.roomName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(
                                        res.status.uppercase(),
                                        color = if (res.status == "active") Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold, fontSize = 12.sp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Date: ${res.date}", fontSize = 14.sp, color = Color.Gray)
                                Text("Time: ${res.startTime} - ${res.endTime}", fontSize = 14.sp, color = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { editingReservation = res }) {
                                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { deleteReservation = res }) {
                                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                    TextButton(onClick = {
                                        val newStatus = if (res.status == "active") "cancelled" else "active"
                                        db.collection("reservations").document(res.id).update("status", newStatus)
                                    }) {
                                        Text(if (res.status == "active") "Cancel" else "Reactivate", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ReservationDialog(
            title = "New Reservation",
            onDismiss = { showAddDialog = false },
            onSave = { room, date, start, end ->
                val data = hashMapOf(
                    "userId" to userId, "roomName" to room, "date" to date,
                    "startTime" to start, "endTime" to end, "status" to "active",
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("reservations").add(data)
                    .addOnSuccessListener { Toast.makeText(context, "Reservation added!", Toast.LENGTH_SHORT).show() }
                showAddDialog = false
            }
        )
    }

    editingReservation?.let { res ->
        ReservationDialog(
            title = "Edit Reservation",
            initialRoom = res.roomName, initialDate = res.date,
            initialStart = res.startTime, initialEnd = res.endTime,
            onDismiss = { editingReservation = null },
            onSave = { room, date, start, end ->
                db.collection("reservations").document(res.id).update(
                    mapOf("roomName" to room, "date" to date, "startTime" to start, "endTime" to end)
                )
                editingReservation = null
            }
        )
    }

    deleteReservation?.let { res ->
        AlertDialog(
            onDismissRequest = { deleteReservation = null },
            title = { Text("Delete Reservation") },
            text = { Text("Are you sure you want to delete the reservation for ${res.roomName}?") },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("reservations").document(res.id).delete()
                    deleteReservation = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteReservation = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ReservationDialog(
    title: String, initialRoom: String = "", initialDate: String = "",
    initialStart: String = "", initialEnd: String = "",
    onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit
) {
    var room by remember { mutableStateOf(initialRoom) }
    var date by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf(initialStart) }
    var endTime by remember { mutableStateOf(initialEnd) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start Time (HH:MM)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End Time (HH:MM)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (error.isNotEmpty()) { Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (room.isBlank() || date.isBlank() || startTime.isBlank() || endTime.isBlank()) {
                    error = "All fields are required"; return@TextButton
                }
                if (!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    error = "Date must be YYYY-MM-DD"; return@TextButton
                }
                if (!startTime.matches(Regex("\\d{2}:\\d{2}")) || !endTime.matches(Regex("\\d{2}:\\d{2}"))) {
                    error = "Time must be HH:MM"; return@TextButton
                }
                if (endTime <= startTime) { error = "End time must be after start time"; return@TextButton }
                onSave(room, date, startTime, endTime)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}