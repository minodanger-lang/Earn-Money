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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

@Composable
private fun AccountScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("حسابي", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("زائر تجريبي", fontWeight = FontWeight.Bold)
                Text("تسجيل الحسابات لم يتم ربطه بالخادم بعد.")
            }
        }
        Text("سيتم إضافة التسجيل وتسجيل الدخول والتحقق من الحساب في المرحلة التالية.")
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
