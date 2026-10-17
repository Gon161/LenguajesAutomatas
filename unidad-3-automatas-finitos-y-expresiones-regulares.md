# Unidad 3. Autómatas finitos y expresiones regulares

Un **autómata finito** es la máquina más simple que reconoce un lenguaje: lee una cadena **símbolo por símbolo**, cambia de estado con cada uno y, al terminar, **acepta** o **rechaza**. Solo recuerda "en qué estado está"; no tiene más memoria.

Visto con lo aprendido en la Unidad 1, un autómata finito es un **grafo dirigido etiquetado** con tres añadidos: un nodo marcado como **inicial**, uno o más nodos marcados como **de aceptación** y una regla de lectura de la cadena. Esto es exactamente lo que hacen los analizadores léxicos de un compilador al reconocer palabras reservadas, números e identificadores.

La unidad cierra con las **expresiones regulares**, otra forma —textual— de describir los mismos lenguajes.

> Los ejemplos de esta unidad reutilizan las clases `Estado` y `Transicion` de la Unidad 1 (1.2.1): cada estado es un objeto que guarda la lista de sus transiciones, y cada transición guarda una **referencia** al estado destino. Sobre ellas se construyen las clases `AFD` (3.1) y `AFN` (3.2), que implementan la interfaz `Reconocedor`. Las expresiones regulares usan `java.util.regex.Pattern`.

## 3.1. Autómata Finito Determinista (AFD)

### 3.1.1. Definición formal (estados, alfabeto, transiciones, estado inicial, estados de aceptación)

#### Alfabeto, cadena y lenguaje

Antes del autómata se necesitan tres conceptos:

| Concepto | Definición | Ejemplo |
|---|---|---|
| **Alfabeto** (Σ) | Conjunto finito de símbolos | Σ = {0, 1}; Σ = {a, b} |
| **Cadena** | Secuencia finita de símbolos de Σ | `0110`, `ab` |
| **Cadena vacía** (ε) | Cadena sin símbolos; longitud 0 | ε |
| **Longitud** \|w\| | Número de símbolos de la cadena `w` | \|0110\| = 4 |
| **Concatenación** `uv` | Escribir `v` justo después de `u` | `ab` · `ba` = `abba`; `w` · ε = `w` |
| **Prefijo / sufijo** | Parte inicial / final de una cadena | `01` es prefijo de `0110`; `10` es sufijo |
| **Σ\*** | Todas las cadenas posibles sobre Σ (incluida ε) | {0,1}\* = {ε, 0, 1, 00, 01, 10, 11, 000, …} |
| **Lenguaje** | Cualquier subconjunto de Σ\* | L = {cadenas con número par de 1s} |

Un autómata es la forma de **decidir si una cadena pertenece a un lenguaje**.

Algunos matices que conviene tener claros desde el principio:

- **Σ\* es infinito, pero cada cadena es finita.** Hay cadenas tan largas como se quiera, pero ninguna es infinita.
- **Cuántas cadenas hay de cada longitud.** Con un alfabeto de k símbolos hay kⁿ cadenas de longitud n. Con Σ = {0, 1}: 1 de longitud 0 (ε), 2 de longitud 1, 4 de longitud 2, 8 de longitud 3.
- **∅ no es lo mismo que {ε}.** El lenguaje vacío ∅ no tiene **ninguna** cadena; el lenguaje {ε} tiene **una**: la cadena vacía. Es la misma diferencia que hay entre una caja vacía y una caja que contiene una hoja en blanco.
- **Un lenguaje puede ser infinito** (como el de los 1s pares), aunque el autómata que lo reconoce sea finito. Esa es justamente la gracia: describir infinitas cadenas con una cantidad finita de estados.

#### Definición formal

Un AFD es una quíntupla **M = (Q, Σ, δ, q₀, F)**:

| Componente | Nombre | Significado | En el diagrama |
|---|---|---|---|
| **Q** | Estados | Conjunto finito de estados | Los círculos (nodos) |
| **Σ** | Alfabeto | Símbolos que puede leer | Las etiquetas de las flechas |
| **δ** | Función de transición | `δ: Q × Σ → Q`; dado un estado y un símbolo, devuelve **un solo** estado | Las flechas (aristas etiquetadas) |
| **q₀** | Estado inicial | Donde empieza la lectura; `q₀ ∈ Q` | Flecha de entrada `──▶` |
| **F** | Estados de aceptación | `F ⊆ Q` | Doble círculo |

La palabra **determinista** significa que, estando en un estado y leyendo un símbolo, **hay exactamente un siguiente estado posible**: nunca hay dudas sobre a dónde ir.

| Característica | Descripción |
|---|---|
| Memoria | Solo el estado actual; no guarda los símbolos ya leídos |
| Lectura | De izquierda a derecha, un símbolo por paso, sin regresar |
| Tiempo | Un paso por símbolo: una cadena de longitud n se decide en n pasos |
| Respuesta | Siempre termina, y siempre con un sí o un no |
| Función δ | **Total**: definida para cada par (estado, símbolo) |
| Poder | Reconoce exactamente los lenguajes **regulares** |

**Por qué existe.** Hay muchos problemas en los que, para decidir, no hace falta recordar todo lo leído, sino solo un **resumen**: "¿llevo un número par de 1s?", "¿lo último que leí fue una `a`?". Un AFD es la máquina que guarda solo ese resumen. A cambio de su memoria limitada, es extremadamente rápido y sencillo de programar, y por eso se usa en los analizadores léxicos, en la validación de formatos y en la búsqueda de texto.

**Por qué δ es una función.** Que δ sea una *función* `Q × Σ → Q` dice dos cosas a la vez: que para **cada** par (estado, símbolo) hay un resultado (es total) y que ese resultado es **uno solo** (es determinista). Si a algún par le faltara su resultado o tuviera dos, ya no sería un AFD.

#### Ejemplo: cadenas binarias con un número par de 1s

- Q = {q0, q1}  (q0 = "van un número par de 1s"; q1 = "van un número impar")
- Σ = {0, 1}
- q₀ = q0
- F = {q0}  (el cero es par, por eso ε también se acepta)

Tabla de transición (`→` marca el estado inicial y `*` los de aceptación):

| Estado | 0 | 1 |
|---|---|---|
| → \* q0 | q0 | q1 |
| q1 | q1 | q0 |

Diagrama:

```
   0                          0
  ┌───┐                      ┌───┐
  ▼   │                      ▼   │
 ──▶ ((q0)) ──────── 1 ────▶ (q1)
        ▲                      │
        └──────── 1 ───────────┘
```

Cada estado tiene un **significado** que se puede decir en palabras, y las transiciones respetan ese significado: un `0` no cambia la paridad (lazo); un `1` la invierte (cambia de estado). Diseñar un autómata es, sobre todo, decidir qué significa cada estado.

#### Cómo trabaja: la computación de una cadena

Se parte de `q₀`; por cada símbolo de la cadena se aplica `δ`; al acabar, la cadena se **acepta** si el estado en que se quedó pertenece a `F`.

Cadena `1001` (dos 1s → debe aceptarse):

```
q0 ──1──▶ q1 ──0──▶ q1 ──0──▶ q1 ──1──▶ q0      q0 ∈ F  →  ACEPTADA
```

Cadena `1101` (tres 1s → debe rechazarse):

```
q0 ──1──▶ q1 ──1──▶ q0 ──0──▶ q0 ──1──▶ q1      q1 ∉ F  →  RECHAZADA
```

Este proceso se formaliza con la **función de transición extendida** δ̂, que en lugar de un símbolo recibe una cadena completa. Se define de forma recursiva:

| Caso | Definición | Significa |
|---|---|---|
| Cadena vacía | δ̂(q, ε) = q | Sin leer nada, no se mueve |
| Cadena `wa` (una cadena `w` seguida del símbolo `a`) | δ̂(q, wa) = δ(δ̂(q, w), a) | Primero se lee `w`, y desde donde quedó se aplica δ con `a` |

Cálculo de δ̂(q0, `1001`) desplegando la definición:

| Paso | Expresión | Valor |
|---|---|---|
| 1 | δ̂(q0, ε) | q0 |
| 2 | δ̂(q0, `1`) = δ(q0, 1) | q1 |
| 3 | δ̂(q0, `10`) = δ(q1, 0) | q1 |
| 4 | δ̂(q0, `100`) = δ(q1, 0) | q1 |
| 5 | δ̂(q0, `1001`) = δ(q1, 1) | **q0** |

El **lenguaje del autómata**, L(M), es el conjunto de todas las cadenas que M acepta:

> **L(M) = { w ∈ Σ\* : al leer w desde q₀, M termina en un estado de F }**, es decir, **L(M) = { w : δ̂(q₀, w) ∈ F }**

> **Ojo con la cadena vacía.** ε se acepta **si y solo si el estado inicial es de aceptación** (no se lee ningún símbolo, así que el autómata se queda en `q₀`).

> **Ojo con "pasar" por un estado de aceptación.** Solo importa el estado en que se **termina**. En `1101` el autómata pasa por q0 (de aceptación) a mitad de la lectura, y aun así la cadena se rechaza porque termina en q1.

#### Lenguajes regulares

Un lenguaje es **regular** si existe un AFD que lo acepta. Más adelante (3.3) se verá que son exactamente los lenguajes que describen las expresiones regulares.

No todos los lenguajes son regulares. Un AFD tiene una cantidad **fija** de estados, así que solo puede distinguir esa cantidad de "situaciones". Un lenguaje que exija recordar una cantidad que crece sin límite, como `aⁿbⁿ` (contar cuántas `a` hubo para compararlas con las `b`), no cabe en ningún AFD. Ese límite es el punto de partida de la Unidad 4.

#### El AFD en Java

Primero se define una interfaz para todo lo que sabe decidir si una cadena pertenece a un lenguaje. El AFD la implementa ahora; el AFN de 3.2 y el autómata de pila de la Unidad 4 también lo harán, y así se podrán comparar entre sí (3.3.2):

```java
// Cualquier objeto que sabe decidir si una cadena pertenece a su lenguaje
public interface Reconocedor {
    boolean acepta(String cadena);
}
```

El AFD es un conjunto de objetos `Estado` enlazados por sus transiciones (Unidad 1), más una referencia al estado inicial. La función δ no se guarda como una tabla aparte: **está repartida** entre los estados, porque cada uno conoce sus propias flechas.

```java
import java.util.ArrayList;

public class AFD implements Reconocedor {

    private String alfabeto = "";
    private ArrayList<Estado> estados = new ArrayList<Estado>();
    private Estado inicial = null;

    public AFD(String alfabeto) {
        this.alfabeto = alfabeto;
    }

    // El primer estado que se agrega queda como estado inicial
    public Estado agregarEstado(String nombre, boolean aceptacion) {
        Estado nuevo = new Estado(nombre, aceptacion);
        estados.add(nuevo);
        if (inicial == null) inicial = nuevo;
        return nuevo;
    }

    public Estado buscar(String nombre) {
        for (Estado e : estados)
            if (e.getNombre().equals(nombre)) return e;
        return null;
    }

    public void agregarTransicion(String desde, char simbolo, String hasta) {
        buscar(desde).conectar(String.valueOf(simbolo), buscar(hasta));
    }

    public boolean acepta(String cadena) {
        return acepta(cadena, false);
    }

    public boolean acepta(String cadena, boolean traza) {
        Estado actual = inicial;

        for (int i = 0; i < cadena.length(); i++) {
            String simbolo = String.valueOf(cadena.charAt(i));
            Estado siguiente = actual.siguiente(simbolo);

            if (siguiente == null) return false;      // transición no definida: se rechaza

            if (traza) System.out.println("  δ(" + actual.getNombre() + ", " + simbolo + ") = " + siguiente.getNombre());
            actual = siguiente;
        }

        return actual.esAceptacion();
    }

    public void imprimir() {
        for (Estado q : estados) {
            String marca = (q == inicial ? "->" : "  ") + (q.esAceptacion() ? "*" : " ");
            String fila = "";

            for (int i = 0; i < alfabeto.length(); i++) {
                String c = String.valueOf(alfabeto.charAt(i));
                Estado destino = q.siguiente(c);
                fila += "   " + c + ":" + (destino == null ? "-" : destino.getNombre());
            }

            System.out.printf("%s %-10s%s%n", marca, q.getNombre(), fila);
        }
    }
}
```

| Parte de la quíntupla | Dónde vive en el código |
|---|---|
| Q | La lista `estados` |
| Σ | El texto `alfabeto` |
| δ | Las listas de `Transicion` de cada `Estado` (se consultan con `siguiente`) |
| q₀ | La referencia `inicial` |
| F | El atributo `aceptacion` de cada `Estado` |

Y el autómata de los 1s pares:

```java
AFD pares = new AFD("01");
pares.agregarEstado("q0", true);      // inicial y de aceptación
pares.agregarEstado("q1", false);
pares.agregarTransicion("q0", '0', "q0");
pares.agregarTransicion("q0", '1', "q1");
pares.agregarTransicion("q1", '0', "q1");
pares.agregarTransicion("q1", '1', "q0");

System.out.println(pares.acepta("1001"));   // → true
System.out.println(pares.acepta("1101"));   // → false
System.out.println(pares.acepta(""));       // → true    (ε: q0 es de aceptación)
System.out.println(pares.acepta("0000"));   // → true    (cero 1s)
System.out.println(pares.acepta("1201"));   // → false   ('2' no está en el alfabeto: no hay flecha)

pares.acepta("1101", true);
// →   δ(q0, 1) = q1
// →   δ(q1, 1) = q0
// →   δ(q0, 0) = q0
// →   δ(q0, 1) = q1

pares.imprimir();
// → ->* q0           0:q0   1:q1
// →     q1           0:q1   1:q0
```

**Qué pasa por dentro de `acepta`.** La variable `actual` es una referencia que **salta de objeto en objeto** siguiendo las flechas, igual que el dedo que recorre el diagrama. No se crean estados ni se copian: el autómata se recorre, no se modifica. Por eso se puede llamar a `acepta` muchas veces con cadenas distintas sobre el mismo objeto.

**Costo.** El ciclo da una vuelta por símbolo, así que son **n pasos** para una cadena de longitud n. En cada paso, `siguiente` revisa la lista de transiciones del estado (a lo más una por símbolo del alfabeto). La memoria que usa no depende del largo de la cadena: siempre es una sola referencia, `actual`. Esa memoria constante es, en código, lo que significa "solo recuerda en qué estado está".

> **Ojo con los símbolos fuera del alfabeto.** Con `"1201"` no hay flecha para `'2'`, `siguiente` devuelve `null` y la cadena se rechaza. Es el comportamiento correcto: una cadena con símbolos que no están en Σ no pertenece a ningún lenguaje sobre Σ.

### 3.1.2. Construcción de AFD a partir de casos prácticos

Diseñar un AFD es **decidir qué hay que recordar** de lo que ya se leyó. Cada cosa distinta que se necesite recordar es un estado.

Una forma precisa de pensarlo: dos prefijos distintos deben llevar al **mismo** estado si, para cualquier continuación que se les agregue, los dos dan la misma respuesta (ambos se aceptan o ambos se rechazan). Si existe alguna continuación que los distingue, necesitan estados **distintos**. En el lenguaje de los 1s pares, `11` y `0` llevan al mismo estado (ambos tienen paridad par), mientras que `1` y `0` no: la continuación ε acepta a `0` y rechaza a `1`.

#### Método

| Paso | Qué hacer |
|---|---|
| 1 | Fijar el **alfabeto** |
| 2 | Preguntarse: *¿qué información del pasado necesito para decidir al final?* Cada respuesta posible es un **estado** |
| 3 | Marcar el **estado inicial** (lo que se sabe antes de leer nada) |
| 4 | Marcar los **estados de aceptación** (las situaciones en que la cadena cumple lo pedido) |
| 5 | Definir **una transición por cada par (estado, símbolo)**; si algún caso "ya no puede cumplir", se manda a un **estado trampa** |
| 6 | **Probar** con cadenas que deben aceptarse, que deben rechazarse y con ε |

> **Estado trampa** (o *sumidero*): estado del que ya no se puede salir hacia la aceptación; todas sus transiciones regresan a él. Sirve para completar la función δ.

Para el paso 6 conviene probar con varias categorías de cadenas, no solo con las obvias:

| Categoría | Qué revela | Ejemplo para "terminan en `ab`" |
|---|---|---|
| Normales que se aceptan | Que el camino principal funciona | `ab`, `aab` |
| Normales que se rechazan | Que no se acepta de más | `abb`, `ba` |
| Cadena vacía | Si el estado inicial está bien marcado | ε |
| Longitud 1 | Casos que se saltan el patrón | `a`, `b` |
| Patrón que se interrumpe y se retoma | Que el autómata se "recupera" bien | `abab`, `bab` |

#### Caso 1: cadenas sobre {a, b} que terminan en `ab`

Lo que hay que recordar es **cuánto del final `ab` se ha completado**:

| Estado | Qué recuerda |
|---|---|
| q0 | Nada útil (o acaba de llegar una `b` que no sirve) |
| q1 | El último símbolo fue `a` (podría empezar `ab`) |
| q2 | Los dos últimos símbolos fueron `ab` (**aceptación**) |

| Estado | a | b |
|---|---|---|
| → q0 | q1 | q0 |
| q1 | q1 | q2 |
| \* q2 | q1 | q0 |

```
        b                 a
       ┌───┐             ┌───┐
       ▼   │             ▼   │
  ──▶ (q0) ──── a ────▶ (q1) ──── b ────▶ ((q2))
       ▲                 ▲                  │ │
       │                 └────── a ─────────┘ │
       └───────────────── b ──────────────────┘
```

Lectura: desde q1 (llevo una `a`), otra `a` me deja en q1; una `b` completa `ab` y paso a q2. Desde q2, una `a` reinicia la búsqueda en q1 y una `b` la deshace (q0).

La transición más delicada es `δ(q1, a) = q1`: después de `aa`, lo último que se leyó sigue siendo una `a`, así que se sigue "llevando una `a`". Un error frecuente es mandarla a q0, y entonces `aab` se rechazaría.

```java
AFD terminaEnAb = new AFD("ab");
terminaEnAb.agregarEstado("q0", false);
terminaEnAb.agregarEstado("q1", false);
terminaEnAb.agregarEstado("q2", true);
terminaEnAb.agregarTransicion("q0", 'a', "q1");
terminaEnAb.agregarTransicion("q0", 'b', "q0");
terminaEnAb.agregarTransicion("q1", 'a', "q1");
terminaEnAb.agregarTransicion("q1", 'b', "q2");
terminaEnAb.agregarTransicion("q2", 'a', "q1");
terminaEnAb.agregarTransicion("q2", 'b', "q0");

String[] pruebas = { "ab", "aab", "abb", "bab", "b", "" };
for (String w : pruebas)
    System.out.printf("%-4s -> %b%n", w.isEmpty() ? "ε" : w, terminaEnAb.acepta(w));

// → ab   -> true
// → aab  -> true
// → abb  -> false
// → bab  -> true
// → b    -> false
// → ε    -> false
```

Con `abab` se ve cómo el autómata sale de la aceptación y vuelve a entrar:

```java
terminaEnAb.acepta("abab", true);
// →   δ(q0, a) = q1
// →   δ(q1, b) = q2
// →   δ(q2, a) = q1
// →   δ(q1, b) = q2
```

Termina en q2: `abab` se acepta. A mitad de la lectura (después de `ab`) ya estaba en q2, salió con la `a` y regresó con la última `b`.

#### Caso 2: cadenas binarias que **empiezan** con `01`

Aquí un error en los dos primeros símbolos ya no tiene remedio, así que hace falta un **estado trampa**:

| Estado | Qué recuerda |
|---|---|
| q0 | Aún no se ha leído nada |
| q1 | Se leyó `0` (voy bien) |
| q2 | Se leyó `01` (**aceptación**; lo que siga ya no importa) |
| trampa | El inicio fue incorrecto |

| Estado | 0 | 1 |
|---|---|---|
| → q0 | q1 | trampa |
| q1 | trampa | q2 |
| \* q2 | q2 | q2 |
| trampa | trampa | trampa |

La diferencia con el caso 1 es de fondo: "terminar en" se decide **al final** y siempre se puede corregir con lo que venga después; "empezar con" se decide **al principio** y una vez decidido ya no cambia. Por eso q2 y trampa son estados de los que nunca se sale: q2 acepta todo lo que siga y trampa rechaza todo lo que siga.

```java
AFD empiezaCon01 = new AFD("01");
empiezaCon01.agregarEstado("q0", false);
empiezaCon01.agregarEstado("q1", false);
empiezaCon01.agregarEstado("q2", true);
empiezaCon01.agregarEstado("trampa", false);
empiezaCon01.agregarTransicion("q0", '0', "q1");
empiezaCon01.agregarTransicion("q0", '1', "trampa");
empiezaCon01.agregarTransicion("q1", '0', "trampa");
empiezaCon01.agregarTransicion("q1", '1', "q2");
empiezaCon01.agregarTransicion("q2", '0', "q2");
empiezaCon01.agregarTransicion("q2", '1', "q2");
empiezaCon01.agregarTransicion("trampa", '0', "trampa");
empiezaCon01.agregarTransicion("trampa", '1', "trampa");

System.out.println(empiezaCon01.acepta("01"));      // → true
System.out.println(empiezaCon01.acepta("0111"));    // → true
System.out.println(empiezaCon01.acepta("10"));      // → false
System.out.println(empiezaCon01.acepta("00"));      // → false
System.out.println(empiezaCon01.acepta("0"));       // → false   (se queda en q1: le falta el 1)
```

> **Ojo con δ incompleta.** Un AFD debe tener una transición por cada símbolo en cada estado. Si falta alguna, la función δ está incompleta. La clase `AFD` de arriba rechaza la cadena cuando no encuentra la transición (`siguiente` devuelve `null`), que equivale a mandarla a un estado trampa implícito; en un examen o diagrama formal, la trampa se dibuja.

| | Trampa dibujada | Trampa implícita (transición faltante) |
|---|---|---|
| ¿Es un AFD formal? | Sí: δ es total | No estrictamente: δ es parcial |
| Resultado para la cadena | Rechazada | Rechazada |
| Cuándo deja de leer | Lee hasta el final, dando vueltas en la trampa | Se detiene en cuanto falta la flecha |
| Dónde se usa | Exámenes, demostraciones, conversión AFN → AFD | Programas, diagramas simplificados |

#### Caso 3: identificadores de un lenguaje de programación

El caso real de los analizadores léxicos: un **identificador** empieza con letra (o guion bajo) y sigue con letras o dígitos. Para no escribir una transición por cada letra, los caracteres se **clasifican** en tres símbolos: `L` (letra o `_`), `D` (dígito) y `O` (otro).

```
                              L, D
                             ┌───┐
                             ▼   │
 ──▶ (inicio) ──── L ────▶ ((ident))
        │                     │
       D, O                   O
        │                     │
        └────▶ (error) ◀──────┘        (desde error, L, D y O vuelven a error)
```

| Estado | L | D | O |
|---|---|---|---|
| → inicio | ident | error | error |
| \* ident | ident | ident | error |
| error | error | error | error |

**Por qué clasificar.** Sin clasificación, el alfabeto serían todas las letras, dígitos y signos: más de 60 símbolos, y cada estado necesitaría más de 60 flechas. Agrupando los símbolos que el autómata trata **igual**, el alfabeto baja a 3 y la tabla a 9 casillas. Es la misma técnica que usan los analizadores léxicos reales: primero se clasifica cada carácter y luego se aplica el autómata sobre las clases.

```java
static char clasificar(char c) {
    if (Character.isLetter(c) || c == '_') return 'L';
    if (Character.isDigit(c)) return 'D';
    return 'O';
}

static String simbolos(String texto) {
    String resultado = "";
    for (int i = 0; i < texto.length(); i++)
        resultado += clasificar(texto.charAt(i));
    return resultado;
}
```

```java
AFD identificador = new AFD("LDO");
identificador.agregarEstado("inicio", false);
identificador.agregarEstado("ident", true);
identificador.agregarEstado("error", false);
identificador.agregarTransicion("inicio", 'L', "ident");
identificador.agregarTransicion("inicio", 'D', "error");
identificador.agregarTransicion("inicio", 'O', "error");
identificador.agregarTransicion("ident", 'L', "ident");
identificador.agregarTransicion("ident", 'D', "ident");
identificador.agregarTransicion("ident", 'O', "error");
identificador.agregarTransicion("error", 'L', "error");
identificador.agregarTransicion("error", 'D', "error");
identificador.agregarTransicion("error", 'O', "error");

String[] textos = { "total", "x1", "_dato", "1x", "a-b" };
for (String texto : textos) {
    boolean valido = identificador.acepta(simbolos(texto));
    System.out.printf("%-6s (%-5s) -> %s%n", texto, simbolos(texto),
                      valido ? "identificador válido" : "no válido");
}

// → total  (LLLLL) -> identificador válido
// → x1     (LD   ) -> identificador válido
// → _dato  (LLLLL) -> identificador válido
// → 1x     (DL   ) -> no válido
// → a-b    (LOL  ) -> no válido
```

> **Ojo con `Character.isLetter`.** También devuelve `true` para letras con acento y de otros alfabetos (`á`, `ñ`, `λ`). Para este ejemplo da igual, pero un lenguaje de programación que solo acepte letras del inglés tendría que clasificar con `(c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')`.

---

## 3.2. Autómata Finito No Determinista (AFN)

### 3.2.1. Definición y diferencias frente al AFD

Un **AFN** tiene la misma estructura que un AFD, pero la función de transición **puede ofrecer varias opciones** (o ninguna) para un mismo estado y símbolo:

> **N = (Q, Σ, δ, q₀, F)**, con **δ: Q × Σ → P(Q)**

`P(Q)` es el conjunto de **subconjuntos** de Q: `δ(q, a)` devuelve un **conjunto de estados** (que puede estar vacío). Si Q tiene 3 estados, P(Q) tiene 2³ = 8 subconjuntos posibles, desde ∅ hasta Q completo.

| Característica | AFD | AFN |
|---|---|---|
| Transiciones por (estado, símbolo) | **Exactamente una** | **Cero, una o varias** |
| Camino para una cadena | Único | Puede haber varios (o ninguno) |
| Cuándo acepta | El camino único termina en un estado de `F` | **Al menos un** camino termina en un estado de `F` |
| Estado trampa | Necesario para completar δ | No hace falta: si no hay transición, ese camino simplemente muere |
| Diagrama | Más grande, lleno de flechas | Normalmente más pequeño y fácil de leer |
| Simulación | Una referencia al estado actual | Un **conjunto** de estados actuales |

> Todo AFD es, en particular, un AFN (con un solo destino por transición). Lo sorprendente —y lo que se prueba en 3.2.2— es lo contrario: **todo AFN se puede convertir en un AFD equivalente**. El no determinismo hace más cómodo el diseño, pero **no reconoce más lenguajes**.

**Por qué existe.** Hay lenguajes que se describen de forma natural con una "apuesta": "en algún momento empieza el final `ab`". Un AFD tiene que llevar la cuenta de todas las posibilidades a la vez en sus estados, lo que vuelve el diseño más difícil. El AFN permite escribir la idea directamente y deja para después (3.2.2) el trabajo de convertirla en un AFD.

Hay dos formas equivalentes de imaginar cómo funciona:

| Interpretación | Idea | Cuándo ayuda |
|---|---|---|
| **Adivinar** | En cada bifurcación, el autómata "elige bien" si existe una elección que lleva a aceptar | Para **diseñar**: se escribe la apuesta correcta |
| **Clonarse** | En cada bifurcación, el autómata se copia y sigue todos los caminos a la vez | Para **simular** y programar |

> **Ojo con "no determinista".** No significa "aleatorio". Un AFN no lanza una moneda: su respuesta está perfectamente definida (acepta si **existe** un camino a la aceptación). Con la misma cadena siempre da el mismo resultado.

#### Ejemplo: cadenas sobre {a, b} que terminan en `ab`

El AFD del caso 1 necesitaba 3 estados y 6 flechas. Con un AFN basta con "adivinar" cuándo empieza el final `ab`:

- q0 se queda leyendo cualquier símbolo, **y además** con una `a` puede saltar a q1.
- q1, con una `b`, pasa a q2 (aceptación).

| Estado | a | b |
|---|---|---|
| → q0 | {q0, q1} | {q0} |
| q1 | ∅ | {q2} |
| \* q2 | ∅ | ∅ |

```
       a, b
       ┌───┐
       ▼   │
  ──▶ (q0) ──── a ────▶ (q1) ──── b ────▶ ((q2))
```

En q0 con una `a` hay **dos** opciones (quedarse o ir a q1): ahí está el no determinismo.

Los caminos posibles para la cadena `aab` forman un árbol de computaciones; basta con que **una** rama termine en aceptación:

```
 q0 ─a─▶ q0 ─a─▶ q0 ─b─▶ q0          termina en q0: esta rama no acepta
 │       │
 │       └─a─▶ q1 ─b─▶ q2            termina en q2: ¡ACEPTA!
 │
 └─a─▶ q1 ─a─▶ (no hay flecha)       esta rama muere
```

#### Cómo se calcula la aceptación

Se sigue **a la vez** todo el conjunto de estados en que podría estar el autómata:

Cadena `aab`:

| Símbolo leído | Estados posibles |
|---|---|
| *(inicio)* | {q0} |
| `a` | {q0, q1} |
| `a` | {q0, q1} ∪ ∅ = {q0, q1} |
| `b` | δ(q0,b) ∪ δ(q1,b) = {q0} ∪ {q2} = {q0, q2} |

Entre {q0, q2} hay un estado de aceptación (q2): **se acepta**.

Cadena `aba`:

| Símbolo leído | Estados posibles |
|---|---|
| *(inicio)* | {q0} |
| `a` | {q0, q1} |
| `b` | {q0, q2} |
| `a` | δ(q0,a) ∪ δ(q2,a) = {q0, q1} ∪ ∅ = {q0, q1} |

Ni q0 ni q1 son de aceptación: **se rechaza** (la cadena termina en `a`).

Seguir **conjuntos** en lugar de caminos individuales es lo que mantiene la simulación eficiente. El número de caminos puede crecer muchísimo (en cada `a` los caminos se duplican), pero el conjunto nunca tiene más elementos que estados tiene el autómata: aquí, a lo más 3. Dos caminos que llegan al mismo estado ya no se distinguen, porque su futuro es idéntico.

#### El AFN en Java

Un AFN usa los mismos objetos `Estado` de la Unidad 1; la única diferencia es que ahora un estado **puede** tener varias transiciones con el mismo símbolo. Se agrega a la clase `Estado` un método que las devuelve todas:

```java
// Se agrega a la clase Estado: todos los estados a los que lleva el símbolo
public ArrayList<Estado> siguientes(String simbolo) {
    ArrayList<Estado> destinos = new ArrayList<Estado>();
    for (Transicion t : transiciones)
        if (t.getSimbolo().equals(simbolo)) destinos.add(t.getDestino());
    return destinos;
}
```

La simulación consiste en mantener el **conjunto** de estados actuales y reemplazarlo, símbolo a símbolo, por la unión de los destinos (método `mover`):

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class AFN implements Reconocedor {

    private String alfabeto = "";
    private ArrayList<Estado> estados = new ArrayList<Estado>();
    private Estado inicial = null;

    public AFN(String alfabeto) {
        this.alfabeto = alfabeto;
    }

    public Estado agregarEstado(String nombre, boolean aceptacion) {
        Estado nuevo = new Estado(nombre, aceptacion);
        estados.add(nuevo);
        if (inicial == null) inicial = nuevo;
        return nuevo;
    }

    public Estado buscar(String nombre) {
        for (Estado e : estados)
            if (e.getNombre().equals(nombre)) return e;
        return null;
    }

    public void agregarTransicion(String desde, char simbolo, String hasta) {
        buscar(desde).conectar(String.valueOf(simbolo), buscar(hasta));
    }

    // Nombre de un conjunto de estados, ordenado: {q0, q1}
    public static String nombre(ArrayList<Estado> conjunto) {
        ArrayList<String> nombres = new ArrayList<String>();
        for (Estado e : conjunto) nombres.add(e.getNombre());
        Collections.sort(nombres);
        return "{" + String.join(", ", nombres) + "}";
    }

    private static boolean tieneAceptacion(ArrayList<Estado> conjunto) {
        for (Estado e : conjunto)
            if (e.esAceptacion()) return true;
        return false;
    }

    // Unión de los destinos de todos los estados del conjunto con ese símbolo
    private static ArrayList<Estado> mover(ArrayList<Estado> conjunto, String simbolo) {
        ArrayList<Estado> resultado = new ArrayList<Estado>();
        for (Estado q : conjunto)
            for (Estado destino : q.siguientes(simbolo))
                if (!resultado.contains(destino)) resultado.add(destino);   // sin repetidos
        return resultado;
    }

    public boolean acepta(String cadena) {
        return acepta(cadena, false);
    }

    public boolean acepta(String cadena, boolean traza) {
        ArrayList<Estado> actuales = new ArrayList<Estado>();
        actuales.add(inicial);

        for (int i = 0; i < cadena.length(); i++) {
            char simbolo = cadena.charAt(i);
            actuales = mover(actuales, String.valueOf(simbolo));
            if (traza) System.out.println("  " + simbolo + " -> " + nombre(actuales));
        }

        return tieneAceptacion(actuales);              // ¿hay al menos un estado de aceptación?
    }
}
```

```java
AFN afn = new AFN("ab");
afn.agregarEstado("q0", false);
afn.agregarEstado("q1", false);
afn.agregarEstado("q2", true);
afn.agregarTransicion("q0", 'a', "q0");
afn.agregarTransicion("q0", 'a', "q1");     // dos flechas con 'a' desde q0
afn.agregarTransicion("q0", 'b', "q0");
afn.agregarTransicion("q1", 'b', "q2");

System.out.println(afn.acepta("aab"));    // → true
System.out.println(afn.acepta("aba"));    // → false

afn.acepta("aab", true);
// →   a -> {q0, q1}
// →   a -> {q0, q1}
// →   b -> {q0, q2}
```

Los pares (estado, símbolo) que no tienen flecha devuelven una lista vacía en `siguientes`: ese camino muere, sin error y sin necesidad de trampa.

| Detalle de `mover` | Por qué |
|---|---|
| `if (!resultado.contains(destino))` | El resultado es un **conjunto**: si q0 y q1 llevaran ambos a q2, q2 debe aparecer una sola vez. Sin esta revisión la lista crecería con repetidos en cada paso |
| `contains` compara referencias | `Estado` no redefine `equals`, así que dos entradas son "iguales" solo si son el mismo objeto, que es justo lo que se quiere |
| `nombre` ordena los nombres | `{q1, q0}` y `{q0, q1}` son el mismo conjunto; ordenarlos da siempre el mismo texto, y eso importa en 3.2.2 |

> **Ojo con usar `siguiente` (singular) en un AFN.** `siguiente` devuelve solo la **primera** flecha que encuentra con ese símbolo. En el AFN de arriba, `q0.siguiente("a")` daría q0 y nunca exploraría el salto a q1, así que `aab` se rechazaría. Para un AFN siempre hay que seguir **todas** las flechas.

### 3.2.2. Conversión de AFN a AFD

Si se sigue el conjunto de estados posibles (como en la tabla anterior), **cada conjunto puede verse como un único estado de un AFD**. Esa es la idea de la **construcción de subconjuntos**: el AFD resultante tiene como estados los **subconjuntos** de estados del AFN.

**Por qué funciona.** Al simular el AFN, en cada paso el conjunto siguiente depende **solo** del conjunto actual y del símbolo leído. Eso es exactamente lo que hace una función de transición determinista: de un "estado" (el conjunto) y un símbolo, sale un único "estado" (el nuevo conjunto). El AFD no adivina nada: hace por adelantado, y una sola vez, el trabajo que el simulador del AFN repite con cada cadena.

#### Algoritmo

| Paso | Qué hacer |
|---|---|
| 1 | El estado inicial del AFD es el conjunto `{q₀}` |
| 2 | Para cada conjunto `S` pendiente y cada símbolo `a`: `δ_D(S, a) = ⋃ δ_N(q, a)` para todo `q ∈ S` |
| 3 | Si el conjunto obtenido es nuevo, se agrega a los pendientes |
| 4 | Un conjunto es **de aceptación** si **contiene al menos un estado de aceptación** del AFN |
| 5 | Se termina cuando no quedan conjuntos pendientes |

Con `n` estados en el AFN, el AFD tiene **a lo más 2ⁿ** estados (los subconjuntos posibles), aunque en la práctica casi siempre son bastantes menos, porque solo se construyen los que son **alcanzables** (idea de la Unidad 1).

| Estados del AFN | Máximo de estados del AFD (2ⁿ) |
|---|---|
| 3 | 8 |
| 5 | 32 |
| 10 | 1 024 |
| 20 | 1 048 576 |

El máximo no es solo teórico: hay lenguajes en los que se alcanza. Por ejemplo, "cadenas sobre {a, b} cuyo **tercer símbolo contando desde el final** es `a`" se describe con un AFN de 4 estados, pero cualquier AFD que lo reconozca necesita al menos 2³ = 8, porque tiene que recordar los últimos tres símbolos leídos. Es el precio de pasar de la comodidad del AFN a la rapidez del AFD.

#### Ejemplo: el AFN de `terminan en ab`

| Conjunto | a | b |
|---|---|---|
| → {q0} | δ(q0,a) = **{q0, q1}** | δ(q0,b) = **{q0}** |
| {q0, q1} | δ(q0,a) ∪ δ(q1,a) = {q0, q1} ∪ ∅ = **{q0, q1}** | δ(q0,b) ∪ δ(q1,b) = {q0} ∪ {q2} = **{q0, q2}** |
| \* {q0, q2} | δ(q0,a) ∪ δ(q2,a) = {q0, q1} ∪ ∅ = **{q0, q1}** | δ(q0,b) ∪ δ(q2,b) = {q0} ∪ ∅ = **{q0}** |

`{q0, q2}` es de aceptación porque contiene a q2. Aparecen solo **tres** conjuntos de los 8 posibles, y si se renombran `{q0} = A`, `{q0,q1} = B` y `{q0,q2} = C`, la tabla es **idéntica a la del AFD diseñado a mano en el caso 1**.

Orden en que el algoritmo descubre los conjuntos (la lista de pendientes funciona como cola):

| Vuelta | Sale de pendientes | Con `a` | Con `b` | Nuevos que entran | Pendientes después |
|---|---|---|---|---|---|
| 0 | — | — | — | {q0} | [{q0}] |
| 1 | {q0} | {q0, q1} (nuevo) | {q0} (ya existe) | {q0, q1} | [{q0, q1}] |
| 2 | {q0, q1} | {q0, q1} (ya existe) | {q0, q2} (nuevo) | {q0, q2} | [{q0, q2}] |
| 3 | {q0, q2} | {q0, q1} (ya existe) | {q0} (ya existe) | — | [ ] → fin |

#### La conversión en Java

Se agrega este método a la clase `AFN`. Construye el AFD con la clase de 3.1.1: cada conjunto se convierte en un `Estado` cuyo nombre es el texto del conjunto (`"{q0, q1}"`). Los conjuntos pendientes se guardan en una `LinkedList` usada como cola:

```java
public AFD convertirAAFD() {
    AFD afd = new AFD(alfabeto);
    LinkedList<ArrayList<Estado>> pendientes = new LinkedList<ArrayList<Estado>>();

    ArrayList<Estado> inicio = new ArrayList<Estado>();
    inicio.add(inicial);
    afd.agregarEstado(nombre(inicio), tieneAceptacion(inicio));
    pendientes.addLast(inicio);

    while (!pendientes.isEmpty()) {
        ArrayList<Estado> conjunto = pendientes.removeFirst();

        for (int i = 0; i < alfabeto.length(); i++) {
            char simbolo = alfabeto.charAt(i);
            ArrayList<Estado> destino = mover(conjunto, String.valueOf(simbolo));
            String nombreDestino = nombre(destino);

            if (afd.buscar(nombreDestino) == null) {           // conjunto nuevo
                afd.agregarEstado(nombreDestino, tieneAceptacion(destino));
                pendientes.addLast(destino);
            }

            afd.agregarTransicion(nombre(conjunto), simbolo, nombreDestino);
        }
    }

    return afd;
}
```

```java
AFD convertido = afn.convertirAAFD();
convertido.imprimir();

// → ->  {q0}         a:{q0, q1}   b:{q0}
// →     {q0, q1}     a:{q0, q1}   b:{q0, q2}
// →   * {q0, q2}     a:{q0, q1}   b:{q0}

System.out.println(convertido.acepta("aab"));   // → true
System.out.println(convertido.acepta("aba"));   // → false
```

**Por qué el nombre ordenado es indispensable.** `afd.buscar(nombreDestino)` decide si un conjunto es nuevo comparando **textos**. Si el mismo conjunto pudiera escribirse como `{q0, q1}` en una vuelta y `{q1, q0}` en otra, el algoritmo creería que son distintos, crearía estados duplicados y, en el peor caso, no terminaría nunca. Ordenar los nombres garantiza que cada conjunto tenga una única representación.

Si en algún paso el conjunto destino queda vacío (`{}`), ese conjunto es justamente el **estado trampa** del AFD: es el que completa la función δ de lo que en el AFN eran caminos que morían.

Por ejemplo, un AFN para "empiezan con `01`" no necesita trampa (p0 ─0▶ p1 ─1▶ p2, y p2 con lazo `0, 1`), pero al convertirlo aparece sola:

```java
AFN empieza = new AFN("01");
empieza.agregarEstado("p0", false);
empieza.agregarEstado("p1", false);
empieza.agregarEstado("p2", true);
empieza.agregarTransicion("p0", '0', "p1");
empieza.agregarTransicion("p1", '1', "p2");
empieza.agregarTransicion("p2", '0', "p2");
empieza.agregarTransicion("p2", '1', "p2");

empieza.convertirAAFD().imprimir();
// → ->  {p0}         0:{p1}   1:{}
// →     {p1}         0:{}   1:{p2}
// →     {}           0:{}   1:{}
// →   * {p2}         0:{p2}   1:{p2}
```

El conjunto `{}` tiene todas sus flechas hacia sí mismo y no es de aceptación: es el estado `trampa` del caso 2 de 3.1.2, construido automáticamente.

> **Ojo con el estado de aceptación del AFD.** Un error frecuente al hacer la conversión a mano: olvidar que el estado de aceptación del AFD es **todo conjunto que contenga** un estado de aceptación del AFN, no solo el conjunto formado únicamente por él.

---

## 3.3. Expresiones regulares

### 3.3.1. Sintaxis y semántica de expresiones regulares

Una **expresión regular** (ER) es una **fórmula** que describe un lenguaje usando tres operaciones. Es una forma textual y compacta de lo mismo que dibuja un autómata.

| Característica | Descripción |
|---|---|
| Es texto | Se puede escribir en una línea, guardar en un archivo o pasar a una función |
| Describe, no ejecuta | Dice **qué** cadenas forman el lenguaje, no **cómo** reconocerlas |
| Se construye por partes | Expresiones pequeñas se combinan en otras más grandes |
| Poder | Exactamente el de los autómatas finitos (3.3.2) |

**Por qué existen si ya hay autómatas.** Un autómata es cómodo para una máquina, pero dibujarlo para escribir "un número con decimales opcionales" es lento. Una expresión regular dice lo mismo en una línea (`[0-9]+(\.[0-9]+)?`), se puede escribir en un programa o en un buscador, y una biblioteca se encarga de convertirla en autómata. Son la **notación** para las personas; los autómatas, la **ejecución** para las máquinas.

#### Las tres operaciones básicas

| Operación | Se escribe | Significado | Ejemplo | Lenguaje |
|---|---|---|---|---|
| **Unión** | `R\|S` | Una cadena de R **o** una de S | `a\|b` | {a, b} |
| **Concatenación** | `RS` | Una cadena de R **seguida** de una de S | `ab` | {ab} |
| **Cerradura de Kleene** | `R*` | **Cero o más** repeticiones de R | `a*` | {ε, a, aa, aaa, …} |

> En los textos de teoría la unión se escribe `R + S` o `R ∪ S`. En este material (y en Java) se escribe `R|S`, para no confundirla con el `+` de "una o más".

Cada operación corresponde a una forma de **combinar lenguajes**: la unión junta dos conjuntos; la concatenación forma todas las parejas "una de aquí seguida de una de allá"; la cerradura permite repetir cualquier número de veces, **incluido cero**. Ese "incluido cero" es la razón por la que `R*` siempre contiene a ε, sea cual sea R.

#### Definición recursiva

Las expresiones regulares sobre un alfabeto Σ se construyen así:

1. `∅` (lenguaje vacío), `ε` (cadena vacía) y cada símbolo `a ∈ Σ` son expresiones regulares.
2. Si `R` y `S` son expresiones regulares, también lo son `(R|S)`, `(RS)` y `(R*)`.
3. Ninguna otra cosa es expresión regular.

Y su **significado** (semántica) `L(R)`, también recursivo:

| Expresión | Lenguaje `L(…)` |
|---|---|
| `∅` | { } |
| `ε` | {ε} |
| `a` | {a} |
| `R\|S` | L(R) ∪ L(S) |
| `RS` | { uv : u ∈ L(R), v ∈ L(S) } |
| `R*` | {ε} ∪ L(R) ∪ L(R)L(R) ∪ … |

La definición tiene la misma forma que un árbol (Unidad 2): las expresiones del punto 1 son las **hojas** y cada operación del punto 2 es un **nodo interno** con una o dos subexpresiones como hijos. Calcular el lenguaje de una expresión es recorrer ese árbol de abajo hacia arriba.

Ejemplo resuelto: el lenguaje de `(a|b)c*`, limitado a cadenas de longitud 3 o menos:

| Subexpresión | Lenguaje (hasta longitud 3) |
|---|---|
| `a` | {a} |
| `b` | {b} |
| `a\|b` | {a, b} |
| `c` | {c} |
| `c*` | {ε, c, cc, ccc} |
| `(a\|b)c*` | {a, b, ac, bc, acc, bcc} (`accc` ya mide 4) |

#### Precedencia

De mayor a menor: **`*`** → **concatenación** → **`|`**. Los paréntesis cambian el orden.

| Expresión | Se lee como | Lenguaje |
|---|---|---|
| `ab*` | `a(b*)` | `a` seguida de cero o más `b`: a, ab, abb, … |
| `(ab)*` | | ε, ab, abab, ababab, … |
| `a\|bc` | `a\|(bc)` | {a, bc} |
| `(a\|b)c` | | {ac, bc} |

Es la misma jerarquía que la de `*` y `+` en aritmética (2.2.2): la cerradura "pega más fuerte" que la concatenación, igual que la multiplicación pega más fuerte que la suma.

Expresiones que se parecen pero describen lenguajes distintos:

| Expresión 1 | Expresión 2 | Cadena que las distingue |
|---|---|---|
| `(a\|b)*` | `a*\|b*` | `ab`: está en la 1 (cualquier mezcla), no en la 2 (solo `a` o solo `b`) |
| `(ab)*` | `a*b*` | `aabb`: está en la 2, no en la 1; `abab`: está en la 1, no en la 2 |
| `a*` | `a+` | ε: está en la 1, no en la 2 |

#### Ejemplos

| Expresión | Lenguaje que describe |
|---|---|
| `(a\|b)*` | **Todas** las cadenas sobre {a, b}, incluida ε |
| `(a\|b)*ab` | Las que **terminan en** `ab` |
| `ab(a\|b)*` | Las que **empiezan con** `ab` |
| `(a\|b)*aa(a\|b)*` | Las que **contienen** `aa` |
| `0*(10*10*)*` | Cadenas binarias con un número **par de 1s** |
| `(0\|10*1)*` | También cadenas binarias con un número **par de 1s** |

Hay lenguajes que se describen con más de una expresión: las dos últimas son **equivalentes**.

Cómo leer `0*(10*10*)*`: empieza con ceros sueltos (`0*`) y luego repite bloques que contienen **exactamente dos** 1s, cada uno seguido de ceros (`10*10*`). Como los 1s solo entran de dos en dos, el total siempre es par.

#### Atajos que usan los lenguajes de programación

Los atajos no agregan poder; son abreviaturas de las tres operaciones básicas:

| Atajo | Significa | Equivale a |
|---|---|---|
| `R+` | Una o más veces R | `RR*` |
| `R?` | R es opcional | `(R\|ε)` |
| `[abc]` | Cualquiera de esos símbolos | `a\|b\|c` |
| `[a-z]` | Cualquiera del rango | `a\|b\|…\|z` |
| `\d` | Un dígito | `[0-9]` |
| `.` | Cualquier símbolo (menos salto de línea) | |

Ejemplos de uso real:

| Qué reconoce | Expresión |
|---|---|
| Identificador (el del caso 3) | `[A-Za-z_][A-Za-z0-9_]*` |
| Número entero | `[0-9]+` |
| Número con decimales opcionales | `[0-9]+(\.[0-9]+)?` |

En `[0-9]+(\.[0-9]+)?` el punto lleva `\` porque `.` solo, sin escapar, significa "cualquier símbolo". Con la diagonal se pide un punto literal.

#### Expresiones regulares en Java

El paquete `java.util.regex` ofrece la clase `Pattern`. Una expresión se **compila** una vez con `Pattern.compile` (Java la traduce a una estructura interna de objetos nodo enlazados, uno por cada pieza de la expresión) y después se prueba con muchas cadenas usando `matcher(w).matches()`:

```java
import java.util.regex.Pattern;

Pattern terminaEnAbRegex = Pattern.compile("(a|b)*ab");

String[] pruebas = { "ab", "aab", "abb", "bab", "" };
for (String w : pruebas)
    System.out.printf("%-4s -> %b%n", w.isEmpty() ? "ε" : w, terminaEnAbRegex.matcher(w).matches());

// → ab   -> true
// → aab  -> true
// → abb  -> false
// → bab  -> true
// → ε    -> false
```

Los mismos resultados que el AFD `terminaEnAb` de 3.1.2: la expresión y el autómata describen el mismo lenguaje.

> **Ojo con `matches()` contra `find()`.** El lenguaje de una ER formal es el de las cadenas que coinciden **completas**. En Java, `matches()` hace exactamente eso: exige que **toda** la cadena coincida. `find()`, en cambio, busca la expresión en **cualquier parte** de la cadena, y eso no es lo que pide la teoría:

```java
Pattern ab = Pattern.compile("ab");
System.out.println(ab.matcher("xxabxx").matches());   // → false  (la cadena completa no es "ab")
System.out.println(ab.matcher("xxabxx").find());      // → true   ("ab" aparece adentro)
```

> **Ojo con la diagonal en Java.** Dentro de un texto de Java, `\` se escribe `\\`. La expresión `[0-9]+(\.[0-9]+)?` se escribe en el código como `"[0-9]+(\\.[0-9]+)?"`. Si se escribe `"\."`, el compilador marca error porque `\.` no es una secuencia válida en una cadena de Java.

```java
System.out.println(Pattern.matches("[A-Za-z_][A-Za-z0-9_]*", "x1"));   // → true
System.out.println(Pattern.matches("[0-9]+(\\.[0-9]+)?", "3.14"));     // → true
System.out.println(Pattern.matches("[0-9]+(\\.[0-9]+)?", "3."));       // → false  (falta un dígito después del punto)
```

> **Ojo con lo que no es regular.** No todo lo que acepta `Pattern` en Java es regular. Funciones como las referencias hacia atrás (`\1`, "repite lo mismo que capturó el grupo 1") permiten reconocer lenguajes que **ningún** autómata finito acepta. En esta materia se usa solo el subconjunto formal: unión, concatenación y cerradura (y sus atajos).

```java
// (a+)b\1 : una o más a, una b, y otra vez LA MISMA cantidad de a → { aⁿbaⁿ : n ≥ 1 }, que no es regular
System.out.println(Pattern.matches("(a+)b\\1", "aabaa"));   // → true
System.out.println(Pattern.matches("(a+)b\\1", "aaba"));    // → false
```

Para aceptar `aabaa` y rechazar `aaba`, la expresión tiene que **contar** las `a`: justo lo que un autómata finito no puede hacer (3.1.1). Para soportar estas funciones, el motor de `Pattern` no trabaja como un AFD: prueba alternativas y, si una falla, retrocede a probar otra (*backtracking*). Eso le da más poder, pero en el peor caso puede ser muchísimo más lento que el paso por símbolo de un AFD.

### 3.3.2. Equivalencia entre expresiones regulares y autómatas finitos

#### El teorema

> **Teorema de Kleene.** Un lenguaje es regular si y solo si existe una expresión regular que lo describe.

Es decir, estas **tres** formas de describir un lenguaje regular son intercambiables:

```
        ┌──────────────┐        ┌──────────────┐
        │ Expresión    │ ◀────▶ │     AFN      │
        │ regular      │        └──────┬───────┘
        └──────────────┘               │  (construcción de subconjuntos, 3.2.2)
                                ┌──────▼───────┐
                                │     AFD      │
                                └──────────────┘
```

Para pasar de una a otra hay dos caminos.

| Forma | Ventaja | Desventaja | Uso típico |
|---|---|---|---|
| Expresión regular | Corta, fácil de escribir y leer | No se "ejecuta" directamente | Escribir patrones en programas y buscadores |
| AFN | Fácil de construir a partir de una ER | Simularlo exige seguir conjuntos de estados | Paso intermedio de la conversión |
| AFD | Ejecución más rápida: un paso por símbolo | Puede tener muchos más estados | Analizadores léxicos, validación |

Saber que las tres son equivalentes permite **elegir la más cómoda para cada tarea**: se piensa y se escribe en ER, se ejecuta como AFD.

#### De ER a autómata: armar piezas

Se construye el autómata a partir de la estructura de la expresión, combinando piezas pequeñas. Para unir las piezas se permite en el AFN una **transición ε**: una flecha que se puede seguir **sin leer ningún símbolo**.

| Expresión | Autómata |
|---|---|
| Símbolo `a` | `──▶ (p) ──a──▶ ((q))` |
| Unión `R\|S` | Un estado nuevo inicial con flechas `ε` hacia el inicio de R y el inicio de S; los finales de R y de S llevan con `ε` a un nuevo estado final |
| Concatenación `RS` | Los estados finales de R se conectan con `ε` al estado inicial de S |
| Cerradura `R*` | Un estado nuevo, inicial y de aceptación (así acepta ε); de él sale `ε` hacia el inicio de R, y de los finales de R regresan `ε` hacia ese estado |

```
 Unión R|S                              Cerradura R*

        ┌─ε─▶ [ R ] ─ε─┐                      ┌───────────────ε───────────────┐
  ──▶ (p)               ▶ ((q))               ▼                               │
        └─ε─▶ [ S ] ─ε─┘             ──▶ ((p)) ──ε──▶ [ R ] ───ε──────────────┘
```

Con esto, cualquier expresión se convierte en un AFN, y con la construcción de subconjuntos, en un AFD.

**Por qué funciona.** La construcción sigue la **definición recursiva** de 3.3.1: hay una pieza para cada caso base (`a`, `ε`, `∅`) y una regla de ensamblado para cada operación. Como toda expresión se arma con esos casos, toda expresión se puede convertir. Las transiciones ε son el "pegamento": permiten conectar piezas sin alterar lo que cada una lee.

Ejemplo de ensamblado para `(a|b)*ab`, de adentro hacia afuera:

| Paso | Pieza | Cómo se arma |
|---|---|---|
| 1 | `a`, `b` | Dos piezas de un símbolo |
| 2 | `a\|b` | Unión de las piezas del paso 1 |
| 3 | `(a\|b)*` | Cerradura del paso 2 |
| 4 | `ab` | Concatenación de una pieza `a` con una pieza `b` |
| 5 | `(a\|b)*ab` | Concatenación del paso 3 con el paso 4 |

El AFN que resulta tiene más estados que el AFN de 3.2.1 (por todas las flechas ε), pero reconoce el mismo lenguaje. La construcción garantiza que **siempre** funciona, no que dé el autómata más pequeño.

#### De autómata a ER: eliminar estados

Se van **eliminando estados** uno por uno, sustituyéndolos por flechas etiquetadas con expresiones regulares, hasta que queda una sola flecha del inicio al final. Ejemplo con el AFD de los **1s pares** (3.1.1):

```
   0                          0
  ┌───┐                      ┌───┐
  ▼   │                      ▼   │
 ──▶ ((q0)) ──────── 1 ────▶ (q1)
        ▲                      │
        └──────── 1 ───────────┘
```

Para eliminar q1 se considera el recorrido que pasa por él: entrar con `1`, dar vueltas con `0*` y salir con `1`. Ese recorrido se convierte en un nuevo lazo sobre q0: **`10*1`**. Queda un solo estado con dos lazos, `0` y `10*1`:

```
 ──▶ ((q0))  con el lazo  0 | 10*1      ⟹      (0 | 10*1)*
```

La ER es **`(0|10*1)*`**.

La regla general para eliminar un estado `q` se aplica a cada par de flechas "entra a q / sale de q":

```
  Antes:   (p) ──R──▶ (q) ──T──▶ (r)        con un lazo S sobre q
                       ↺ S

  Después: (p) ────── R S* T ──────▶ (r)    (si ya había una flecha U de p a r: R S* T | U)
```

En el ejemplo, p y r son el mismo estado q0, R = `1`, S = `0` y T = `1`: el nuevo lazo es `1 0* 1`, que se une con el lazo `0` que ya existía.

> **Ojo con el resultado de la eliminación.** Si se eliminan los estados en otro orden se obtiene otra expresión, en apariencia distinta, pero equivalente. No hay una única ER "correcta" para un autómata; lo que debe coincidir es el **lenguaje**.

#### Comprobación en Java

Una ER y un autómata son equivalentes si aceptan **las mismas cadenas**. No se puede comprobar con todas las cadenas (son infinitas), pero sí con todas las de longitud hasta un límite. Esto **no es una demostración**, pero detecta de inmediato los errores.

```java
import java.util.ArrayList;

public class Equivalencia {

    // Todas las cadenas de exactamente 'largo' símbolos sobre el alfabeto
    public static ArrayList<String> cadenas(String alfabeto, int largo) {
        ArrayList<String> resultado = new ArrayList<String>();
        if (largo == 0) {
            resultado.add("");
            return resultado;
        }

        for (String prefijo : cadenas(alfabeto, largo - 1))
            for (int i = 0; i < alfabeto.length(); i++)
                resultado.add(prefijo + alfabeto.charAt(i));
        return resultado;
    }

    public static boolean equivalentes(Reconocedor f, Reconocedor g, String alfabeto, int maxLargo) {
        for (int largo = 0; largo <= maxLargo; largo++)
            for (String w : cadenas(alfabeto, largo))
                if (f.acepta(w) != g.acepta(w)) {
                    System.out.println("Difieren en: \"" + w + "\"");
                    return false;
                }
        return true;
    }
}
```

`cadenas` es recursiva: las cadenas de longitud n se obtienen tomando cada cadena de longitud n − 1 y agregándole cada símbolo al final. `cadenas("01", 2)` da `[00, 01, 10, 11]`.

Aquí se ve la utilidad de la interfaz `Reconocedor` (3.1.1): `equivalentes` recibe dos objetos que **saben aceptar cadenas**, sin importar si son un AFD, un AFN o una expresión regular. Un `AFD` o un `AFN` se pasan directamente. Una expresión regular se adapta con una **lambda**: como `Reconocedor` tiene un solo método, `w -> er.matcher(w).matches()` crea un objeto cuyo método `acepta(w)` hace exactamente eso.

```java
// Usa pares (3.1.1) y afn (3.2.1), creados antes en el mismo programa

// El AFD de 3.1.1 contra la ER obtenida por eliminación de estados
Pattern er = Pattern.compile("(0|10*1)*");
System.out.println(Equivalencia.equivalentes(pares, w -> er.matcher(w).matches(), "01", 10));    // → true

// El AFN de 3.2.1 contra la ER (a|b)*ab
Pattern er2 = Pattern.compile("(a|b)*ab");
System.out.println(Equivalencia.equivalentes(afn, w -> er2.matcher(w).matches(), "ab", 10));     // → true

// El AFN contra el AFD que se obtuvo al convertirlo (3.2.2)
System.out.println(Equivalencia.equivalentes(afn, afn.convertirAAFD(), "ab", 10));               // → true

// La otra ER de los 1s pares
Pattern otra = Pattern.compile("0*(10*10*)*");
System.out.println(Equivalencia.equivalentes(pares, w -> otra.matcher(w).matches(), "01", 10));  // → true

// Una ER incorrecta: acepta TODAS las cadenas binarias
Pattern mala = Pattern.compile("(0|1)*");
System.out.println(Equivalencia.equivalentes(pares, w -> mala.matcher(w).matches(), "01", 10));  // → Difieren en: "1"
                                                                                                  // → false
```

¿Cuántas cadenas revisa cada comprobación? Con 2 símbolos y longitud hasta 10: 1 + 2 + 4 + … + 1 024 = 2¹¹ − 1 = **2 047** cadenas. Como las cadenas se prueban de la más corta a la más larga, cuando dos reconocedores difieren se reporta el **contraejemplo más corto**: con la ER incorrecta es `"1"`, que tiene un solo 1 (impar) y aun así la ER lo acepta.

> **Ojo con creer que la prueba demuestra.** Que coincidan en las 2 047 cadenas no garantiza que coincidan en todas: podrían diferir por primera vez en una cadena de longitud 11. Para lenguajes regulares hay una cota que sí lo garantizaría (depende del número de estados), pero para practicar basta con un límite razonable: los errores de diseño casi siempre aparecen en cadenas cortas.
