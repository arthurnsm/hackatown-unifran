const API_URL = 'http://127.0.0.1:8080/api/impacts';
const map = L.map('map', {
    zoomControl: false,
    minZoom: 4,
    maxBounds: [[-46, -88], [18, -20]],
    maxBoundsViscosity: 0.7
});

L.control.zoom({ position: 'bottomright' }).addTo(map);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 18
}).addTo(map);
map.setView([-14.2, -51.9], 4);

const layerGroup = L.layerGroup().addTo(map);
let activeMetric = 'temperature';

const mockData = {
    temperature: { points: [
        { city: 'Brasília', id: 'DF', latitude: -15.78, longitude: -47.93, currentValue: 26.5, historicalValue: 24, anomaly: 2.5, intensity: 1, direction: 'HOTTER' },
        { city: 'São Paulo', id: 'SP', latitude: -23.55, longitude: -46.63, currentValue: 24.1, historicalValue: 22, anomaly: 2.1, intensity: .84, direction: 'HOTTER' },
        { city: 'Manaus', id: 'AM', latitude: -3.12, longitude: -60.02, currentValue: 29.5, historicalValue: 28, anomaly: 1.5, intensity: .6, direction: 'HOTTER' }
    ]},
    rainfall: { points: [
        { city: 'Porto Alegre', id: 'RS', latitude: -30.03, longitude: -51.22, currentValue: 2100, historicalValue: 1500, anomaly: 40, intensity: 1, direction: 'WETTER' },
        { city: 'Florianópolis', id: 'SC', latitude: -27.59, longitude: -48.55, currentValue: 1800, historicalValue: 1400, anomaly: 28.6, intensity: .7, direction: 'WETTER' }
    ]}
};

function unitsFor(metric) {
    return metric === 'temperature'
        ? { anomaly: '°C', value: '°C' }
        : { anomaly: '%', value: ' mm' };
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
    if (point.city && point.state) return `${point.city}, ${point.state}`;
    return point.city || 'Local analisado';
}

function renderMap(points, metric) {
    layerGroup.clearLayers();
    const units = unitsFor(metric);

    points.forEach(point => {
        const color = colorFor(point.direction);
        const radius = 6 + Math.max(0, Number(point.intensity || 0)) * 17;
        const marker = L.circleMarker([point.latitude, point.longitude], {
            radius,
            fillColor: color,
            color: '#fff',
            weight: 1.5,
            opacity: 1,
            fillOpacity: .72
        });

        marker.bindPopup(`
            <div style="font-family:Inter,sans-serif;min-width:180px;color:#12231c">
                <strong style="display:block;font-size:15px;margin-bottom:3px">${placeName(point)}</strong>
                <span style="color:#66736d;font-size:12px">${directionLabel(point.direction)}</span>
                <div style="font-size:24px;font-weight:750;color:${color};margin:10px 0">${signed(point.anomaly)}${units.anomaly}</div>
                <div style="font-size:12px;line-height:1.6">Período: <b>${Number(point.currentValue).toFixed(1)}${units.value}</b><br>Histórico: <b>${Number(point.historicalValue).toFixed(1)}${units.value}</b></div>
            </div>
        `);
        marker.addTo(layerGroup);
    });

    renderInsights(points, metric);
}

function renderInsights(points, metric) {
    const sorted = [...points].sort((a, b) => Math.abs(b.anomaly) - Math.abs(a.anomaly));
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
        <div class="highlight-value" style="color:${color}">${signed(highlight.anomaly, 2)}${units.anomaly}</div>
        <div class="highlight-direction">${directionLabel(highlight.direction)} que a referência histórica</div>
        <div class="comparison">
            <div class="stat"><span>No El Niño</span><strong>${Number(highlight.currentValue).toFixed(1)}${units.value}</strong></div>
            <div class="stat"><span>Média histórica</span><strong>${Number(highlight.historicalValue).toFixed(1)}${units.value}</strong></div>
        </div>
    `;

    ranking.innerHTML = sorted.slice(1, 4).map((point, index) => `
        <div class="ranking-row">
            <span class="rank">0${index + 2}</span>
            <div class="ranking-place"><strong>${placeName(point)}</strong><span>${directionLabel(point.direction)}</span></div>
            <span class="ranking-value" style="color:${colorFor(point.direction)}">${signed(point.anomaly)}${units.anomaly}</span>
        </div>
    `).join('');
}

function updateMetricCopy(metric) {
    const temperature = metric === 'temperature';
    document.querySelector('[data-metric="temperature"]').classList.toggle('active', temperature);
    document.querySelector('[data-metric="rainfall"]').classList.toggle('active', !temperature);
    document.getElementById('map-title').textContent = temperature ? 'Anomalia de temperatura' : 'Anomalia de precipitação';
    document.getElementById('map-description').textContent = temperature
        ? 'Diferença da temperatura média em relação ao histórico.'
        : 'Variação percentual da chuva acumulada em relação ao histórico.';
    document.getElementById('legend-scale').classList.toggle('rainfall', !temperature);
}

function setConnectionStatus(status) {
    const dot = document.getElementById('status-dot');
    const label = document.getElementById('source-label');
    dot.className = `status-dot ${status === 'loading' ? 'loading' : status === 'offline' ? 'offline' : ''}`;
    label.textContent = status === 'loading'
        ? 'Carregando Open-Meteo / ERA5'
        : status === 'offline' ? 'Demonstração local' : 'Fonte: Open-Meteo / ERA5';
}

async function loadData(metric) {
    setConnectionStatus('loading');
    try {
        const response = await fetch(`${API_URL}?metric=${metric}`);
        if (!response.ok) throw new Error(`API respondeu ${response.status}`);
        const data = await response.json();
        if (!Array.isArray(data.points)) throw new Error('Resposta sem pontos geográficos');
        if (metric !== activeMetric) return;
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
