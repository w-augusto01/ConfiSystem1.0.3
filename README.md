# ConfiSystem

Sistema de apoio à gestão de confinamento de bovinos. Telas atuais:

- **Fornecedores** — cadastro de fornecedores/terceiros.
- **Matérias-Primas** — cadastro dos insumos (nome, categoria, unidade, estoque mínimo).
- **Compras** — entrada de insumos: toda mercadoria que chega é lançada aqui.
- **Confinamento** — distribuição dos animais por piquete e controle de tratos (entrada de ração no cocho), com cálculo automático do consumo médio.
- **Formulação de Ração** — monta a receita de uma batida de 1.000 Kg combinando matérias-primas por percentual, calculando Kg, custo e estoque automaticamente.

## Stack

- Java 21
- **Spring Boot 3.3** (contexto/injeção de dependência + Spring Data JPA) — sem servidor web, é um app desktop.
- JavaFX 21 (interface gráfica)
- PostgreSQL

## Estrutura

```
br.com.confisystem
 ├── ConfiSystemApplication   -> inicializa Spring (sem web) + JavaFX
 ├── entity                   -> entidades JPA (Fornecedor, MateriaPrima, Compra, CompraItem, ...)
 ├── repository               -> interfaces Spring Data JPA (substituem os DAOs manuais)
 ├── service                  -> regras de negócio (ex.: rateio de despesas da compra)
 └── controller                -> controllers @FXML, agora Beans do Spring (@Component)
```

## Tela de Compras — como funciona o rateio

Ao lançar uma compra, o frete, o seguro, outras despesas e o desconto da nota
não pertencem a nenhum item específico. O `CompraService` distribui
("rateia") esse valor entre os itens da nota **proporcionalmente ao valor de
produto de cada item**, e calcula automaticamente:

- **Despesas rateadas** do item
- **Custo final** = valor do produto + despesas rateadas
- **Custo por kg/L** = custo final ÷ quantidade

Esses valores aparecem em tempo real na tabela conforme os itens são
adicionados e as despesas gerais são digitadas, e são recalculados de forma
definitiva no momento de salvar.

## Módulo Confinamento — como funciona o consumo médio

Cada **piquete** tem uma quantidade de animais e recebe **tratos** (lançamentos
de ração: data + quantidade em Kg + qual ração). O sistema **não** calcula
consumo por animal — como você não tem como garantir quanto cada cabeça comeu,
isso seria enganoso. Em vez disso:

> Ao lançar um novo trato, o `PiqueteService` olha para o trato anterior
> daquele piquete e calcula: `dias = data do novo trato − data do trato
> anterior`. O **consumo médio do ciclo** é `quantidade do trato anterior ÷
> dias`. Esse é o Kg/dia que aquele piquete consumiu durante aquele intervalo.

Exemplo do seu pedido: trato de 300 Kg em 07/09, próximo trato em 14/09 →
7 dias → consumo médio = 300 ÷ 7 ≈ 42,86 Kg/dia.

## Fórmula → Ordem de Produção → Confinamento (o fluxo completo)

Esse é o encadeamento entre três módulos, pensado para separar "criar a
receita" de "produzir de verdade" de "consumir a ração pronta":

### 1) Formulação de Ração = só a receita (não mexe em estoque)

Você cadastra a **fórmula** (ex.: "Ração Bovino 18%") informando o nome e,
para cada matéria-prima, a quantidade em Kg (o percentual é calculado
sozinho), fechando em 1.000 Kg de referência. Salvar uma fórmula **nunca**
é bloqueado por falta de estoque — é só a receita, você pode querer
cadastrá-la mesmo sem ter as matérias-primas em mãos ainda. As fórmulas já
salvas aparecem na lista lateral da própria tela.

O custo mostrado ali é só uma **estimativa** (baseada no preço da última
compra de cada matéria-prima no momento em que a fórmula foi salva) — não
é o custo definitivo, que só é travado quando a produção é finalizada.

### 2) Ordem de Produção = gera a ordem, imprime, e só consome estoque ao finalizar

Tela nova, separada, só para isso: o operador escolhe a fórmula e quantos
**Kg totais** quer produzir. Como o sistema de produção só comporta
**1.000 Kg por batida**, o pedido é dividido automaticamente:

- Pedir 500 Kg → 1 batida de 500 Kg (a receita inteira dividida por 2).
- Pedir 1.000 Kg → 1 batida de 1.000 Kg (a receita cheia).
- Pedir 3.000 Kg → 3 batidas de 1.000 Kg cada (a receita cheia repetida
  3 vezes) — não existe uma "batida de 3.000 Kg", por isso a divisão.

A ordem nasce com status **PENDENTE** e, nesse momento, **não mexe em
estoque nenhum** — dá pra gerar e imprimir a ordem mesmo sem ter a
matéria-prima ainda. A folha impressa (botão "Ver / Imprimir Ordem", que
abre o diálogo real de impressão do Windows/sistema via
`javafx.print.PrinterJob`) mostra cada matéria-prima com o peso de **uma**
batida e uma coluna "BATIDA" por batida necessária, para o operador marcar
com X conforme vai pesando — igual à planilha de referência que você
mandou.


