package com.simats.civicissue

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface CivicApiService {
    @GET("api/complaints")
    suspend fun getComplaints(): List<Complaint>

    @POST("api/complaints")
    suspend fun createComplaint(@Body complaint: Complaint): Complaint

    @GET("api/reports/recent")
    suspend fun getRecentReports(): List<CitizenReportDto>
}
