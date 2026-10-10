package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null
) {
    companion object {
        fun fromFirebaseUser(firebaseUser: FirebaseUser?): AuthUser? {
            return firebaseUser?.let {
                AuthUser(
                    uid = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoUrl?.toString()
                )
            }
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    data class Loading(val message: String = "Please wait...") : AuthUiState()
    data class Authenticated(val user: AuthUser) : AuthUiState()
    data class Unauthenticated(val errorMessage: String? = null) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "AuthViewModel"

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _isFirebaseAvailable = MutableStateFlow(false)
    val isFirebaseAvailable: StateFlow<Boolean> = _isFirebaseAvailable.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        initializeFirebaseAuth()
    }

    private fun initializeFirebaseAuth() {
        try {
            val app = getApplication<Application>()
            val hasApps = FirebaseApp.getApps(app).isNotEmpty()
            if (hasApps) {
                val auth = FirebaseAuth.getInstance()
                firebaseAuth = auth
                _isFirebaseAvailable.value = true

                val initialUser = auth.currentUser
                if (initialUser != null) {
                    val authUser = AuthUser.fromFirebaseUser(initialUser)
                    _currentUser.value = authUser
                    _uiState.value = AuthUiState.Authenticated(authUser!!)
                } else {
                    _uiState.value = AuthUiState.Unauthenticated()
                }

                val listener = FirebaseAuth.AuthStateListener { fa ->
                    val user = fa.currentUser
                    val authUser = AuthUser.fromFirebaseUser(user)
                    _currentUser.value = authUser
                    if (authUser != null) {
                        _uiState.value = AuthUiState.Authenticated(authUser)
                    } else if (_uiState.value !is AuthUiState.Loading) {
                        _uiState.value = AuthUiState.Unauthenticated()
                    }
                }
                authStateListener = listener
                auth.addAuthStateListener(listener)
            } else {
                Log.w(tag, "FirebaseApp is not yet initialized. Waiting for google-services configuration.")
                _isFirebaseAvailable.value = false
                _uiState.value = AuthUiState.Unauthenticated(
                    errorMessage = "Firebase is not yet provisioned. Local-only mode active."
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Error initializing FirebaseAuth: ${e.message}", e)
            _isFirebaseAvailable.value = false
            _uiState.value = AuthUiState.Unauthenticated(
                errorMessage = "Authentication unavailable: ${e.localizedMessage ?: "Unknown initialization error"}"
            )
        }
    }

    /**
     * Google Sign-In via Jetpack Credential Manager using GoogleIdTokenCredential.
     */
    fun signInWithGoogle(
        activityContext: Context,
        serverClientId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val auth = firebaseAuth
        if (auth == null || !_isFirebaseAvailable.value) {
            val errorMsg = "Firebase Authentication is not yet provisioned or configured."
            _uiState.value = AuthUiState.Unauthenticated(errorMsg)
            onError(errorMsg)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Initiating Google Sign-In...")
            try {
                val credentialManager = CredentialManager.create(activityContext)
                val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = activityContext
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val authUser = AuthUser.fromFirebaseUser(authResult.user)

                    if (authUser != null) {
                        _currentUser.value = authUser
                        _uiState.value = AuthUiState.Authenticated(authUser)
                        onSuccess()
                    } else {
                        val msg = "Sign-in completed but user profile was empty."
                        _uiState.value = AuthUiState.Unauthenticated(msg)
                        onError(msg)
                    }
                } else {
                    val msg = "Unexpected credential type received from Credential Manager."
                    _uiState.value = AuthUiState.Unauthenticated(msg)
                    onError(msg)
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(tag, "Google Sign-In was cancelled by the user")
                _uiState.value = AuthUiState.Unauthenticated()
            } catch (e: GetCredentialException) {
                val msg = "Credential Manager error: ${e.message}"
                Log.e(tag, msg, e)
                _uiState.value = AuthUiState.Unauthenticated(msg)
                onError(msg)
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Google Sign-In failed"
                Log.e(tag, "Failed to sign in with Google credential: $msg", e)
                _uiState.value = AuthUiState.Unauthenticated(msg)
                onError(msg)
            }
        }
    }

    /**
     * Real email & password sign-in via FirebaseAuth.
     */
    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val auth = firebaseAuth
        if (auth == null || !_isFirebaseAvailable.value) {
            val errorMsg = "Firebase Authentication is not yet configured."
            _uiState.value = AuthUiState.Unauthenticated(errorMsg)
            onError(errorMsg)
            return
        }

        if (email.isBlank() || pass.isBlank()) {
            val errorMsg = "Email and password cannot be empty."
            _uiState.value = AuthUiState.Unauthenticated(errorMsg)
            onError(errorMsg)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Signing in with email...")
            try {
                val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
                val authUser = AuthUser.fromFirebaseUser(result.user)
                if (authUser != null) {
                    _currentUser.value = authUser
                    _uiState.value = AuthUiState.Authenticated(authUser)
                    onSuccess()
                } else {
                    val msg = "Authentication succeeded but user profile was null."
                    _uiState.value = AuthUiState.Unauthenticated(msg)
                    onError(msg)
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Email sign-in failed."
                Log.e(tag, "Sign-in error: $msg", e)
                _uiState.value = AuthUiState.Unauthenticated(msg)
                onError(msg)
            }
        }
    }

    /**
     * Real account registration via FirebaseAuth.
     */
    fun signUpWithEmail(
        email: String,
        pass: String,
        displayName: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val auth = firebaseAuth
        if (auth == null || !_isFirebaseAvailable.value) {
            val errorMsg = "Firebase Authentication is not yet configured."
            _uiState.value = AuthUiState.Unauthenticated(errorMsg)
            onError(errorMsg)
            return
        }

        if (email.isBlank() || pass.length < 6) {
            val errorMsg = "Please provide a valid email and a password of at least 6 characters."
            _uiState.value = AuthUiState.Unauthenticated(errorMsg)
            onError(errorMsg)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Creating your account...")
            try {
                val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val user = result.user

                if (user != null && displayName.isNotBlank()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName.trim())
                        .build()
                    user.updateProfile(profileUpdates).await()
                }

                val authUser = AuthUser.fromFirebaseUser(auth.currentUser ?: user)
                if (authUser != null) {
                    _currentUser.value = authUser
                    _uiState.value = AuthUiState.Authenticated(authUser)
                    onSuccess()
                } else {
                    val msg = "Account created successfully."
                    _uiState.value = AuthUiState.Unauthenticated(msg)
                    onSuccess()
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Failed to create account."
                Log.e(tag, "Sign-up error: $msg", e)
                _uiState.value = AuthUiState.Unauthenticated(msg)
                onError(msg)
            }
        }
    }

    /**
     * Password reset email via FirebaseAuth.
     */
    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val auth = firebaseAuth
        if (auth == null || !_isFirebaseAvailable.value) {
            val errorMsg = "Firebase Authentication is not yet configured."
            _uiState.value = AuthUiState.Unauthenticated(errorMsg)
            onError(errorMsg)
            return
        }

        if (email.isBlank()) {
            val errorMsg = "Please enter your email address to receive password reset instructions."
            _uiState.value = AuthUiState.Unauthenticated(errorMsg)
            onError(errorMsg)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Sending password reset email...")
            try {
                auth.sendPasswordResetEmail(email.trim()).await()
                _uiState.value = AuthUiState.Unauthenticated("Password reset email sent to $email.")
                onSuccess()
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Failed to send password reset email."
                Log.e(tag, "Password reset error: $msg", e)
                _uiState.value = AuthUiState.Unauthenticated(msg)
                onError(msg)
            }
        }
    }

    /**
     * Sign out current user.
     */
    fun signOut(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                firebaseAuth?.signOut()
                _currentUser.value = null
                _uiState.value = AuthUiState.Unauthenticated()
                onComplete()
            } catch (e: Exception) {
                Log.e(tag, "Error signing out: ${e.message}", e)
                _uiState.value = AuthUiState.Unauthenticated(e.localizedMessage)
                onComplete()
            }
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Unauthenticated) {
            _uiState.value = AuthUiState.Unauthenticated(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        authStateListener?.let { listener ->
            firebaseAuth?.removeAuthStateListener(listener)
        }
    }
}
