package com.simats.civicissue

object TokenManager {
    private var token: String? = null
    private var currentUser: UserProfile? = null

    // Temporary storage for password reset flow
    var pendingEmail: String? = null
    var pendingOtp: String? = null

    @Synchronized
    fun saveToken(t: String?) { 
        token = t 
    }

    @Synchronized
    fun getToken(): String? = token

    @Synchronized
    fun saveUser(u: UserProfile?) { 
        currentUser = u 
    }

    @Synchronized
    fun getUser(): UserProfile? = currentUser

    @Synchronized
    fun clear() { 
        token = null
        currentUser = null
        pendingEmail = null
        pendingOtp = null
    }

    @Synchronized
    fun isLoggedIn(): Boolean = !token.isNullOrBlank()

    @Synchronized
    fun getUserRole(): String {
        return try {
            currentUser?.role ?: "citizen"
        } catch (e: Exception) {
            "citizen"
        }
    }
    
    @Synchronized
    fun isSessionValid(): Boolean {
        return isLoggedIn() && currentUser != null
    }
}
