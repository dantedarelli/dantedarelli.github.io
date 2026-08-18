# Viajar é Legal — aplicativo Android

Versão mobile do dashboard AirBnB RJ × NY. Kotlin + Jetpack Compose (Material 3),
sem banco de dados e sem rede: a base vem embutida no código e tudo é calculado
no aparelho.

**Autor:** Dante Darelli · dante.darelli@hotmail.com

---

## Acesso

| Campo   | Valor      |
|---------|------------|
| Usuário | `dante`    |
| Senha   | `dante123` |

As credenciais são fixas em [`data/Auth.kt`](app/src/main/java/br/com/viajarelegal/airbnb/data/Auth.kt)
e ficam visíveis na própria tela de login, por ser um ambiente de demonstração.

## Como abrir e rodar

1. Abra a pasta `AppAirBnB` no **Android Studio** (Ladybug ou mais recente).
2. Aceite a sincronização do Gradle — ela baixa AGP 8.7.3, Kotlin 2.0.21 e o
   Compose BOM 2024.12.01.
3. Rode em um emulador ou aparelho com **Android 7.0 (API 24)** ou superior.

O repositório não inclui o `gradle-wrapper.jar` (binário). O Android Studio
gera o wrapper sozinho na primeira sincronização; para gerar pela linha de
comando, com o Gradle instalado:

```bash
gradle wrapper --gradle-version 8.9
```

## O que o aplicativo faz

**Painel** — indicadores (anúncios, mediana, faixa interquartil, extremos,
disponibilidade, avaliações), histograma de preços com classes de
Freedman–Diaconis, diagrama de caixa por cidade, ranking de bairros, composição
por tipo de acomodação e série anual de atividade.

**Bairros** — mapa de calor em *treemap squarified*: a área de cada célula é o
número de anúncios e a cor é a métrica escolhida (mediana, média, contagem,
disponibilidade ou avaliações), na mesma escala sequencial de 7 classes do
coroplético da versão web. Substitui o mapa geográfico sem exigir chave de API
nem conexão.

**Comparar** — RJ e NY lado a lado em três blocos: moeda local, base comum
convertida por uma **taxa de câmbio que o usuário edita**, e indicadores
adimensionais (coeficiente de variação, assimetria, ocupação) que não dependem
de câmbio nenhum.

**Anúncios** — lista com busca, ordenação e ficha completa em folha inferior.

**Filtros** (ícone no cabeçalho) — cidades, tipos, bairros, faixa de preço,
noites mínimas, disponibilidade, avaliações e o tratamento de valores extremos
(IQR de Tukey, corte por percentil, Z-score modificado por MAD; remover,
winsorizar ou destacar). Os limites são calculados **por cidade**, já que as
moedas são diferentes.

**Importar** (botão flutuante) — duas vias:

- *Planilha*: escolhe um CSV pelo seletor do sistema, detecta o separador
  (`,` `;` tabulação ou `|`), reconhece as colunas por apelidos em inglês e
  português, mostra prévia e permite **remapear qualquer coluna à mão** antes de
  confirmar.
- *Linha manual*: formulário validado para lançar um anúncio avulso.

O que é importado entra na base viva e passa a valer imediatamente em todos os
gráficos, filtros e comparações.

## Dados embutidos

`data/SeedData.kt` traz **600 anúncios** — 300 do Rio de Janeiro e 300 de Nova
York — em amostra estratificada por bairro × tipo de acomodação, extraída dos
arquivos `ny.csv` e `rj.csv` (padrão Inside Airbnb) em 18/08/2026, mantendo
apenas registros com preço e coordenadas válidos. O sorteio é determinístico
(semente 20260818), então a base é sempre a mesma.

Cuidados herdados dos arquivos originais e já tratados aqui:

- preços em **moedas diferentes** (USD em NY, BRL no RJ) — nunca compartilham
  escala sem conversão explícita;
- `neighbourhood_group` **vazio no RJ** — as agregações caem para `neighbourhood`;
- linhas sem preço foram descartadas na amostragem.

Como não há persistência, fechar o aplicativo devolve a base ao estado embutido.

## Estrutura

```
app/src/main/java/br/com/viajarelegal/airbnb/
├── MainActivity.kt          entrada e alternância de tema
├── data/                    Auth, Listing, SeedData, CsvParser, Importer, repositório
├── domain/                  Stats, Outliers, Filters, Format
└── ui/
    ├── AppRaiz.kt           navegação, barra inferior, botão de importar
    ├── DashboardViewModel   estado de filtros e todas as agregações derivadas
    ├── components/          painéis, cartões, etiquetas e os gráficos em Canvas
    ├── theme/               paleta transposta do dashboard web (claro e escuro)
    ├── login/ painel/ bairros/ comparar/ anuncios/ filtros/ importar/
```

Nenhuma biblioteca de gráficos: histograma, rosca, barras, linha, boxplot e
treemap são desenhados em `Canvas` do Compose.

## Permissões

O `AndroidManifest.xml` não declara permissão alguma. A leitura de planilhas usa
o *Storage Access Framework*, que concede acesso pontual apenas ao arquivo que o
usuário escolher.
