package br.com.conde.labirinto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Os números que aparecem escritos no README, travados em teste. */
class MedidasTest {

    /* ===================== 1. correcao ===================== */

    @Test
    @DisplayName("a grade 3x3 tem 4.096 padroes, 431 conexos e 192 perfeitos")
    void correcao() {
        Medidas.Correcao tres = Medidas.correcao(3, 3);

        assertEquals(12, tres.paredes());
        assertEquals(4096L, tres.labirintos());
        assertEquals(431, tres.conexos());
        assertEquals(192, tres.perfeitos());
        assertEquals(239, tres.comCiclo());

        /* Conexos sao os perfeitos mais os com ciclo. */
        assertEquals(tres.conexos(), tres.perfeitos() + tres.comCiclo());
    }

    @Test
    @DisplayName("nenhum perfeito tem numero errado de paredes caidas")
    void semErros() {
        for (Medidas.Correcao c : Medidas.correcao())
            assertEquals(0, c.erros(), c.linhas() + "x" + c.colunas());
    }

    /* ===================== 2. quantos ===================== */

    @Test
    @DisplayName("OS PERFEITOS BATEM com a contagem de arvores geradoras: 4, 15, 192, 56")
    void batemComKirchhoff() {
        for (Medidas.Contagem c : Medidas.quantos()) {
            assertTrue(c.bate(), c.grade() + ": " + c.perfeitos() + " contra " + c.tabelado());

            assertEquals(c.tabelado(), c.perfeitos(), c.grade());
        }

        assertEquals(192, Medidas.correcao(3, 3).perfeitos());
    }

    /* ===================== 3. maodireita ===================== */

    @Test
    @DisplayName("COM A SAIDA NO CANTO a mao direita nao falha nenhuma vez")
    void noCantoNuncaFalha() {
        Medidas.Regra canto = Medidas.maoDireita("canto de baixo a direita");

        assertEquals(431, canto.conexos());
        assertEquals(0, canto.falhou());
        assertEquals(0.0, canto.taxaDeFalha(), 1e-12);
    }

    @Test
    @DisplayName("E COM A SAIDA NO MEIO ela falha em 15 dos 431")
    void noMeioFalha() {
        Medidas.Regra meio = Medidas.maoDireita("centro da grade");

        assertEquals(431, meio.conexos());
        assertEquals(15, meio.falhou());

        assertTrue(meio.taxaDeFalha() > 0.03);
    }

    @Test
    @DisplayName("e ela funciona ate nos labirintos COM ciclo, se a saida for no canto")
    void comCicloTambemFunciona() {
        int comCiclo = 0;
        int falhou = 0;

        for (long bits = 0; bits < 4096; bits++) {
            Labirinto labirinto = new Labirinto(3, 3, (int) bits);

            if (!labirinto.conexo() || !labirinto.semCiclo()) continue;

            comCiclo++;

            if (!Medidas.chega(labirinto, 8)) falhou++;
        }

        assertEquals(239, comCiclo);
        assertEquals(0, falhou, "a ressalva das ilhas nao se aplica com saida no canto");
    }

    /* ===================== 4. geradores ===================== */

    @Test
    @DisplayName("TODOS os perfeitos estao na linha de oito paredes caidas")
    void soUmaLinhaTemPerfeito() {
        for (Medidas.Paredes p : Medidas.geradores())
            assertEquals(p.derrubadas() == 8, p.podeSerPerfeito(),
                    p.derrubadas() + " paredes caidas");
    }

    @Test
    @DisplayName("e a distribuicao soma 4.096, que e o total de padroes")
    void distribuicaoFecha() {
        int soma = 0;

        for (Medidas.Paredes p : Medidas.geradores()) soma += p.quantos();

        assertEquals(4096, soma);

        /* A linha de oito tem binomial(12,8) = 495 padroes, e so 192 sao perfeitos. */
        Medidas.Paredes oito = Medidas.geradores().get(8);

        assertEquals(495, oito.quantos());
        assertTrue(oito.quantos() > Medidas.correcao(3, 3).perfeitos());
    }

    /* ===================== 5. caminhos ===================== */

    @Test
    @DisplayName("a mao direita anda 1,69 vezes o menor caminho, em media")
    void andaMaisQueOPreciso() {
        Medidas.Caminho c = Medidas.caminhos();

        assertEquals(192, c.labirintos());

        assertEquals(1.69, c.razaoMedia(), 0.02);

        assertTrue(c.passosDaMao() > c.menorCaminho());
    }

    @Test
    @DisplayName("e no pior labirinto ela anda tres vezes o necessario")
    void piorCaso() {
        assertEquals(3, Medidas.caminhos().piorRazao());
    }
}
