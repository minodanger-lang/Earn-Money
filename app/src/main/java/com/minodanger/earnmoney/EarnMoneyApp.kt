package com.minodanger.earnmoney

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private enum class AppTab(val title: String, val symbol: String) {
    HOME("الرئيسية", "⌂"),
    TASKS("المهام", "✓"),
    WALLET("المحفظة", "◉"),
    ACCOUNT("حسابي", "●")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarnMoneyApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = AppTab.entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Earn Money", fontWeight = FontWeight.Bold)
                        Text(
                            "نسخة تجريبية للتطوير",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Text(tab.symbol) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (tabs[selectedTab]) {
            AppTab.HOME -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                onOpenTasks = { selectedTab = 1 },
                onOpenWallet = { selectedTab = 2 }
            )
            AppTab.TASKS -> TasksScreen(Modifier.padding(innerPadding))
            AppTab.WALLET -> WalletScreen(Modifier.padding(innerPadding))
            AppTab.ACCOUNT -> AccountScreen(Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenTasks: () -> Unit,
    onOpenWallet: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            DemoNotice()
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("رصيدك المتاح", color = MaterialTheme.colorScheme.surface)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "0.00 جنيه",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "لا توجد أرباح حقيقية مسجلة في النسخة التجريبية",
                        color = MaterialTheme.colorScheme.surface
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onOpenWallet) { Text("عرض المحفظة") }
                }
            }
        }
        item {
            Text("ابدأ من هنا", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            ActionCard("اكتشف المهام", "تعرّف على شكل قائمة المهام التجريبية", "عرض المهام", onOpenTasks)
        }
        item {
            ActionCard("راجع المحفظة", "تابع الرصيد التجريبي وحالة السحب", "فتح المحفظة", onOpenWallet)
        }
        item {
            Text(
                "لن يتم إضافة أموال أو إتاحة السحب إلا بعد ربط مزود معتمد والتحقق من إنجاز المهام واستلام مستحقات التطبيق.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TasksScreen(modifier: Modifier = Modifier) {
    val offers = remember { demoOffers }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("المهام والعروض", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("هذه أمثلة توضيحية وليست عروضًا متاحة أو مدفوعة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
        }
        items(offers) { offer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(offer.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text(offer.rewardLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(offer.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("عرض تجريبي — غير قابل للتنفيذ", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun WalletScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("المحفظة", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item { BalanceCard("الرصيد المتاح", "0.00 جنيه") }
        item { BalanceCard("الرصيد قيد المراجعة", "0.00 جنيه") }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("طلبات السحب", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("السحب غير مفعّل في هذه النسخة. سيتم تفعيله بعد اعتماد وسيلة دفع واختبار تحويل حقيقي.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                        Text("السحب غير متاح حاليًا")
                    }
                }
            }
        }
        item {
            Text("سجل العمليات سيظهر هنا بعد ربط الحسابات ومصدر المكافآت.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private const val AUTH_API_BASE_URL = "https://earn-money-git-setup-initial-foundation-earn-money3.vercel.app"
private val authExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()

@Composable
private fun AccountScreen(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { context.getSharedPreferences("earn_money_auth", android.content.Context.MODE_PRIVATE) }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(preferences.getString("email", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var signedInEmail by remember { mutableStateOf(
        if (preferences.getString("access_token", null) != null) preferences.getString("email", null) else null
    ) }

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("حسابي", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (signedInEmail != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تم تسجيل الدخول", fontWeight = FontWeight.Bold)
                    Text(signedInEmail ?: "")
                    Button(onClick = {
                        preferences.edit().clear().apply()
                        signedInEmail = null
                        password = ""
                        message = "تم تسجيل الخروج."
                    }) { Text("تسجيل الخروج") }
                }
            }
        } else {
            Text(
                if (isRegister) "إنشاء حساب جديد" else "تسجيل الدخول",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (isRegister) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("الاسم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("البريد الإلكتروني") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("كلمة المرور (8 أحرف على الأقل)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val cleanEmail = email.trim()
                    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                        message = "اكتب بريدًا إلكترونيًا صحيحًا."
                    } else if (password.length < 8) {
                        message = "كلمة المرور يجب ألا تقل عن 8 أحرف."
                    } else if (isRegister && displayName.isBlank()) {
                        message = "اكتب اسمك."
                    } else {
                        isLoading = true
                        message = "جارٍ الاتصال..."
                        authExecutor.execute {
                            var resultMessage = "تعذر الاتصال بالخادم. حاول مرة أخرى."
                            var token: String? = null
                            try {
                                val connection = java.net.URL(
                                    "$AUTH_API_BASE_URL/api/auth/${if (isRegister) "register" else "login"}"
                                ).openConnection() as java.net.HttpURLConnection
                                connection.requestMethod = "POST"
                                connection.connectTimeout = 15000
                                connection.readTimeout = 20000
                                connection.doOutput = true
                                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                                val body = org.json.JSONObject()
                                    .put("email", cleanEmail)
                                    .put("password", password)
                                if (isRegister) body.put("displayName", displayName.trim())
                                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                                val code = connection.responseCode
                                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                                val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                                val response = org.json.JSONObject(responseText.ifBlank { "{}" })
                                if (code in 200..299) {
                                    resultMessage = response.optString("message", "تمت العملية بنجاح.")
                                    val session = response.optJSONObject("session")
                                    token = session?.optString("accessToken")?.takeIf { !it.isNullOrBlank() }
                                    if (token != null) {
                                        preferences.edit()
                                            .putString("access_token", token)
                                            .putString("refresh_token", session?.optString("refreshToken"))
                                            .putString("email", cleanEmail)
                                            .apply()
                                    }
                                } else {
                                    resultMessage = response.optString("message", "تعذر إتمام العملية. راجع البيانات وحاول مرة أخرى.")
                                }
                                connection.disconnect()
                            } catch (_: Exception) {
                                resultMessage = "تعذر الاتصال بالخادم. تأكد من الإنترنت وحاول مرة أخرى."
                            }
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                isLoading = false
                                message = resultMessage
                                if (token != null) signedInEmail = cleanEmail
                                if (isRegister && token == null && resultMessage.contains("تحقق من بريدك")) {
                                    isRegister = false
                                }
                            }
                        }
                    }
                }
            ) {
                Text(if (isLoading) "يرجى الانتظار..." else if (isRegister) "إنشاء الحساب" else "تسجيل الدخول")
            }
            Button(
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    isRegister = !isRegister
                    message = ""
                }
            ) {
                Text(if (isRegister) "لدي حساب بالفعل — تسجيل الدخول" else "ليس لدي حساب — إنشاء حساب")
            }
        }
        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
        Text(
            "يتطلب إنشاء الحساب بريدًا إلكترونيًا صالحًا. إذا طُلب تأكيد البريد، افتح رسالة التأكيد أولًا ثم سجّل الدخول.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DemoNotice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Text(
            "وضع تجريبي: الأرقام والمهام المعروضة للتصميم والاختبار فقط، ولا تمثل أموالًا أو أرباحًا حقيقية.",
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun BalanceCard(title: String, amount: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionCard(title: String, description: String, action: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onClick) { Text(action) }
        }
    }
}
