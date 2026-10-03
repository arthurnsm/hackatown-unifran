# Mapa de impactos do El Niño

Backend Java simples para um mapa de calor do Brasil. Ele compara a temperatura média e a chuva acumulada nos últimos sete dias com a média das mesmas janelas nos três anos anteriores, usando dados da Open-Meteo.

As séries usam o modelo de reanálise ERA5, para que temperatura e precipitação mantenham a mesma fonte em todo o período.

Não há banco de dados nem persistência. A resposta da Open-Meteo fica somente na memória por 6 horas.

## Organização MVC

- `model`: objetos do domínio e modelos retornados ao frontend;
- `controller`: entrada HTTP, validação da métrica e coordenação da resposta;
- `view`: serialização dos modelos e erros para JSON;
- `service`: cálculo dos impactos e cache em memória;
- `client`: comunicação com a Open-Meteo;
- `config`: grade de amostragem distribuída pelo território brasileiro.

## Requisitos

- Java 17 ou mais recente
- Maven 3.8 ou mais recente

## Executar

```bash
mvn clean test
mvn exec:java
```

A API inicia em `http://localhost:8080`.

## Endpoints para o frontend

```text
GET /api/impacts?metric=temperature
GET /api/impacts?metric=rainfall
GET /health
```

O frontend pode trocar apenas o valor de `metric` quando o usuário clicar no toggle. CORS já está liberado para facilitar o protótipo.

Cada ponto contém:

- `latitude` e `longitude`: posição no mapa;
- `currentValue`: temperatura média ou chuva acumulada nos últimos sete dias;
- `historicalValue`: média das mesmas janelas nos três anos anteriores;
- `absoluteChange`: diferença absoluta; em chuva, evita distorções quando a referência está próxima de zero;
- `percentageReliable`: indica se a referência semanal de chuva é de pelo menos 5 mm e o percentual pode ser exibido;
- `anomaly`: diferença em °C ou em porcentagem de chuva;
- `intensity`: magnitude normalizada entre 0 e 1 para o mapa de calor;
- `direction`: `HOTTER`, `COOLER`, `WETTER` ou `DRIER`.

Exemplo mínimo no JavaScript do frontend:

```javascript
async function carregarMapa(metrica) {
  const resposta = await fetch(`http://localhost:8080/api/impacts?metric=${metrica}`);
  if (!resposta.ok) throw new Error("Falha ao carregar impactos");
  const dados = await resposta.json();
  desenharMapaDeCalor(dados.points); // use point.latitude, longitude e intensity
}

carregarMapa("temperature");
// No toggle: carregarMapa("rainfall");
```

## Decisão de cálculo

- Temperatura: média diária no evento menos a média diária da linha de base.
- Chuva: precipitação acumulada no evento comparada à precipitação anual média da linha de base.
- Intensidade: valor absoluto da anomalia dividido pela maior anomalia entre as 59 amostras nacionais.

Os valores representam anomalias da data atual e não provam causalidade isolada do El Niño.

## Alerta de enchente via Telegram

O módulo de alertas segue MVC: `model` representa o pedido e a resposta, `controller` coordena o envio, `service` valida e monta a mensagem, e `view` exibe o resultado. O cliente Telegram realiza a integração externa.

Defina estas variáveis de ambiente — ou acrescente-as ao arquivo `backend/.env`, que é ignorado pelo Git:

```bash
export TELEGRAM_BOT_TOKEN='token-fornecido-pelo-BotFather'
export TELEGRAM_CHAT_ID='id-do-chat-que-recebe-o-alerta'
```

O usuário precisa abrir uma conversa com o bot e enviar `/start` antes de receber alertas. Para executar a demonstração:

```bash
mvn -Dmaven.repo.local=/tmp/hackatown-m2 compile && java -cp target/classes br.com.hackatown.elnino.alerts.AlertApplication
```

O backend verifica diariamente a previsão agregada do dia seguinte: temperatura média diária e precipitação acumulada diária. Sem risco, não envia nada. Os limites atuais são 30 mm de chuva e 35 °C de temperatura média. Enquanto o cadastro não existe, use `ALERT_CITY`, `ALERT_STATE`, `ALERT_LATITUDE` e `ALERT_LONGITUDE` para definir o local monitorado. O padrão atual é Salvador, BA.

## Cadastro de alertas

O frontend deve enviar `POST /api/alert-subscriptions` com `chatId`, `city`, `state`, `latitude` e `longitude`. Os cadastros ficam em `backend/data/alert-subscribers.json`, que não é versionado.
