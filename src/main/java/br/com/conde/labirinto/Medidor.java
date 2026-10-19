package br.com.conde.labirinto;

import java.util.Locale;

/** O MEDIDOR: roda as varreduras e imprime as tabelas do README. */
public final class Medidor {

    private Medidor() {}

    public static void main(String[] argumentos) {
        Locale.setDefault(Locale.US);

        String passo = argumentos.length > 0 ? argumentos[0] : "tudo";

        switch (passo) {
            case "correcao" -> correcao();
            case "quantos" -> quantos();
            case "maodireita" -> maodireita();
            case "geradores" -> geradores();
            case "caminhos" -> caminhos();
            default -> {
                correcao();
                quantos();
                maodireita();
                geradores();
                caminhos();
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("=".repeat(texto.length()));
        System.out.println();
    }

    static void correcao() {
        titulo("1. o que cada padrao de parede e");

        System.out.println("grade  paredes  labirintos  conexos  perfeitos  com ciclo  erros");

        for (Medidas.Correcao c : Medidas.correcao())
            System.out.printf("%2dx%d  %7d  %10d  %7d  %9d  %9d  %5d%n", c.linhas(),
                    c.colunas(), c.paredes(), c.labirintos(), c.conexos(), c.perfeitos(),
                    c.comCiclo(), c.erros());

        System.out.println();
        System.out.println("numa grade 3x3 ha 12 paredes internas, entao ha 4.096 labirintos");
        System.out.println("possiveis e todos cabem num laco. nenhum gerador foi usado: o");
        System.out.println("dominio e a lista completa de padroes.");
        System.out.println();
        System.out.println("a ultima coluna e zero: todo perfeito tem exatamente celulas menos");
        System.out.println("uma parede caida, que e o teorema usado na medida seguinte.");
    }

    static void quantos() {
        titulo("2. quantos labirintos perfeitos existem");

        System.out.println("grade  perfeitos  tabelado  bate");

        for (Medidas.Contagem c : Medidas.quantos())
            System.out.printf("%5s  %9d  %8d  %4s%n", c.grade(), c.perfeitos(), c.tabelado(),
                    c.bate() ? "sim" : "NAO");

        System.out.println();
        System.out.println("um labirinto perfeito e uma ARVORE GERADORA da grade, e o numero de");
        System.out.println("arvores geradoras de uma grade e calculavel pelo teorema de");
        System.out.println("kirchhoff, sem enumerar nada.");
        System.out.println();
        System.out.println("sao numeros contados por outras pessoas, por um caminho que nao e o");
        System.out.println("meu. e a testemunha mais forte que este repositorio tem.");

        System.out.println();
        System.out.println("um perfeito da grade 3x3:");
        System.out.println();
        System.out.print(Medidas.exemplo().mostrar());
    }

    static void maodireita() {
        titulo("3. a regra da mao direita, e a condicao que ninguem diz junto");

        System.out.println("destino                     conexos  falhou  taxa");

        for (Medidas.Regra r : Medidas.maoDireita())
            System.out.printf("%-26s  %7d  %6d  %5.1f%%%n", r.destino(), r.conexos(),
                    r.falhou(), 100 * r.taxaDeFalha());

        System.out.println();
        System.out.println("o conselho e encostar a mao numa parede e andar sem tirar. a");
        System.out.println("ressalva famosa fala em ILHAS, pedacos de parede soltos no meio.");
        System.out.println();
        System.out.println("a medida diz que a ressalva esta no lugar errado. com a saida num");
        System.out.println("CANTO a regra funcionou em TODOS os 431 labirintos conexos da grade");
        System.out.println("3x3, inclusive nos 239 que tem ciclo. com a saida no MEIO, ela falha.");
        System.out.println();
        System.out.println("o que importa nao e haver ilha. e a saida estar ou nao na mesma");
        System.out.println("parede que se esta seguindo.");
    }

    static void geradores() {
        titulo("4. quantas paredes caem, e por que so um numero serve");

        System.out.println("paredes caidas  padroes  tem perfeito");

        for (Medidas.Paredes p : Medidas.geradores())
            System.out.printf("%14d  %7d  %12s%n", p.derrubadas(), p.quantos(),
                    p.podeSerPerfeito() ? "sim" : "");

        System.out.println();
        System.out.println("todos os perfeitos estao numa linha so: a de oito paredes caidas,");
        System.out.println("que e nove celulas menos um. nem uma a menos, porque ai falta");
        System.out.println("ligacao; nem uma a mais, porque ai ha ciclo.");
    }

    static void caminhos() {
        titulo("5. quanto a mao direita anda a mais");

        Medidas.Caminho c = Medidas.caminhos();

        System.out.printf("labirintos perfeitos       %d%n", c.labirintos());
        System.out.printf("passos da mao direita      %.2f em media%n", c.passosDaMao());
        System.out.printf("menor caminho possivel     %.2f em media%n", c.menorCaminho());
        System.out.printf("razao                      %.2fx%n", c.razaoMedia());
        System.out.printf("pior razao num labirinto   %dx%n", c.piorRazao());

        System.out.println();
        System.out.println("ela acha a saida, e nao acha o caminho curto. a busca em largura da");
        System.out.println("o menor numero de passos possivel, e a razao entre os dois e o preco");
        System.out.println("de usar uma regra que nao enxerga o labirinto.");
    }
}
