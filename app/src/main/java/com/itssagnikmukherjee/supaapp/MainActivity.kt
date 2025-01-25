@file:OptIn(SupabaseInternal::class, SupabaseExperimental::class)

package com.itssagnikmukherjee.supaapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.itssagnikmukherjee.supaapp.ui.theme.SupaAppTheme
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.selectAsFlow
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.flow.Flow

class MainActivity : ComponentActivity() {

    val supabase = createSupabaseClient(
        supabaseUrl = "",
        supabaseKey = ""
    ) {
        install(Postgrest)
        install(Realtime)
        httpConfig {
            this.install(WebSockets)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SupaAppTheme {
                CafeList()
            }
        }
    }

    @Composable
    fun CafeList(modifier: Modifier = Modifier) {
        var cafeList by remember { mutableStateOf<List<Cafe>>(emptyList()) }
        LaunchedEffect(key1 = true) {
            try {
                getCafeListRealtime().collect {
                    cafeList = it
                }
            } catch (e: Exception) {
                Log.e("CafeListRealtime", "Error fetching data: ${e.message}")
            }
        }

        LazyColumn {
            items(cafeList.size) {
                Text(text = cafeList[it].name)
                Text(text = cafeList[it].description)
                AsyncImage(cafeList[it].image,"")
            }
        }
    }

    fun getCafeListRealtime(): Flow<List<Cafe>> {
        return supabase.from(table = "cafe").selectAsFlow(Cafe::id)
    }

    data class Cafe(
        val id: Int,
        val name: String,
        val description: String,
        val image: String
    )
}

