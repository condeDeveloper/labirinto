package br.com.conde.labirinto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O labirinto e as propriedades que o definem.
 *
 * <p>O risco aqui é o índice da parede: calculado de dois jeitos diferentes, ele derruba
 * paredes que não existem e o labirinto fica consistente consigo mesmo e errado. Por isso o
 * primeiro teste confere a ida e volta do índice.
 */
class LabirintoTest {

    @Test
    @DisplayName("o numero de paredes internas de uma grade")
    void quantasParedes() {
        assertEquals(4, Labirinto.quantasParedes(2, 2));
        assertEquals(12, Labirinto.quantasParedes(3, 3));
        assertEquals(24, Labirinto.quantasParedes(4, 4));

        /* Duas por celula, menos as da borda. */
        assertEquals(7, Labirinto.quantasParedes(2, 3));
    }

    @Test
    @DisplayName("CADA PAREDE TEM UM INDICE SO, e nenhum indice serve a duas")
    void indicesDistintos() {
        for (int linhas = 2; linhas <= 4; linhas++)
            for (int colunas = 2; colunas <= 4; colunas++) {
                Labirinto labirinto = new Labirinto(linhas, colunas, 0);

                java.util.Set<Integer> vistos = new java.util.HashSet<>();

                for (int linha = 0; linha < linhas; linha++)
                    for (int coluna = 0; coluna < colunas; coluna++) {
                        if (coluna + 1 < colunas)
                            assertTrue(vistos.add(labirinto.indiceDaParede(linha, coluna, true)),
                                    "indice repetido na horizontal");

                        if (linha + 1 < linhas)
                            assertTrue(vistos.add(labirinto.indiceDaParede(linha, coluna, false)),
                                    "indice repetido na vertical");
                    }

                assertEquals(Labirinto.quantasParedes(linhas, colunas), vistos.size());

                for (int indice : vistos)
                    assertTrue(indice >= 0 && indice < Labirinto.quantasParedes(linhas, colunas),
                            "indice fora da faixa: " + indice);
            }
    }

    @Test
    @DisplayName("sem parede derrubada, nada se alcanca")
    void fechado() {
        Labirinto fechado = new Labirinto(3, 3, 0);

        assertFalse(fechado.conexo());
        assertFalse(fechado.perfeito());

        for (int celula = 0; celula < 9; celula++)
            assertTrue(fechado.vizinhas(celula).isEmpty(), "celula " + celula);
    }

    @Test
    @DisplayName("com tudo derrubado, tudo se alcanca e ha ciclo")
    void aberto() {
        Labirinto aberto = new Labirinto(3, 3, (1 << 12) - 1);

        assertTrue(aberto.conexo());
        assertTrue(aberto.semCiclo(), "com tudo aberto tem ciclo");
        assertFalse(aberto.perfeito());

        assertEquals(12, aberto.quantasDerrubadas());
    }

    @Test
    @DisplayName("A VIZINHANCA E SIMETRICA: se a alcanca b, b alcanca a")
    void vizinhancaSimetrica() {
        for (long bits = 0; bits < 4096; bits++) {
            Labirinto labirinto = new Labirinto(3, 3, (int) bits);

            for (int celula = 0; celula < 9; celula++)
                for (int vizinha : labirinto.vizinhas(celula))
                    assertTrue(labirinto.vizinhas(vizinha).contains(celula),
                            "bits " + bits + ": " + celula + " e " + vizinha);
        }
    }

    @Test
    @DisplayName("TODO PERFEITO tem exatamente celulas menos uma parede caida")
    void perfeitoTemNMenosUm() {
        for (long bits = 0; bits < 4096; bits++) {
            Labirinto labirinto = new Labirinto(3, 3, (int) bits);

            if (!labirinto.perfeito()) continue;

            assertEquals(8, labirinto.quantasDerrubadas(), "bits " + bits);
        }
    }

    @Test
    @DisplayName("e todo com oito paredes caidas e conexo e perfeito")
    void oitoCaidasEConexo() {
        for (long bits = 0; bits < 4096; bits++) {
            Labirinto labirinto = new Labirinto(3, 3, (int) bits);

            if (labirinto.quantasDerrubadas() != 8) continue;

            /* Nem todo padrao de oito e perfeito: pode ter ciclo e um pedaco solto. */
            assertEquals(labirinto.conexo() && !labirinto.semCiclo(), labirinto.perfeito(),
                    "bits " + bits);
        }
    }

    @Test
    @DisplayName("num perfeito a busca em largura sempre acha o caminho")
    void buscaSempreAcha() {
        for (long bits = 0; bits < 4096; bits++) {
            Labirinto labirinto = new Labirinto(3, 3, (int) bits);

            if (!labirinto.perfeito()) continue;

            assertTrue(MaoDireita.menorCaminho(labirinto) > 0, "bits " + bits);
        }
    }
}
