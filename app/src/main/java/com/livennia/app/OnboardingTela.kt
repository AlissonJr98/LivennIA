package com.livennia.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val onboardingPaginas = listOf(
    Triple("Bem-vindo ao LivennIA", "Uma aventura sombria, escrita por uma IA, só para você.", "📖"),
    Triple("Crie seu personagem", "Dê um nome ao seu herói e comece sua jornada em uma floresta escura.", "🧙"),
    Triple("Escolha suas ações", "Leia a narrativa, escolha uma das opções ou escreva sua própria ação.", "⚔️"),
    Triple("Sobreviva", "Gerencie sua vida, colete itens e explore novos lugares. Boa sorte, viajante!", "💀")
)

@Composable
fun OnboardingTela(vm: AppViewModel) {
    val pagerState = rememberPagerState(pageCount = { onboardingPaginas.size })
    val escopo = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { pagina ->
            val (titulo, descricao, emoji) = onboardingPaginas[pagina]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(emoji, style = MaterialTheme.typography.headlineLarge, fontSize = 64.sp)
                Spacer(Modifier.height(24.dp))
                Text(titulo, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(descricao, color = TextoSuave, textAlign = TextAlign.Center)
            }
        }
        // Indicadores
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(onboardingPaginas.size) { i ->
                val cor = if (i == pagerState.currentPage) MaterialTheme.colorScheme.primary else TextoSuave
                Text("●", color = cor, modifier = Modifier.padding(4.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        if (pagerState.currentPage == onboardingPaginas.lastIndex) {
            Button(
                onClick = {
                    vm.marcarOnboardingVisto()
                    vm.navegarPara(Tela.Entrada)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Começar")
            }
        } else {
            TextButton(onClick = {
                escopo.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }) {
                Text("Próximo")
            }
        }
        TextButton(onClick = {
            vm.marcarOnboardingVisto()
            vm.navegarPara(Tela.Entrada)
        }) {
            Text("Pular")
        }
    }
}