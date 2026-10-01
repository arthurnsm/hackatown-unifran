# Mapa de impactos do El Niño

Backend Java simples para um mapa de calor do Brasil. Ele compara o El Niño de junho de 2023 a maio de 2024 com a média dos dez períodos anuais anteriores, usando dados históricos da Open-Meteo.

As séries usam o modelo de reanálise ERA5, para que temperatura e precipitação mantenham a mesma fonte em todo o período.

Não há banco de dados nem persistência. A resposta da Open-Meteo fica somente na memória por 6 horas.

## Organização MVC

- `model`: objetos do domínio e modelos retornados ao frontend;
- `controller`: entrada HTTP, validação da métrica e coordenação da resposta;
- `view`: serialização dos modelos e erros para JSON;
- `service`: cálculo dos impactos e cache em memória;
- `client`: comunicação com a Open-Meteo;
- `config`: lista dos pontos geográficos das capitais.

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
- `currentValue`: valor no período do El Niño;
- `historicalValue`: média histórica usada na comparação;
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
- Intensidade: valor absoluto da anomalia dividido pela maior anomalia entre as 27 capitais.

Os valores representam associação temporal e anomalias durante o período, não provam causalidade isolada do El Niño.
