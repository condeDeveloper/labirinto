package br.com.conde.labirinto;

import java.util.ArrayList;
import java.util.List;

/**
 * AS MEDIDAS: todos os labirintos de grades pequenas, enumerados pelos padrões de parede.
 *
 * <p>Numa grade 3x3 há 12 paredes internas, então há 4.096 labirintos possíveis e todos cabem
 * num laço. Nada aqui é sorteado, e nenhum gerador de labirinto é usado: o domínio é a lista
 * completa de padrões.
 */
public final class Medidas {

    private Medidas() {}

    /** As grades varridas inteiras. */
    public static final int[][] GRADES = {{2, 2}, {2, 3}, {3, 3}, {2, 4}};

    /* ===================== 1. correcao ===================== */

    public record Correcao(int linhas, int colunas, int paredes, long labirintos, int conexos,
            int perfeitos, int comCiclo, int erros) {}

    private static List<Correcao> correcao;

    /**
     * O QUE CADA PADRÃO DE PAREDE É.
     *
     * <p>Conexo é poder ir de qualquer célula a qualquer outra. Perfeito é conexo e sem ciclo,
     * ou seja, com um caminho só entre cada par.
     *
     * <p>A última coluna confere o teorema que a segunda medida usa: num labirinto perfeito o
     * número de paredes derrubadas é exatamente células menos um. Se algum perfeito tivesse
     * outro número, a conta por árvores geradoras não valeria.
     */
    public static List<Correcao> correcao() {
        if (correcao != null) return correcao;

        List<Correcao> saida = new ArrayList<>();

        for (int[] grade : GRADES) {
            int linhas = grade[0];
            int colunas = grade[1];

            long total = Labirinto.quantosLabirintos(linhas, colunas);

            int conexos = 0;
            int perfeitos = 0;
            int comCiclo = 0;
            int erros = 0;

            for (long bits = 0; bits < total; bits++) {
                Labirinto labirinto = new Labirinto(linhas, colunas, (int) bits);

                if (!labirinto.conexo()) continue;

                conexos++;

                if (labirinto.semCiclo()) {
                    comCiclo++;
                    continue;
                }

                perfeitos++;

                if (labirinto.quantasDerrubadas() != labirinto.celulas() - 1) erros++;
            }

            saida.add(new Correcao(linhas, colunas,
                    Labirinto.quantasParedes(linhas, colunas), total, conexos, perfeitos,
                    comCiclo, erros));
        }

        return correcao = List.copyOf(saida);
    }

    public static Correcao correcao(int linhas, int colunas) {
        return correcao().stream()
                .filter(x -> x.linhas() == linhas && x.colunas() == colunas)
                .findFirst().orElseThrow();
    }

    /* ===================== 2. quantos ===================== */

    public record Contagem(String grade, int perfeitos, int tabelado) {

        public boolean bate() {
            return perfeitos == tabelado;
        }
    }

    /**
     * QUANTOS LABIRINTOS PERFEITOS EXISTEM, contra a contagem tabelada.
     *
     * <p>Um labirinto perfeito é uma árvore geradora da grade, e o número de árvores geradoras
     * de uma grade é um resultado conhecido, calculável pelo teorema de Kirchhoff. Para as
     * grades daqui: 4, 15, 192 e 56.
     *
     * <p>Essa é a segunda testemunha mais forte deste repositório: números contados por outras
     * pessoas, por um caminho que não é enumerar.
     */
    public static List<Contagem> quantos() {
        int[] tabeladas = {4, 15, 192, 56};

        List<Contagem> saida = new ArrayList<>();

        for (int indice = 0; indice < GRADES.length; indice++) {
            int[] grade = GRADES[indice];

            saida.add(new Contagem(grade[0] + "x" + grade[1],
                    correcao(grade[0], grade[1]).perfeitos(), tabeladas[indice]));
        }

        return saida;
    }

    /* ===================== 3. maodireita ===================== */

    public record Regra(String destino, int conexos, int falhou) {

        public double taxaDeFalha() {
            return conexos == 0 ? 0 : falhou / (double) conexos;
        }
    }

    private static List<Regra> maoDireita;

    /**
     * A REGRA DA MÃO DIREITA, e a condição que ninguém diz junto.
     *
     * <p>O conselho é encostar a mão numa parede e andar sem tirar. A ressalva famosa fala em
     * ilhas, pedaços de parede soltos no meio. A medida mostra que a ressalva está no lugar
     * errado.
     *
     * <p>Com a saída num CANTO, a regra funcionou em todos os 431 labirintos conexos da grade
     * 3x3, inclusive nos que têm ciclo. Com a saída no MEIO, ela falha. O que importa não é a
     * ilha: é a saída estar ou não na mesma parede que se está seguindo.
     */
    public static List<Regra> maoDireita() {
        if (maoDireita != null) return maoDireita;

        int linhas = 3;
        int colunas = 3;

        long total = Labirinto.quantosLabirintos(linhas, colunas);

        int conexos = 0;
        int falhouNoCanto = 0;
        int falhouNoMeio = 0;

        for (long bits = 0; bits < total; bits++) {
            Labirinto labirinto = new Labirinto(linhas, colunas, (int) bits);

            if (!labirinto.conexo()) continue;

            conexos++;

            if (!chega(labirinto, labirinto.celulas() - 1)) falhouNoCanto++;

            if (!chega(labirinto, 4)) falhouNoMeio++;
        }

        return maoDireita = List.of(new Regra("canto de baixo a direita", conexos, falhouNoCanto),
                new Regra("centro da grade", conexos, falhouNoMeio));
    }

    /** Anda com a mão na direita até um destino qualquer. */
    public static boolean chega(Labirinto labirinto, int destino) {
        int[] deltaLinha = {-1, 0, 1, 0};
        int[] deltaColuna = {0, 1, 0, -1};

        int linha = 0;
        int coluna = 0;
        int rumo = 2;

        int teto = 8 * labirinto.celulas() * labirinto.celulas();

        for (int passo = 0; passo < teto; passo++) {
            boolean andou = false;

            for (int giro = 1; giro >= -2 && !andou; giro--) {
                int tentativa = (rumo + giro + 4) % 4;

                boolean da = switch (tentativa) {
                    case 0 -> linha > 0 && labirinto.passa(linha - 1, coluna, false);
                    case 1 -> labirinto.passa(linha, coluna, true);
                    case 2 -> labirinto.passa(linha, coluna, false);
                    default -> coluna > 0 && labirinto.passa(linha, coluna - 1, true);
                };

                if (!da) continue;

                rumo = tentativa;

                linha += deltaLinha[tentativa];
                coluna += deltaColuna[tentativa];

                andou = true;
            }

            if (!andou) return false;

            if (labirinto.celula(linha, coluna) == destino) return true;
        }

        return false;
    }

    public static Regra maoDireita(String destino) {
        return maoDireita().stream().filter(x -> x.destino().equals(destino))
                .findFirst().orElseThrow();
    }

    /* ===================== 4. geradores ===================== */

    public record Paredes(int derrubadas, int quantos, boolean podeSerPerfeito) {}

    private static List<Paredes> geradores;

    /**
     * QUANTAS PAREDES CAEM, e por que só um número serve.
     *
     * <p>Um labirinto perfeito tem exatamente {@code células - 1} paredes derrubadas. Nem uma
     * a menos, porque aí falta ligação; nem uma a mais, porque aí há ciclo.
     *
     * <p>A tabela mostra a distribuição de todos os 4.096 padrões da grade 3x3 por número de
     * paredes caídas, e que todos os perfeitos estão numa linha só.
     */
    public static List<Paredes> geradores() {
        if (geradores != null) return geradores;

        int[] quantos = new int[13];
        boolean[] temPerfeito = new boolean[13];

        for (long bits = 0; bits < Labirinto.quantosLabirintos(3, 3); bits++) {
            Labirinto labirinto = new Labirinto(3, 3, (int) bits);

            int caidas = labirinto.quantasDerrubadas();

            quantos[caidas]++;

            if (labirinto.perfeito()) temPerfeito[caidas] = true;
        }

        List<Paredes> saida = new ArrayList<>();

        for (int caidas = 0; caidas <= 12; caidas++)
            saida.add(new Paredes(caidas, quantos[caidas], temPerfeito[caidas]));

        return geradores = List.copyOf(saida);
    }

    /* ===================== 5. caminhos ===================== */

    public record Caminho(int labirintos, double passosDaMao, double menorCaminho,
            int piorRazao) {

        public double razaoMedia() {
            return menorCaminho == 0 ? 0 : passosDaMao / menorCaminho;
        }
    }

    private static Caminho caminhos;

    /**
     * QUANTO A MÃO DIREITA ANDA A MAIS.
     *
     * <p>Ela acha a saída, e não acha o caminho curto. A busca em largura serve de referência:
     * ela dá o menor número de passos possível, e a razão entre os dois é o preço de usar uma
     * regra que não enxerga o labirinto.
     */
    public static Caminho caminhos() {
        if (caminhos != null) return caminhos;

        long total = Labirinto.quantosLabirintos(3, 3);

        long somaMao = 0;
        long somaMenor = 0;

        int quantos = 0;
        int piorRazao = 0;

        for (long bits = 0; bits < total; bits++) {
            Labirinto labirinto = new Labirinto(3, 3, (int) bits);

            if (!labirinto.perfeito()) continue;

            MaoDireita.Tentativa tentativa = MaoDireita.atravessar(labirinto);

            int menor = MaoDireita.menorCaminho(labirinto);

            if (!tentativa.chegou() || menor <= 0) continue;

            quantos++;

            somaMao += tentativa.passos();
            somaMenor += menor;

            piorRazao = Math.max(piorRazao, tentativa.passos() / menor);
        }

        return caminhos = new Caminho(quantos, somaMao / (double) quantos,
                somaMenor / (double) quantos, piorRazao);
    }

    /** Um labirinto perfeito da grade 3x3, para a tabela. */
    public static Labirinto exemplo() {
        for (long bits = 0; bits < Labirinto.quantosLabirintos(3, 3); bits++) {
            Labirinto labirinto = new Labirinto(3, 3, (int) bits);

            if (labirinto.perfeito()) return labirinto;
        }

        throw new IllegalStateException("nenhum perfeito, o que seria noticia");
    }
}
