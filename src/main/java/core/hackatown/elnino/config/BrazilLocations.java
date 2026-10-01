package br.com.hackatown.elnino.config;

import br.com.hackatown.elnino.model.Location;

import java.util.List;

public final class BrazilLocations {
    private BrazilLocations() {
    }

    public static final List<Location> CAPITALS = List.of(
            new Location("AC", "Rio Branco", "Acre", -9.9754, -67.8249),
            new Location("AL", "Maceió", "Alagoas", -9.6498, -35.7089),
            new Location("AP", "Macapá", "Amapá", 0.0349, -51.0694),
            new Location("AM", "Manaus", "Amazonas", -3.1190, -60.0217),
            new Location("BA", "Salvador", "Bahia", -12.9777, -38.5016),
            new Location("CE", "Fortaleza", "Ceará", -3.7319, -38.5267),
            new Location("DF", "Brasília", "Distrito Federal", -15.7939, -47.8828),
            new Location("ES", "Vitória", "Espírito Santo", -20.3155, -40.3128),
            new Location("GO", "Goiânia", "Goiás", -16.6869, -49.2648),
            new Location("MA", "São Luís", "Maranhão", -2.5307, -44.3068),
            new Location("MT", "Cuiabá", "Mato Grosso", -15.6014, -56.0979),
            new Location("MS", "Campo Grande", "Mato Grosso do Sul", -20.4697, -54.6201),
            new Location("MG", "Belo Horizonte", "Minas Gerais", -19.9167, -43.9345),
            new Location("PA", "Belém", "Pará", -1.4558, -48.4902),
            new Location("PB", "João Pessoa", "Paraíba", -7.1195, -34.8450),
            new Location("PR", "Curitiba", "Paraná", -25.4284, -49.2733),
            new Location("PE", "Recife", "Pernambuco", -8.0476, -34.8770),
            new Location("PI", "Teresina", "Piauí", -5.0892, -42.8016),
            new Location("RJ", "Rio de Janeiro", "Rio de Janeiro", -22.9068, -43.1729),
            new Location("RN", "Natal", "Rio Grande do Norte", -5.7945, -35.2110),
            new Location("RS", "Porto Alegre", "Rio Grande do Sul", -30.0346, -51.2177),
            new Location("RO", "Porto Velho", "Rondônia", -8.7608, -63.8999),
            new Location("RR", "Boa Vista", "Roraima", 2.8235, -60.6758),
            new Location("SC", "Florianópolis", "Santa Catarina", -27.5949, -48.5482),
            new Location("SP", "São Paulo", "São Paulo", -23.5505, -46.6333),
            new Location("SE", "Aracaju", "Sergipe", -10.9472, -37.0731),
            new Location("TO", "Palmas", "Tocantins", -10.1840, -48.3336)
    );
}
