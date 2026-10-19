package br.com.conde.labirinto;

/**
 * A REGRA DA MÃO DIREITA: encoste a mão na parede e ande sem tirar.
 *
 * <p>É o conselho que todo mundo já ouviu, e ele funciona, com uma condição que quase nunca é
 * dita junto: só vale quando toda parede do labirinto está ligada à borda.
 *
 * <p>Se existir uma ilha, um pedaço de parede solto no meio, quem começar encostado nela vai
 * dar a volta nela para sempre. A terceira medida conta em quantos labirintos isso acontece.
 *
 * <p>Esta implementação anda célula a célula, mantendo a direção e virando pelo lado direito
 * primeiro, que é a tradução discreta de "mão na parede da direita".
 */
public final class MaoDireita {

    private MaoDireita() {}

    /** Os quatro rumos, em ordem horária: cima, direita, baixo, esquerda. */
    private static final int[] DELTA_LINHA = {-1, 0, 1, 0};
    private static final int[] DELTA_COLUNA = {0, 1, 0, -1};

    /** O que aconteceu ao tentar atravessar. */
    public record Tentativa(boolean chegou, int passos, boolean deuVoltaCompleta) {}

    /**
     * Anda da célula de cima à esquerda até a de baixo à direita, com a mão na direita.
     *
     * <p>O teto de passos não é preguiça: ele é a maneira de detectar que a regra entrou num
     * laço. Sem teto, um labirinto com ilha faria isto rodar para sempre, que é exatamente o
     * defeito que se quer medir.
     */
    public static Tentativa atravessar(Labirinto labirinto) {
        int destino = labirinto.celulas() - 1;

        int linha = 0;
        int coluna = 0;

        int rumo = 2;

        int teto = 8 * labirinto.celulas() * labirinto.celulas();

        for (int passos = 1; passos <= teto; passos++) {
            /* Primeiro tenta virar para a direita do rumo atual; depois seguir reto; depois
             * virar para a esquerda; e por fim voltar. E a ordem que mantem a mao encostada. */
            boolean andou = false;

            for (int giro = 1; giro >= -2 && !andou; giro--) {
                int tentativa = (rumo + giro + 4) % 4;

                if (!daParaIr(labirinto, linha, coluna, tentativa)) continue;

                rumo = tentativa;

                linha += DELTA_LINHA[tentativa];
                coluna += DELTA_COLUNA[tentativa];

                andou = true;
            }

            if (!andou) return new Tentativa(false, passos, false);

            if (labirinto.celula(linha, coluna) == destino)
                return new Tentativa(true, passos, false);
        }

        return new Tentativa(false, teto, true);
    }

    private static boolean daParaIr(Labirinto labirinto, int linha, int coluna, int rumo) {
        return switch (rumo) {
            case 0 -> linha > 0 && labirinto.passa(linha - 1, coluna, false);
            case 1 -> labirinto.passa(linha, coluna, true);
            case 2 -> labirinto.passa(linha, coluna, false);
            default -> coluna > 0 && labirinto.passa(linha, coluna - 1, true);
        };
    }

    /**
     * A BUSCA EM LARGURA, que é o juiz: ela acha o caminho se ele existir.
     *
     * <p>Ela não tem nada em comum com seguir parede. Quando a mão direita falha e esta acha o
     * caminho, a falha é da regra e não do labirinto.
     */
    public static int menorCaminho(Labirinto labirinto) {
        int destino = labirinto.celulas() - 1;

        int[] distancia = new int[labirinto.celulas()];

        java.util.Arrays.fill(distancia, -1);

        java.util.ArrayDeque<Integer> fila = new java.util.ArrayDeque<>();

        distancia[0] = 0;
        fila.add(0);

        while (!fila.isEmpty()) {
            int atual = fila.poll();

            if (atual == destino) return distancia[atual];

            for (int vizinha : labirinto.vizinhas(atual)) {
                if (distancia[vizinha] >= 0) continue;

                distancia[vizinha] = distancia[atual] + 1;

                fila.add(vizinha);
            }
        }

        return -1;
    }
}
