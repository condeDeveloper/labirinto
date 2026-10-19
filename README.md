# Labirintos

Labirintos em Java 21, com **todos** os labirintos de grades pequenas enumerados pelos padrões de parede, e a regra da mão direita medida em cada um deles.

A ressalva famosa sobre a regra da mão direita fala em ilhas: pedaços de parede soltos no meio. A medida diz que a ressalva está no lugar errado. Com a saída num canto, a regra funcionou em **todos os 431** labirintos conexos da grade 3x3, inclusive nos 239 que têm ciclo.

```
mvn -q exec:java -Dexec.args=maodireita
```

## O domínio é a lista completa

Numa grade 3x3 há 12 paredes internas, cada uma de pé ou derrubada. Então há 2¹² = **4.096** labirintos possíveis, e todos cabem num laço.

Nenhum gerador de labirinto é usado aqui. Geradores produzem labirintos bons, e produzir labirintos bons esconde exatamente os casos que interessam.

## As medidas

### 1. O que cada padrão de parede é

| grade | paredes | labirintos | conexos | perfeitos | com ciclo | erros |
|---|---:|---:|---:|---:|---:|---:|
| 2x2 | 4 | 16 | 5 | 4 | 1 | 0 |
| 2x3 | 7 | 128 | 23 | 15 | 8 | 0 |
| 3x3 | 12 | 4.096 | 431 | 192 | 239 | 0 |
| 2x4 | 10 | 1.024 | 105 | 56 | 49 | 0 |

**Conexo** é poder ir de qualquer célula a qualquer outra. **Perfeito** é conexo e sem ciclo, ou seja, com um caminho só entre cada par.

Dos 4.096 padrões da grade 3x3, só 431 são labirintos de verdade, e menos da metade desses são perfeitos.

### 2. Quantos labirintos perfeitos existem

| grade | perfeitos | tabelado | bate |
|---|---:|---:|---|
| 2x2 | 4 | 4 | sim |
| 2x3 | 15 | 15 | sim |
| 3x3 | **192** | 192 | sim |
| 2x4 | 56 | 56 | sim |

Um labirinto perfeito é uma **árvore geradora** da grade, e o número de árvores geradoras de uma grade é calculável pelo teorema de Kirchhoff, sem enumerar nada.

São números contados por outras pessoas, por um caminho que não é o meu. É a testemunha mais forte que este repositório tem: ela testa a varredura inteira de uma vez.

### 3. A regra da mão direita, e a condição que ninguém diz junto

| destino | conexos | falhou | taxa |
|---|---:|---:|---:|
| canto de baixo à direita | 431 | **0** | 0,0% |
| centro da grade | 431 | **15** | 3,5% |

O conselho é encostar a mão numa parede e andar sem tirar. A ressalva famosa fala em ilhas.

A medida diz outra coisa. Com a saída num **canto**, a regra funcionou em todos os 431 labirintos conexos, **inclusive nos 239 que têm ciclo**. Com a saída no **meio**, ela falha em 15.

O que importa não é haver ilha. É a saída estar ou não na mesma parede que se está seguindo. Num labirinto com entrada e saída na borda externa, que é o caso normal, a regra é mais robusta do que a fama dela sugere.

### 4. Quantas paredes caem, e por que só um número serve

| paredes caídas | padrões | tem perfeito |
|---:|---:|---|
| 7 | 792 | |
| **8** | **495** | **sim** |
| 9 | 220 | |
| 10 | 66 | |

Todos os 192 perfeitos estão numa linha só: a de oito paredes caídas, que é nove células menos um. Nem uma a menos, porque aí falta ligação; nem uma a mais, porque aí há ciclo.

E a linha de oito tem 495 padrões, dos quais só 192 são perfeitos. O número certo de paredes é necessário e não é suficiente: os outros 303 têm um pedaço solto e um ciclo ao mesmo tempo.

### 5. Quanto a mão direita anda a mais

```
labirintos perfeitos       192
passos da mão direita     7,19 em média
menor caminho possível    4,25 em média
razão                     1,69x
pior razão num labirinto     3x
```

Ela acha a saída, e não acha o caminho curto. A busca em largura dá o menor número de passos possível, e a razão entre os dois é o preço de usar uma regra que não enxerga o labirinto, só a parede ao lado.

## O que as medidas me corrigiram

**A ressalva sobre a mão direita está no lugar errado.** Eu escrevi o repositório esperando contar em quantos labirintos com ciclo a regra falha. A resposta foi zero, em 239. A condição verdadeira não é sobre o labirinto, é sobre onde está a saída: se ela está na borda externa, a regra sempre chega.

**Número certo de paredes não faz labirinto.** Os 495 padrões com oito paredes caídas incluem 303 que não são labirinto nenhum, com um pedaço solto e um ciclo ao mesmo tempo. Contar paredes é condição necessária e vale pouco sozinha.

**A maioria dos padrões não é labirinto.** Dos 4.096, só 431 são conexos. Gerador de labirinto existe para não passar por essa peneira, e por isso ele é a ferramenta errada para estudar o que acontece fora dela.

## Rodar

```bash
mvn -q verify                              # 18 testes
mvn -q exec:java                           # todas as medidas
mvn -q exec:java -Dexec.args=correcao      # 1
mvn -q exec:java -Dexec.args=quantos       # 2
mvn -q exec:java -Dexec.args=maodireita    # 3
mvn -q exec:java -Dexec.args=geradores     # 4
mvn -q exec:java -Dexec.args=caminhos      # 5
```

Java 21, sem dependência fora do JUnit. Nenhum número deste README foi digitado à mão: todos saem de `Medidas`, e os testes conferem os que aparecem escritos aqui.
