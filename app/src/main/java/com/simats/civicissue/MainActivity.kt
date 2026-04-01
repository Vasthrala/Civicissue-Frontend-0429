package com.simats.civicissue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.civicissue.ui.theme.CivicIssueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CivicIssueTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(onNavigate = {
                            SafeNavigator.navigate(navController, "role_selection") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }
                    composable("role_selection") {
                        RoleSelectionScreen(onRoleSelected = { role ->
                            SafeNavigator.navigate(navController, "login/$role")
                        })
                    }
                    composable("login/{role}") { backStackEntry ->
                        val role = backStackEntry.arguments?.getString("role") ?: "Citizen"
                        LoginScreen(
                            role = role,
                            onBack = { navController.popBackStack() },
                            onSignUp = { SafeNavigator.navigate(navController, "signup") },
                            onForgotPassword = { SafeNavigator.navigate(navController, "reset_password/$role") },
                            onLoginSuccess = { userRole ->
                                when (userRole) {
                                    "admin" -> SafeNavigator.navigate(navController, "admin_dashboard") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    "officer" -> SafeNavigator.navigate(navController, "officer_dashboard") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    else -> SafeNavigator.navigate(navController, "citizen_dashboard") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                    composable("signup") {
                        SignUpScreen(
                            onBack = { navController.popBackStack() },
                            onVerifyAccount = { SafeNavigator.navigate(navController, "verify_account") },
                            onLogin = { navController.popBackStack() }
                        )
                    }
                    composable("verify_account") {
                        VerifyAccountScreen(
                            onBack = { navController.popBackStack() },
                            onVerify = { 
                                SafeNavigator.navigate(navController, "account_created") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("account_created") {
                        AccountCreatedScreen(
                            onProceedToLogin = {
                                SafeNavigator.navigate(navController, "role_selection") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("admin_dashboard") {
                        AdminDashboardScreen(
                            onNotifications = { SafeNavigator.navigate(navController, "notifications") },
                            onAssignedClick = { SafeNavigator.navigate(navController, "assigned_issues") },
                            onInProgressClick = { SafeNavigator.navigate(navController, "in_progress_issues") },
                            onCompletedClick = { SafeNavigator.navigate(navController, "completed_issues") },
                            onSettingsClick = { SafeNavigator.navigate(navController, "settings") },
                            onLogoutClick = { SafeNavigator.navigate(navController, "logout") },
                            onStatusClick = { SafeNavigator.navigate(navController, "status_tracking") },
                            onReportsClick = { SafeNavigator.navigate(navController, "all_reports") },
                            onProfileClick = { SafeNavigator.navigate(navController, "admin_profile") },
                            onHistoryClick = { SafeNavigator.navigate(navController, "issue_history") },
                            onProfessionalDashboardClick = { SafeNavigator.navigate(navController, "modern_admin_dashboard") },
                            onTaskClick = { complaintId ->
                                SafeNavigator.navigate(navController, "complaint_detail/$complaintId")
                            },
                            onAIChatClick = { SafeNavigator.navigate(navController, "admin_ai_chatbot") },
                            onAnalyticsClick = { SafeNavigator.navigate(navController, "analytics") }
                        )
                    }
                    composable("admin_ai_chatbot") {
                        AdminAIChatbotScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("issue_history") {
                        IssueHistoryScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("modern_admin_dashboard") {
                        ModernAdminDashboardScreen(
                            onComplaintClick = { complaintId ->
                                SafeNavigator.navigate(navController, "complaint_detail/$complaintId")
                            },
                            onBack = { navController.popBackStack() },
                            onReportsClick = { SafeNavigator.navigate(navController, "all_reports") },
                            onProfileClick = { SafeNavigator.navigate(navController, "admin_profile") }
                        )
                    }
                    composable("citizen_dashboard") {
                        CitizenDashboardScreen(
                            onReportIssue = { SafeNavigator.navigate(navController, "report_issue") },
                            onViewMyIssues = { SafeNavigator.navigate(navController, "citizen_issues") },
                            onActiveIssuesClick = { SafeNavigator.navigate(navController, "active_issues") },
                            onResolvedIssuesClick = { SafeNavigator.navigate(navController, "resolved_issues") },
                            onNotificationsClick = { SafeNavigator.navigate(navController, "citizen_notifications") },
                            onProfileClick = { SafeNavigator.navigate(navController, "citizen_profile") },
                            onLogoutClick = {
                                SafeNavigator.navigate(navController, "logout")
                            },
                            onAIChatClick = { SafeNavigator.navigate(navController, "ai_chatbot") },
                            onComplaintClick = { complaintId ->
                                SafeNavigator.navigate(navController, "complaint_detail/$complaintId")
                            }
                        )
                    }
                    composable("report_issue") {
                        ReportIssueScreen(
                            onBack = {
                                SafeNavigator.navigate(navController, "citizen_dashboard") {
                                    popUpTo("citizen_dashboard") { inclusive = true }
                                }
                            },
                            onViewComplaints = {
                                SafeNavigator.navigate(navController, "citizen_issues") {
                                    popUpTo("citizen_dashboard") { inclusive = false }
                                }
                            },
                            onProfileClick = { SafeNavigator.navigate(navController, "citizen_profile") }
                        )
                    }
                    composable("citizen_notifications") {
                        CitizenNotificationScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("ai_chatbot") {
                        AIChatbotScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("citizen_issues") {
                        CitizenIssuesScreen(
                            onBack = { navController.popBackStack() },
                            onHomeClick = {
                                SafeNavigator.navigate(navController, "citizen_dashboard") {
                                    popUpTo("citizen_dashboard") { inclusive = true }
                                }
                            },
                            onReportClick = { SafeNavigator.navigate(navController, "report_issue") },
                            onProfileClick = { SafeNavigator.navigate(navController, "citizen_profile") }
                        )
                    }
                    composable("active_issues") {
                        ActiveIssuesScreen(
                            onBack = { navController.popBackStack() },
                            onHomeClick = { SafeNavigator.navigate(navController, "citizen_dashboard") },
                            onReportClick = { SafeNavigator.navigate(navController, "report_issue") },
                            onIssuesClick = { SafeNavigator.navigate(navController, "citizen_issues") },
                            onProfileClick = { SafeNavigator.navigate(navController, "citizen_profile") }
                        )
                    }
                    composable("resolved_issues") {
                        ResolvedIssuesScreen(
                            onBack = { navController.popBackStack() },
                            onHomeClick = { SafeNavigator.navigate(navController, "citizen_dashboard") },
                            onReportClick = { SafeNavigator.navigate(navController, "report_issue") },
                            onIssuesClick = { SafeNavigator.navigate(navController, "citizen_issues") },
                            onProfileClick = { SafeNavigator.navigate(navController, "citizen_profile") }
                        )
                    }
                    composable("citizen_profile") {
                        CitizenProfileScreen(
                            onBack = { navController.popBackStack() },
                            onHomeClick = { SafeNavigator.navigate(navController, "citizen_dashboard") },
                            onReportClick = { SafeNavigator.navigate(navController, "report_issue") },
                            onIssuesClick = { SafeNavigator.navigate(navController, "citizen_issues") },
                            onEditProfile = { SafeNavigator.navigate(navController, "edit_profile") },
                            onChangePassword = { SafeNavigator.navigate(navController, "citizen_change_password") },
                            onLogoutClick = {
                                TokenManager.clear()
                                SafeNavigator.navigate(navController, "role_selection") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("edit_profile") {
                        EditProfileScreen(
                            onBack = { navController.popBackStack() },
                            onSave = { navController.popBackStack() }
                        )
                    }
                    composable("citizen_change_password") {
                        ChangePasswordScreen(
                            onBack = { navController.popBackStack() },
                            onUpdatePassword = {
                                SafeNavigator.navigate(navController, "password_updated/Citizen") {
                                    popUpTo("citizen_profile") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("complaint_detail/{complaintId}") { backStackEntry ->
                        val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
                        ComplaintDetailScreen(
                            complaintId = complaintId,
                            onBack = { navController.popBackStack() },
                            onAssignOfficer = { 
                                SafeNavigator.navigate(navController, "assign_officer/$complaintId") 
                            },
                            onUpdateStatus = { status -> /* update status */ },
                            onResolveClick = { id ->
                                SafeNavigator.navigate(navController, "admin_resolve_issue/$id")
                            },
                            onReviewClick = { id ->
                                SafeNavigator.navigate(navController, "admin_review/$id")
                            }
                        )
                    }

                    composable("assign_officer/{complaintId}") { backStackEntry ->
                        val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
                        AssignOfficerScreen(
                            complaintId = complaintId,
                            onBack = { navController.popBackStack() },
                            onAssignComplete = { officer ->
                                // Logic for assignment completion
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("admin_resolve_issue/{complaintId}") { backStackEntry ->
                        val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
                        AdminResolveIssueScreen(
                            complaintId = complaintId,
                            onBack = { navController.popBackStack() },
                            onResolveSuccess = {
                                SafeNavigator.navigate(navController, "admin_dashboard") {
                                    popUpTo("admin_dashboard") { inclusive = true }
                                }
                            }
                        )
                    }

                    // ===== OFFICER ROUTES =====
                    composable("officer_dashboard") {
                        OfficerDashboardScreen(
                            onNotificationsClick = { SafeNavigator.navigate(navController, "officer_notifications") },
                            onProfileClick = { SafeNavigator.navigate(navController, "officer_profile") },
                            onComplaintClick = { complaintId ->
                                SafeNavigator.navigate(navController, "officer_complaint_detail/$complaintId")
                            },
                            onLogoutClick = { SafeNavigator.navigate(navController, "logout") }
                        )
                    }

                    composable("officer_complaint_detail/{complaintId}") { backStackEntry ->
                        val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
                        OfficerComplaintDetailScreen(
                            complaintId = complaintId,
                            onBack = { navController.popBackStack() },
                            onPostUpdate = { SafeNavigator.navigate(navController, "officer_post_update/$complaintId") },
                            onComplete = { SafeNavigator.navigate(navController, "officer_complete/$complaintId") }
                        )
                    }

                    composable("officer_post_update/{complaintId}") { backStackEntry ->
                        val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
                        OfficerPostUpdateScreen(
                            complaintId = complaintId,
                            onBack = { navController.popBackStack() },
                            onSuccess = { navController.popBackStack() }
                        )
                    }

                    composable("officer_complete/{complaintId}") { backStackEntry ->
                        val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
                        OfficerCompleteScreen(
                            complaintId = complaintId,
                            onBack = { navController.popBackStack() },
                            onSuccess = {
                                SafeNavigator.navigate(navController, "officer_dashboard") {
                                    popUpTo("officer_dashboard") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("officer_notifications") {
                        OfficerNotificationScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("officer_profile") {
                        OfficerProfileScreen(
                            onBack = { navController.popBackStack() },
                            onEditProfile = { SafeNavigator.navigate(navController, "edit_profile") },
                            onChangePassword = { SafeNavigator.navigate(navController, "officer_change_password") },
                            onLogoutClick = {
                                TokenManager.clear()
                                SafeNavigator.navigate(navController, "role_selection") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("officer_change_password") {
                        ChangePasswordScreen(
                            onBack = { navController.popBackStack() },
                            onUpdatePassword = {
                                SafeNavigator.navigate(navController, "password_updated/Officer") {
                                    popUpTo("officer_profile") { inclusive = true }
                                }
                            }
                        )
                    }

                    // ===== ADMIN REVIEW ROUTE =====
                    composable("admin_review/{complaintId}") { backStackEntry ->
                        val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
                        AdminReviewScreen(
                            complaintId = complaintId,
                            onBack = { navController.popBackStack() },
                            onReviewComplete = {
                                SafeNavigator.navigate(navController, "admin_dashboard") {
                                    popUpTo("admin_dashboard") { inclusive = true }
                                }
                            }
                        )
                    }

                    // ===== ADMIN OFFICER MANAGEMENT ROUTES =====
                    composable("manage_officers") {
                        ManageOfficersScreen(
                            onBack = { navController.popBackStack() },
                            onOfficerClick = { officerId ->
                                SafeNavigator.navigate(navController, "admin_officer_detail/$officerId")
                            },
                            onCreateOfficer = { SafeNavigator.navigate(navController, "create_officer") }
                        )
                    }

                    composable("admin_officer_detail/{officerId}") { backStackEntry ->
                        val officerId = backStackEntry.arguments?.getString("officerId") ?: ""
                        AdminOfficerDetailScreen(
                            officerId = officerId,
                            onBack = { navController.popBackStack() },
                            onComplaintClick = { complaintId ->
                                SafeNavigator.navigate(navController, "complaint_detail/$complaintId")
                            }
                        )
                    }

                    composable("create_officer") {
                        CreateOfficerScreen(
                            onBack = { navController.popBackStack() },
                            onSuccess = { navController.popBackStack() }
                        )
                    }

                    composable("notifications") {
                        NotificationScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("assigned_issues") {
                        AssignedIssuesScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("in_progress_issues") {
                        InProgressIssuesScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("completed_issues") {
                        CompletedIssuesScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onLogout = { SafeNavigator.navigate(navController, "logout") },
                            onManageCategories = { SafeNavigator.navigate(navController, "manage_categories") },
                            onManageDepartments = { SafeNavigator.navigate(navController, "manage_departments") },
                            onManageOfficers = { SafeNavigator.navigate(navController, "manage_officers") },
                            onSystemLogs = { SafeNavigator.navigate(navController, "system_logs") }
                        )
                    }
                    composable("manage_categories") {
                        ManageCategoriesScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("manage_departments") {
                        ManageDepartmentsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("system_logs") {
                        SystemLogsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("all_reports") {
                        AllReportsScreen(
                            onBack = { navController.popBackStack() },
                            onComplaintClick = { complaintId ->
                                SafeNavigator.navigate(navController, "complaint_detail/$complaintId")
                            }
                        )
                    }
                    composable("status_tracking") {
                        StatusScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("analytics") {
                        AnalyticsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("admin_profile") {
                        AdminProfileScreen(
                            onBack = { navController.popBackStack() },
                            onChangePassword = { SafeNavigator.navigate(navController, "change_password") },
                            onLogoutClick = {
                                TokenManager.clear()
                                SafeNavigator.navigate(navController, "role_selection") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("change_password") {
                        ChangePasswordScreen(
                            onBack = { navController.popBackStack() },
                            onUpdatePassword = {
                                SafeNavigator.navigate(navController, "password_updated/Admin") {
                                    popUpTo("admin_profile") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("logout") {
                        LogoutScreen(
                            onConfirm = {
                                TokenManager.clear()
                                SafeNavigator.navigate(navController, "role_selection") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }
                    composable("reset_password/{role}") { backStackEntry ->
                        val role = backStackEntry.arguments?.getString("role") ?: "Citizen"
                        ResetPasswordScreen(
                            role = role,
                            onBack = { navController.popBackStack() },
                            onSendOTP = { SafeNavigator.navigate(navController, "verify_otp/$role") }
                        )
                    }
                    composable("verify_otp/{role}") { backStackEntry ->
                        val role = backStackEntry.arguments?.getString("role") ?: "Citizen"
                        VerifyOTPScreen(
                            role = role,
                            onBack = { navController.popBackStack() },
                            onContinue = { SafeNavigator.navigate(navController, "create_new_password/$role") }
                        )
                    }
                    composable("create_new_password/{role}") { backStackEntry ->
                        val role = backStackEntry.arguments?.getString("role") ?: "Citizen"
                        CreateNewPasswordScreen(
                            role = role,
                            onBack = { navController.popBackStack() },
                            onUpdatePassword = { SafeNavigator.navigate(navController, "password_updated/$role") }
                        )
                    }
                    composable("password_updated/{role}") { backStackEntry ->
                        val role = backStackEntry.arguments?.getString("role") ?: "Citizen"
                        PasswordUpdatedScreen(
                            role = role,
                            onBackToLogin = {
                                SafeNavigator.navigate(navController, "login/$role") {
                                    popUpTo("login/$role") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}