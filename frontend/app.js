const API_URL = 'http://127.0.0.1:8080/api/impacts';
const map = L.map('map', {
    zoomControl: false,
    minZoom: 3,
    zoomSnap: .25,
    maxBounds: [[-46, -88], [18, -20]],
    maxBoundsViscosity: 0.7
});

L.control.zoom({ position: 'bottomright' }).addTo(map);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 18
}).addTo(map);
map.setView([-14.2, -51.9], 3.5);

const layerGroup = L.layerGroup().addTo(map);
let activeMetric = 'temperature';

const mockData = {
    temperature: { points: [
        { city: 'Brasília', id: 'DF', latitude: -15.78, longitude: -47.93, currentValue: 26.5, historicalValue: 24, anomaly: 2.5, intensity: 1, direction: 'HOTTER' },
        { city: 'São Paulo', id: 'SP', latitude: -23.55, longitude: -46.63, currentValue: 24.1, historicalValue: 22, anomaly: 2.1, intensity: .84, direction: 'HOTTER' },
        { city: 'Manaus', id: 'AM', latitude: -3.12, longitude: -60.02, currentValue: 29.5, historicalValue: 28, anomaly: 1.5, intensity: .6, direction: 'HOTTER' }
    ]},
    rainfall: { points: [
        { city: 'Porto Alegre', id: 'RS', latitude: -30.03, longitude: -51.22, currentValue: 80, historicalValue: 50, anomaly: 60, absoluteChange: 30, percentageReliable: true, intensity: 1, direction: 'WETTER' },
        { city: 'Florianópolis', id: 'SC', latitude: -27.59, longitude: -48.55, currentValue: 55, historicalValue: 40, anomaly: 37.5, absoluteChange: 15, percentageReliable: true, intensity: .5, direction: 'WETTER' }
    ]}
};

function unitsFor(metric) {
    return metric === 'temperature'
        ? { anomaly: '°C', value: '°C' }
        : { anomaly: '%', value: ' mm' };
}

function anomalyDisplay(point, metric, digits = 1) {
    if (metric === 'temperature') return `${signed(point.anomaly, digits)}°C`;

    const millimeters = `${signed(point.absoluteChange, digits)} mm`;
    return point.percentageReliable
        ? `${millimeters} (${signed(point.anomaly, digits)}%)`
        : millimeters;
}

function anomalyContext(point, metric) {
    if (metric === 'rainfall' && !point.percentageReliable) {
        return 'percentual omitido: referência histórica próxima de zero';
    }
    return `${directionLabel(point.direction)} que a referência histórica`;
}

function colorFor(direction) {
    return ({ HOTTER: '#d9573f', COOLER: '#3977a9', WETTER: '#2d8075', DRIER: '#c98432' })[direction] || '#74817a';
}

function directionLabel(direction) {
    return ({ HOTTER: 'mais quente', COOLER: 'mais frio', WETTER: 'mais chuvoso', DRIER: 'mais seco' })[direction] || 'alteração observada';
}

function signed(value, digits = 1) {
    return `${value > 0 ? '+' : ''}${Number(value).toFixed(digits)}`;
}

function placeName(point) {
    if (point.city && point.id) return `${point.city}, ${point.id}`;
    if (point.city && point.state && point.state.includes('°')) return `${point.city} · ${point.state}`;
    if (point.city && point.state) return `${point.city}, ${point.state}`;
    return point.city || 'Local analisado';
}

function renderMap(points, metric) {
    layerGroup.clearLayers();
    const units = unitsFor(metric);
    const positive = [];
    const negative = [];

    points.forEach(point => {
        const heatPoint = [point.latitude, point.longitude, Math.max(.18, Number(point.intensity || 0))];
        if (point.direction === 'HOTTER' || point.direction === 'WETTER') positive.push(heatPoint);
        else negative.push(heatPoint);
    });

    const positiveGradient = metric === 'temperature'
        ? { .15: '#f4d9a7', .5: '#ed9a52', 1: '#d9573f' }
        : { .15: '#c9ded2', .5: '#69aa96', 1: '#1d7167' };
    const negativeGradient = metric === 'temperature'
        ? { .15: '#dce7ed', .55: '#77a9c7', 1: '#3977a9' }
        : { .15: '#f1dfc2', .55: '#dda85e', 1: '#bd7428' };

    const heatOptions = { radius: 65, blur: 45, maxZoom: 7, minOpacity: .34 };
    if (positive.length) L.heatLayer(positive, { ...heatOptions, gradient: positiveGradient }).addTo(layerGroup);
    if (negative.length) L.heatLayer(negative, { ...heatOptions, gradient: negativeGradient }).addTo(layerGroup);

    points.forEach(point => {
        const color = colorFor(point.direction);
        const clickRadius = 9 + Math.max(0, Number(point.intensity || 0)) * 4;
        const marker = L.circleMarker([point.latitude, point.longitude], {
            radius: clickRadius,
            fillColor: color,
            color: '#fff',
            weight: 1.5,
            opacity: .9,
            fillOpacity: .42,
            bubblingMouseEvents: false
        });

        marker.bindPopup(`
            <div style="font-family:Inter,sans-serif;min-width:180px;color:#12231c">
                <strong style="display:block;font-size:15px;margin-bottom:3px">${placeName(point)}</strong>
                <span style="color:#66736d;font-size:12px">${directionLabel(point.direction)}</span>
                <div style="font-size:24px;font-weight:750;color:${color};margin:10px 0">${anomalyDisplay(point, metric)}</div>
                <div style="color:#66736d;font-size:11px;margin:-6px 0 8px">${anomalyContext(point, metric)}</div>
                <div style="font-size:12px;line-height:1.6">Últimos 7 dias: <b>${Number(point.currentValue).toFixed(1)}${units.value}</b><br>Média das mesmas janelas: <b>${Number(point.historicalValue).toFixed(1)}${units.value}</b></div>
            </div>
        `);
        marker.addTo(layerGroup);
    });

    renderInsights(points, metric);
}

function renderInsights(points, metric) {
    const magnitude = point => Math.abs(
        metric === 'rainfall' ? Number(point.absoluteChange || 0) : Number(point.anomaly || 0)
    );
    const sorted = [...points].sort((a, b) => magnitude(b) - magnitude(a));
    const units = unitsFor(metric);
    const highlight = sorted[0];
    const highlightPanel = document.getElementById('highlight-panel');
    const ranking = document.getElementById('ranking-list');

    if (!highlight) {
        highlightPanel.innerHTML = '<p class="eyebrow">Maior impacto observado</p><p class="loading-copy">Nenhum dado encontrado.</p>';
        ranking.innerHTML = '';
        return;
    }

    const color = colorFor(highlight.direction);
    highlightPanel.innerHTML = `
        <p class="eyebrow">Maior impacto observado</p>
        <h2 class="highlight-place">${placeName(highlight)}</h2>
        <div class="highlight-value" style="color:${color}">${anomalyDisplay(highlight, metric, 1)}</div>
        <div class="highlight-direction">${anomalyContext(highlight, metric)}</div>
        <div class="comparison">
            <div class="stat"><span>Últimos 7 dias</span><strong>${Number(highlight.currentValue).toFixed(1)}${units.value}</strong></div>
            <div class="stat"><span>Janelas anteriores · média</span><strong>${Number(highlight.historicalValue).toFixed(1)}${units.value}</strong></div>
        </div>
    `;

    ranking.innerHTML = sorted.slice(1, 4).map((point, index) => `
        <div class="ranking-row">
            <span class="rank">0${index + 2}</span>
            <div class="ranking-place"><strong>${placeName(point)}</strong><span>${directionLabel(point.direction)}</span></div>
            <span class="ranking-value" style="color:${colorFor(point.direction)}">${anomalyDisplay(point, metric)}</span>
        </div>
    `).join('');
}

function updateMetricCopy(metric) {
    const temperature = metric === 'temperature';
    document.querySelector('[data-metric="temperature"]').classList.toggle('active', temperature);
    document.querySelector('[data-metric="rainfall"]').classList.toggle('active', !temperature);
    document.getElementById('map-title').textContent = temperature ? 'Anomalia de temperatura' : 'Anomalia de precipitação';
    document.getElementById('map-description').textContent = temperature
        ? 'Interpolação de 59 amostras da diferença de temperatura em relação ao histórico.'
        : 'Interpolação de 59 amostras da diferença de chuva acumulada em milímetros.';
    document.getElementById('legend-scale').classList.toggle('rainfall', !temperature);
    document.getElementById('legend-start').textContent = temperature ? 'Mais frio' : 'Mais seco';
    document.getElementById('legend-end').textContent = temperature ? 'Mais quente' : 'Mais chuvoso';
}

function setConnectionStatus(status) {
    const dot = document.getElementById('status-dot');
    const label = document.getElementById('source-label');
    dot.className = `status-dot ${status === 'loading' ? 'loading' : status === 'offline' ? 'offline' : ''}`;
    label.textContent = status === 'loading'
        ? 'Carregando Open-Meteo / ERA5'
        : status === 'offline' ? 'Demonstração local' : 'Fonte: Open-Meteo / ERA5';
}

function updateComparisonCopy(comparison) {
    if (!comparison) return;
    document.getElementById('comparison-summary').textContent =
        `${comparison.event} · comparação com ${comparison.baseline}`;
    document.getElementById('method-note').textContent =
        `Referência histórica: ${comparison.baseline}. A comparação mostra uma anomalia climática, não atribuição causal isolada ao El Niño.`;
}

async function loadData(metric) {
    setConnectionStatus('loading');
    try {
        const response = await fetch(`${API_URL}?metric=${metric}`);
        if (!response.ok) throw new Error(`API respondeu ${response.status}`);
        const data = await response.json();
        if (!Array.isArray(data.points)) throw new Error('Resposta sem pontos geográficos');
        if (metric !== activeMetric) return;
        updateComparisonCopy(data.comparison);
        renderMap(data.points, metric);
        setConnectionStatus('online');
    } catch (error) {
        console.warn('Backend indisponível; exibindo dados de demonstração.', error);
        if (metric !== activeMetric) return;
        renderMap(mockData[metric].points, metric);
        setConnectionStatus('offline');
    }
}

function setMetric(metric) {
    activeMetric = metric;
    updateMetricCopy(metric);
    loadData(metric);
}

document.querySelectorAll('[data-metric]').forEach(button => {
    button.addEventListener('click', () => setMetric(button.dataset.metric));
});

window.addEventListener('resize', () => map.invalidateSize());
setMetric('temperature');
