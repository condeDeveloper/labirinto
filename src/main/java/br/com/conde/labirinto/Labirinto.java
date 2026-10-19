package br.com.conde.labirinto;

import java.util.ArrayList;
import java.util.List;

/**
 * UM LABIRINTO numa grade, guardado pelas PAREDES QUE CAÍRAM.
 *
 * <p>Numa grade de {@code linhas x colunas}, cada par de células vizinhas tem uma parede entre
 * elas, que pode estar de pé ou derrubada. Guardar as derrubadas, e não as células, é o que
 * torna o assunto enumerável: numa grade 3x3 há 12 paredes internas, então há {@code 2^12}
 * labirintos possíveis, e todos cabem num laço.
 *
 * <p>Um labirinto é PERFEITO quando dá para ir de qualquer célula a qualquer outra, e por um
 * caminho só. Isso é exatamente o que define uma árvore, e a segunda medida usa esse fato
 * para contar quantos existem.
 */
public record Labirinto(int linhas, int colunas, int paredesDerrubadas) {

    /** Quantas paredes internas tem uma grade deste tamanho. */
    public static int quantasParedes(int linhas, int colunas) {
        return linhas * (colunas - 1) + colunas * (linhas - 1);
    }

    /** Quantos labirintos diferentes cabem nesta grade. */
    public static long quantosLabirintos(int linhas, int colunas) {
        return 1L << quantasParedes(linhas, colunas);
    }

    public int celulas() {
        return linhas * colunas;
    }

    public int paredes() {
        return quantasParedes(linhas, colunas);
    }

    /**
     * O ÍNDICE DA PAREDE entre duas células vizinhas.
     *
     * <p>As horizontais vêm primeiro, depois as verticais. A ordem não importa para nada além
     * de ser consistente, e ser consistente importa muito: um índice calculado de dois jeitos
     * diferentes derruba paredes que não existem.
     */
    public int indiceDaParede(int linha, int coluna, boolean paraDireita) {
        if (paraDireita) return linha * (colunas - 1) + coluna;

        return linhas * (colunas - 1) + linha * colunas + coluna;
    }

    public boolean passa(int linha, int coluna, boolean paraDireita) {
        if (paraDireita && coluna + 1 >= colunas) return false;
        if (!paraDireita && linha + 1 >= linhas) return false;

        return (paredesDerrubadas & (1 << indiceDaParede(linha, coluna, paraDireita))) != 0;
    }

    public int celula(int linha, int coluna) {
        return linha * colunas + coluna;
    }

    /** As células vizinhas que dá para alcançar a partir desta, em um passo. */
    public List<Integer> vizinhas(int celula) {
        int linha = celula / colunas;
        int coluna = celula % colunas;

        List<Integer> saida = new ArrayList<>();

        if (passa(linha, coluna, true)) saida.add(celula(linha, coluna + 1));
        if (coluna > 0 && passa(linha, coluna - 1, true)) saida.add(celula(linha, coluna - 1));
        if (passa(linha, coluna, false)) saida.add(celula(linha + 1, coluna));
        if (linha > 0 && passa(linha - 1, coluna, false)) saida.add(celula(linha - 1, coluna));

        return saida;
    }

    public int quantasDerrubadas() {
        return Integer.bitCount(paredesDerrubadas);
    }

    /* ===================== as propriedades ===================== */

    /** Dá para ir de qualquer célula a qualquer outra. */
    public boolean conexo() {
        boolean[] visto = new boolean[celulas()];

        java.util.ArrayDeque<Integer> fila = new java.util.ArrayDeque<>();

        visto[0] = true;
        fila.add(0);

        int alcancadas = 1;

        while (!fila.isEmpty()) {
            int atual = fila.poll();

            for (int vizinha : vizinhas(atual)) {
                if (visto[vizinha]) continue;

                visto[vizinha] = true;
                alcancadas++;

                fila.add(vizinha);
            }
        }

        return alcancadas == celulas();
    }

    /**
     * NÃO TEM CICLO, que é o mesmo que dizer que há um caminho só entre cada par.
     *
     * <p>Com n células e n-1 passagens, conexo já implica sem ciclo. A conferência aqui é
     * direta mesmo assim, por busca, porque contar passagens e concluir seria usar o teorema
     * que a segunda medida quer verificar.
     */
    public boolean semCiclo() {
        boolean[] visto = new boolean[celulas()];

        for (int inicio = 0; inicio < celulas(); inicio++) {
            if (visto[inicio]) continue;

            if (temCicloA(inicio, -1, visto)) return true;
        }

        return false;
    }

    private boolean temCicloA(int atual, int veioDe, boolean[] visto) {
        visto[atual] = true;

        for (int vizinha : vizinhas(atual)) {
            if (vizinha == veioDe) continue;

            if (visto[vizinha]) return true;

            if (temCicloA(vizinha, atual, visto)) return true;
        }

        return false;
    }

    /** Perfeito: conexo e sem ciclo. Um caminho só entre cada par de células. */
    public boolean perfeito() {
        return conexo() && !semCiclo();
    }

    /**
     * TEM ILHA: existe um pedaço de parede que não encosta na borda.
     *
     * <p>Isso é o que quebra a regra da mão direita. Enquanto toda parede estiver ligada à
     * borda, andar com a mão encostada percorre o contorno e passa por tudo. Uma parede solta
     * no meio é um contorno separado, e quem entra nele fica lá.
     *
     * <p>Num labirinto perfeito nunca há ilha, porque não há ciclo de parede. O jeito de
     * fabricar uma é derrubar paredes a mais, criando um ciclo de passagem em volta de um
     * bloco.
     */
    public boolean temIlha() {
        return !semCiclo() && conexo();
    }

    public String mostrar() {
        StringBuilder saida = new StringBuilder();

        saida.append("+").append("--+".repeat(colunas)).append('\n');

        for (int linha = 0; linha < linhas; linha++) {
            saida.append('|');

            for (int coluna = 0; coluna < colunas; coluna++)
                saida.append("  ").append(passa(linha, coluna, true) ? ' ' : '|');

            saida.append('\n').append('+');

            for (int coluna = 0; coluna < colunas; coluna++)
                saida.append(passa(linha, coluna, false) ? "  " : "--").append('+');

            saida.append('\n');
        }

        return saida.toString();
    }
}
