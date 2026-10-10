package com.example.ui.screens.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.NoteEntity
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.MidnightDarkSurface
import com.example.ui.theme.SkySecondary

data class CuratedResource(
    val title: String,
    val category: String, // Hackathons, Courses, Tools, Open Source, Startup & Grants, Communities
    val provider: String,
    val url: String,
    val verificationStatus: String, // VERIFIED, INFERRED, PROPOSED, UNKNOWN
    val summary: String,
    val whyItMatters: String,
    val estimatedTimeOrCost: String
)

@Composable
fun ExploreScreen(
    onSaveToNotes: (NoteEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var savedResourceTitles by remember { mutableStateOf(setOf<String>()) }

    val categories = listOf("All", "Hackathons", "Courses", "Tools", "Startup & Grants", "Communities", "Datasets & APIs")

    val allResources = remember {
        listOf(
            CuratedResource(
                title = "Smart India Hackathon (SIH)",
                category = "Hackathons",
                provider = "Ministry of Education & AICTE",
                url = "https://sih.gov.in",
                verificationStatus = "VERIFIED",
                summary = "World's largest open-innovation student hackathon tackling nationwide ministry & industry problem statements.",
                whyItMatters = "Direct exposure to government stakeholders, cash prizes, and incubation opportunities.",
                estimatedTimeOrCost = "Free participation / 36-hour grand finale"
            ),
            CuratedResource(
                title = "Devfolio Hackathon Season",
                category = "Hackathons",
                provider = "Devfolio Community",
                url = "https://devfolio.co/hackathons",
                verificationStatus = "VERIFIED",
                summary = "Continuous premier web3, AI, and developer hackathon directory with verified judging criteria and mentor support.",
                whyItMatters = "Ideal platform for testing MVPs, winning sponsor tracks, and networking with hiring tech leads.",
                estimatedTimeOrCost = "Free registration"
            ),
            CuratedResource(
                title = "Fast.ai: Practical Deep Learning for Coders",
                category = "Courses",
                provider = "fast.ai (Jeremy Howard)",
                url = "https://course.fast.ai",
                verificationStatus = "VERIFIED",
                summary = "Top-down pragmatic deep learning curriculum emphasizing real model deployment before mathematical proofs.",
                whyItMatters = "Cuts theoretical overwhelm and enables shipping functional AI prototypes in days.",
                estimatedTimeOrCost = "Free / 8-12 weeks"
            ),
            CuratedResource(
                title = "Y Combinator Startup School",
                category = "Startup & Grants",
                provider = "Y Combinator",
                url = "https://startupschool.org",
                verificationStatus = "VERIFIED",
                summary = "Comprehensive online curriculum covering customer validation, talking to users, pricing, and scaling early MVPs.",
                whyItMatters = "Enforces the principle of ₹0 customer validation prior to deploying capital.",
                estimatedTimeOrCost = "Free / Self-paced"
            ),
            CuratedResource(
                title = "Hugging Face Models & Datasets Hub",
                category = "Tools",
                provider = "Hugging Face",
                url = "https://huggingface.co",
                verificationStatus = "VERIFIED",
                summary = "The central open-source machine learning hub hosting over 500k foundation models, datasets, and spaces.",
                whyItMatters = "Essential for selecting pretrained embeddings, vision models, and lightweight LLMs for offline/on-device hackathon builds.",
                estimatedTimeOrCost = "Free open-source tier"
            ),
            CuratedResource(
                title = "Kaggle Open Datasets",
                category = "Datasets & APIs",
                provider = "Kaggle",
                url = "https://kaggle.com/datasets",
                verificationStatus = "VERIFIED",
                summary = "Curated machine learning datasets spanning agriculture, healthcare, e-commerce, and regional economics.",
                whyItMatters = "Provides domain data to back up hackathon pitches with real-world empirical validation.",
                estimatedTimeOrCost = "Free access"
            ),
            CuratedResource(
                title = "Google for Startups Accelerator: India",
                category = "Startup & Grants",
                provider = "Google",
                url = "https://startup.google.com/programs/accelerator/india",
                verificationStatus = "VERIFIED",
                summary = "3-month equity-free mentorship program for high-potential early-stage tech founders.",
                whyItMatters = "Access to Cloud credits, AI architects, and institutional investor showcases.",
                estimatedTimeOrCost = "Equity-free cohort application"
            ),
            CuratedResource(
                title = "Papers with Code Trending SOTA",
                category = "Tools",
                provider = "Meta AI & Community",
                url = "https://paperswithcode.com",
                verificationStatus = "VERIFIED",
                summary = "Tracks state-of-the-art AI benchmarks connected directly to GitHub repositories.",
                whyItMatters = "Prevents reinventing wheels; verifies whether novel architecture has existing reference code.",
                estimatedTimeOrCost = "Free"
            )
        )
    }

    val filteredResources = allResources.filter { res ->
        (selectedCategory == "All" || res.category == selectedCategory) &&
        (searchQuery.isBlank() || res.title.contains(searchQuery, ignoreCase = true) || res.summary.contains(searchQuery, ignoreCase = true) || res.provider.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("explore_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Resource Discovery Engine",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Curated hackathons, grants, courses & tools with verification status",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search hackathons, courses, grants, tools...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("explore_search_input")
        )

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberGoldPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = AmberGoldPrimary
                    )
                )
            }
        }

        // Resource List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredResources) { resource ->
                val isSaved = savedResourceTitles.contains(resource.title)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = resource.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Provider: ${resource.provider} • ${resource.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkySecondary
                                )
                            }

                            // Verification Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldTertiary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, EmeraldTertiary.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = EmeraldTertiary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = resource.verificationStatus,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = EmeraldTertiary
                                    )
                                }
                            }
                        }

                        Text(
                            text = resource.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Why It Matters Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "WHY THIS MATTERS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                                    color = AmberGoldPrimary
                                )
                                Text(
                                    text = resource.whyItMatters,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Commitment: ${resource.estimatedTimeOrCost}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Bottom Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = resource.url,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    if (!isSaved) {
                                        onSaveToNotes(
                                            NoteEntity(
                                                title = "Resource: ${resource.title}",
                                                content = "Provider: ${resource.provider}\nCategory: ${resource.category}\nURL: ${resource.url}\n\nSummary:\n${resource.summary}\n\nWhy it matters:\n${resource.whyItMatters}\n\nVerification: ${resource.verificationStatus}",
                                                tags = "resource, ${resource.category.lowercase()}"
                                            )
                                        )
                                        savedResourceTitles = savedResourceTitles + resource.title
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSaved) EmeraldTertiary.copy(alpha = 0.2f) else AmberGoldPrimary,
                                    contentColor = if (isSaved) EmeraldTertiary else Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSaved) "Saved to Notes" else "Save Resource", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
