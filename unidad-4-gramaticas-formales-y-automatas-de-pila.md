# Unidad 4. Gramáticas formales y autómatas de pila

Los autómatas finitos de la Unidad 3 tienen un límite claro: **no pueden contar**. Un AFD que lee `aaa…abbb…` no puede recordar cuántas `a` llevó, porque solo recuerda "en qué estado estoy" y tiene un número finito de estados. Por eso no existe un AFD para el lenguaje `{ aⁿbⁿ : n ≥ 0 }` ni para los **paréntesis balanceados**, que son justo lo que hace falta para analizar un programa.

El argumento de por qué no existe es corto y vale la pena entenderlo. Supón que alguien afirma tener un AFD de **k** estados para `aⁿbⁿ`. Al leer las k + 1 cadenas `ε, a, aa, …, aᵏ`, el autómata solo tiene k estados donde quedar, así que **dos de ellas** terminan en el mismo estado (como meter k + 1 cartas en k cajones). Digamos que `aⁱ` y `aʲ`, con i ≠ j:

| Cadena | Después de leer las `a` | Después de leer `bⁱ` | Debería |
|---|---|---|---|
| `aⁱbⁱ` | Estado p | Termina en un estado de aceptación | Aceptarse |
| `aʲbⁱ` | **El mismo** estado p | Termina en **el mismo** estado, porque lee lo mismo desde p | Rechazarse (i ≠ j) |

El autómata no puede distinguirlas: o acepta las dos o rechaza las dos. Cualquiera que sea k, falla. Hace falta una memoria que **crezca** con la entrada.

Esta unidad resuelve el problema por dos caminos que resultan equivalentes:

- Una **gramática libre de contexto** *genera* las cadenas del lenguaje mediante reglas de reemplazo (el árbol de derivación de la Unidad 2 es su dibujo).
- Un **autómata de pila** *reconoce* las cadenas añadiendo a un autómata finito una **pila**: una memoria sin límite de tamaño, pero que solo se puede leer por arriba.

La unidad cierra uniendo todo el curso: del grafo al autómata, del árbol a la gramática.

> Los ejemplos de esta unidad usan la clase `Gramatica` que se construye en 4.1.1; las clases `NodoPila`, `TransicionPila`, `Configuracion` y `AutomataDePila` de 4.2.1, y la interfaz `Reconocedor` con la clase `Equivalencia` de la Unidad 3 (3.3.2). La pila del autómata se implementa como una **lista enlazada** de objetos `NodoPila`.

## 4.1. Gramáticas libres de contexto

### 4.1.1. Definición formal (no terminales, terminales, producciones, símbolo inicial)

Una **gramática libre de contexto** (GLC) es una cuádrupla **G = (V, Σ, P, S)**:

| Componente | Nombre | Significado | Ejemplo en `S → aSb \| ε` |
|---|---|---|---|
| **V** | Símbolos **no terminales** (variables) | Se reemplazan usando reglas | {S} |
| **Σ** | Símbolos **terminales** | Los símbolos de las cadenas finales; nunca se reemplazan | {a, b} |
| **P** | **Producciones** | Reglas `A → α`, con `A ∈ V` y `α` una secuencia de terminales y no terminales | `S → aSb`, `S → ε` |
| **S** | Símbolo **inicial** | No terminal con el que empieza toda derivación; `S ∈ V` | S |

Convención de este material: los **no terminales** se escriben con **mayúscula** (`S`, `E`, `T`) y los **terminales** con minúsculas, dígitos o signos (`a`, `b`, `2`, `+`, `(`). Varias producciones del mismo no terminal se escriben juntas con `|`:

```
S → aSb | ε          equivale a las dos reglas:   S → aSb     S → ε
```

| Característica | Descripción |
|---|---|
| Genera, no reconoce | Dice cómo **construir** las cadenas; para decidir si una cadena pertenece hay que buscar una derivación |
| Finita | Tiene un número finito de reglas, pero puede generar un lenguaje infinito |
| Recursiva | Un no terminal puede aparecer en su propio lado derecho (`S → aSb`); de ahí sale el poder de "contar" |
| Lado izquierdo | Siempre **un solo** no terminal |
| Su dibujo | El árbol de derivación (Unidad 2) |

**Por qué existe.** Los lenguajes de programación están llenos de estructuras **anidadas**: paréntesis dentro de paréntesis, bloques `{ }` dentro de bloques, `if` dentro de `if`. Una expresión regular no puede describirlas (no puede contar la profundidad), pero una gramática sí, gracias a la recursión. Por eso la sintaxis de casi todos los lenguajes de programación se publica como una gramática libre de contexto.

**De dónde sale el poder de contar.** En `S → aSb`, cada aplicación de la regla pone una `a` y una `b` **al mismo tiempo**, en lados opuestos del `S` que queda en medio. Por eso nunca puede haber más de una que de otra: el emparejamiento lo garantiza la forma de la regla, no un contador. Es la misma estructura que un par de paréntesis: se abre y se cierra en el mismo paso, y lo de adentro queda en medio.

#### ¿Por qué "libre de contexto"?

Porque el lado izquierdo de cada producción es **un solo no terminal**: `A` se puede reemplazar por `α` **sin importar qué símbolos tenga alrededor** (el contexto). Una regla como `aAb → aab` ("`A` solo se reemplaza si está entre una `a` y una `b`") **no** es libre de contexto.

| Regla | ¿Libre de contexto? | Por qué |
|---|---|---|
| `S → aSb` | Sí | Lado izquierdo: un solo no terminal |
| `A → ε` | Sí | Un solo no terminal; reemplazarlo por nada está permitido |
| `aAb → aab` | No | El lado izquierdo incluye el contexto `a…b` |
| `AB → BA` | No | Dos símbolos en el lado izquierdo |

Que el reemplazo no dependa del contexto es lo que permite dibujar la derivación como un **árbol**: lo que cuelga de cada no terminal se decide solo con ese no terminal, sin mirar a sus vecinos.

#### Derivación y lenguaje de la gramática

- `α ⇒ β` significa: **en un paso**, aplicando una producción, `α` se transforma en `β`.
- `α ⇒* β` significa: en **cero o más pasos**.
- El **lenguaje de la gramática** es el conjunto de cadenas **solo de terminales** que se derivan del símbolo inicial:

> **L(G) = { w ∈ Σ\* : S ⇒\* w }**

Una **forma sentencial** es cualquier cadena intermedia de la derivación (mezcla de terminales y no terminales), por ejemplo `aSb` o `aaSbb`.

Derivación de `aabb` con `S → aSb | ε`:

```
S ⇒ aSb ⇒ aaSbb ⇒ aabb
```

> **Ojo con las formas sentenciales.** `aSb` se deriva de `S`, pero **no** pertenece a L(G): todavía tiene un no terminal. Solo las cadenas formadas únicamente por terminales están en el lenguaje.

#### Ejemplos de gramáticas

| Lenguaje | Gramática |
|---|---|
| `aⁿbⁿ` (n ≥ 0): mismas `a` que `b`, en ese orden | `S → aSb \| ε` |
| Paréntesis balanceados | `S → (S)S \| ε` |
| Palíndromos sobre {a, b} | `S → aSa \| bSb \| a \| b \| ε` |
| Expresiones aritméticas con `+` y `*` (Unidad 2) | `E → E+T \| T`   `T → T*F \| F`   `F → (E) \| n` |

> Los palíndromos funcionan así: cada `aSa` o `bSb` agrega el **mismo símbolo a ambos extremos**, y el caso base (`a`, `b` o `ε`) pone el centro (longitud impar o par).

La gramática de paréntesis `S → (S)S` se lee: "una cadena balanceada es o vacía, o un par `( )` que **encierra** una cadena balanceada, **seguido** de otra cadena balanceada". El primer `S` es lo de adentro; el segundo, lo que viene después. Por ejemplo, `(())()` se descompone así:

| Parte | Cadena | Qué `S` la genera |
|---|---|---|
| Par exterior | `( … )` con `()` adentro | `S → (S)S`, el primer `S` produce `()` |
| Lo que sigue | `()` | El segundo `S` produce `()` |

#### La gramática en Java

La gramática se guarda en un `LinkedHashMap`: a cada no terminal (un `char` mayúscula) le corresponde la lista de lados derechos de sus producciones (la cadena vacía `""` representa `ε`). Se usa `LinkedHashMap` en lugar de `HashMap` porque recuerda el **orden** en que se agregaron las reglas: por dentro, además de la tabla, mantiene una lista doblemente enlazada de las entradas.

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeSet;

public class Gramatica {

    private char inicial = 'S';
    // A cada no terminal le corresponde la lista de lados derechos de sus producciones ("" = ε)
    private LinkedHashMap<Character, ArrayList<String>> producciones = new LinkedHashMap<Character, ArrayList<String>>();

    public Gramatica(char inicial) {
        this.inicial = inicial;
    }

    public char getInicial() {
        return inicial;
    }

    public LinkedHashMap<Character, ArrayList<String>> getProducciones() {
        return producciones;
    }

    // agregar('S', "aSb", "") registra S → aSb | ε
    public void agregar(char noTerminal, String... lados) {
        ArrayList<String> lista = producciones.get(noTerminal);
        if (lista == null) {
            lista = new ArrayList<String>();
            producciones.put(noTerminal, lista);
        }
        for (String lado : lados) lista.add(lado);
    }

    public boolean esNoTerminal(char c) {
        return producciones.containsKey(c);
    }

    public ArrayList<Character> terminales() {
        ArrayList<Character> resultado = new ArrayList<Character>();
        for (ArrayList<String> lados : producciones.values())
            for (String lado : lados)
                for (char c : lado.toCharArray())
                    if (!esNoTerminal(c) && !resultado.contains(c)) resultado.add(c);
        return resultado;
    }

    private int primerNoTerminal(String forma) {
        for (int i = 0; i < forma.length(); i++)
            if (esNoTerminal(forma.charAt(i))) return i;
        return -1;                                       // solo terminales
    }

    private int contarTerminales(String forma) {
        int total = 0;
        for (char c : forma.toCharArray())
            if (!esNoTerminal(c)) total++;
        return total;
    }

    // Todas las cadenas del lenguaje de longitud <= maxLargo (derivando siempre el no terminal más a la izquierda)
    public TreeSet<String> genera(int maxLargo) {
        // Orden: primero por longitud y, a igual longitud, alfabético
        TreeSet<String> resultado = new TreeSet<String>((x, y) ->
            x.length() != y.length() ? Integer.compare(x.length(), y.length()) : x.compareTo(y));

        HashSet<String> vistas = new HashSet<String>();
        LinkedList<String> cola = new LinkedList<String>();
        vistas.add(String.valueOf(inicial));
        cola.addLast(String.valueOf(inicial));

        while (!cola.isEmpty()) {
            String forma = cola.removeFirst();
            int pos = primerNoTerminal(forma);

            if (pos < 0) {                               // solo terminales: es una cadena del lenguaje
                resultado.add(forma);
                continue;
            }

            for (String lado : producciones.get(forma.charAt(pos))) {
                String nueva = forma.substring(0, pos) + lado + forma.substring(pos + 1);

                // Poda: si ya tiene más terminales de los permitidos, nunca dará una cadena válida
                if (contarTerminales(nueva) <= maxLargo && vistas.add(nueva))
                    cola.addLast(nueva);
            }
        }

        return resultado;
    }

    // Una derivación por la izquierda de 'objetivo', o null si la cadena no pertenece al lenguaje
    public ArrayList<String> derivacion(String objetivo) {
        HashMap<String, String> padre = new HashMap<String, String>();   // forma → forma de la que salió
        LinkedList<String> cola = new LinkedList<String>();
        padre.put(String.valueOf(inicial), null);
        cola.addLast(String.valueOf(inicial));

        while (!cola.isEmpty()) {
            String forma = cola.removeFirst();
            int pos = primerNoTerminal(forma);

            if (pos < 0) {
                if (!forma.equals(objetivo)) continue;

                ArrayList<String> camino = new ArrayList<String>();
                for (String f = forma; f != null; f = padre.get(f)) camino.add(f);
                Collections.reverse(camino);
                return camino;
            }

            for (String lado : producciones.get(forma.charAt(pos))) {
                String nueva = forma.substring(0, pos) + lado + forma.substring(pos + 1);

                // Poda: los terminales iniciales de la forma deben coincidir con el inicio del objetivo
                int p = primerNoTerminal(nueva);
                String prefijo = p < 0 ? nueva : nueva.substring(0, p);
                if (!objetivo.startsWith(prefijo)) continue;
                if (contarTerminales(nueva) > objetivo.length()) continue;

                if (!padre.containsKey(nueva)) {
                    padre.put(nueva, forma);
                    cola.addLast(nueva);
                }
            }
        }

        return null;
    }
}
```

**Cómo trabaja `genera`.** Es la **búsqueda en anchura** de la Unidad 1, pero sobre un grafo que no está guardado en ningún lado: los "nodos" son formas sentenciales y las "aristas" son los pasos de derivación. Cada forma se expande reemplazando su no terminal más a la izquierda por cada uno de sus lados derechos.

| Pieza | Para qué sirve | Qué pasaría sin ella |
|---|---|---|
| Poda por número de terminales | Descarta formas que ya tienen más terminales que `maxLargo` | El grafo de formas es **infinito** (`S`, `aSb`, `aaSbb`, …) y la búsqueda nunca terminaría |
| Conjunto `vistas` | No expande dos veces la misma forma | Formas a las que se llega por caminos distintos se repetirían |
| `TreeSet` con comparador | Guarda los resultados sin repetidos y ordenados | Saldrían en el orden en que se encuentran, más difícil de leer |

Traza de `genera(4)` con `S → aSb | ε`:

| Sale de la cola | Expansiones (`aSb` y `ε`) | ¿Entran? | Cola después | Resultado |
|---|---|---|---|---|
| `S` | `aSb` (2 terminales), `""` (0) | Ambas | [aSb, ""] | { } |
| `aSb` | `aaSbb` (4), `ab` (2) | Ambas | ["", aaSbb, ab] | { } |
| `""` | — (solo terminales) | — | [aaSbb, ab] | {ε} |
| `aaSbb` | `aaaSbbb` (6 > 4: **poda**), `aabb` (4) | Solo `aabb` | [ab, aabb] | {ε} |
| `ab` | — | — | [aabb] | {ε, ab} |
| `aabb` | — | — | [ ] | {ε, ab, aabb} |

**Cómo trabaja `derivacion`.** También es una BFS, pero guarda en el mapa `padre` de qué forma salió cada forma nueva. Cuando encuentra el objetivo, sigue esos enlaces hacia atrás (`f = padre.get(f)`) hasta llegar a `S`, cuyo padre es `null`. Es una **lista enlazada implícita**: cada forma "apunta" a la anterior, y recorrerla de principio a fin da la derivación al revés; por eso al final se invierte con `Collections.reverse`. La poda por prefijo descarta, por ejemplo, la forma `bSb` cuando el objetivo empieza con `a`: ya nunca podrá convertirse en él.

Generar las cadenas de un lenguaje:

```java
Gramatica anbn = new Gramatica('S');
anbn.agregar('S', "aSb", "");                  // S → aSb | ε

for (String w : anbn.genera(6))
    System.out.println(w.isEmpty() ? "ε" : w);
// → ε
// → ab
// → aabb
// → aaabbb

Gramatica parentesis = new Gramatica('S');
parentesis.agregar('S', "(S)S", "");           // S → (S)S | ε

String linea = "";
for (String w : parentesis.genera(6))
    linea += (w.isEmpty() ? "ε" : w) + "  ";
System.out.println(linea.trim());
// → ε  ()  (())  ()()  ((()))  (()())  (())()  ()(())  ()()()
```

Y encontrar la derivación de una cadena concreta:

```java
Gramatica palindromos = new Gramatica('S');
palindromos.agregar('S', "aSa", "bSb", "a", "b", "");

String[] pruebas = { "aabb", "abba", "abab" };
for (String w : pruebas) {
    Gramatica g = w.equals("aabb") ? anbn : palindromos;
    ArrayList<String> d = g.derivacion(w);

    if (d == null) {
        System.out.println(w + ": no pertenece al lenguaje");
    } else {
        String pasos = "";
        for (String f : d) pasos += (pasos.isEmpty() ? "" : " ⇒ ") + (f.isEmpty() ? "ε" : f);
        System.out.println(w + ": " + pasos);
    }
}

// → aabb: S ⇒ aSb ⇒ aaSbb ⇒ aabb
// → abba: S ⇒ aSa ⇒ abSba ⇒ abba
// → abab: no pertenece al lenguaje
```

> **Ojo con "pertenece" y "no pertenece".** En la gramática `S → aSb | ε` la cadena `aabb` **sí** pertenece, pero `aab` y `abab` **no** (`anbn.derivacion("aab")` devuelve `null`). Una cadena pertenece al lenguaje si **existe al menos una** derivación desde `S`; para demostrar que *no* pertenece hay que mostrar que ninguna derivación la produce. El programa puede afirmarlo porque la poda vuelve finita la búsqueda: revisa **todas** las derivaciones que podrían producir la cadena.

### 4.1.2. Derivaciones y jerarquía de Chomsky (panorama general)

#### Derivación por la izquierda y por la derecha

Cuando en una forma sentencial hay varios no terminales, hay que decidir **cuál reemplazar primero**. Las dos estrategias estándar son:

| Derivación | Reemplaza siempre… |
|---|---|
| **Por la izquierda** (*leftmost*) | el no terminal **más a la izquierda** |
| **Por la derecha** (*rightmost*) | el no terminal **más a la derecha** |

Con la gramática de expresiones de la Unidad 2, para `2+3*4`:

```
Por la izquierda:
E ⇒ E+T ⇒ T+T ⇒ F+T ⇒ 2+T ⇒ 2+T*F ⇒ 2+F*F ⇒ 2+3*F ⇒ 2+3*4

Por la derecha:
E ⇒ E+T ⇒ E+T*F ⇒ E+T*4 ⇒ E+F*4 ⇒ E+3*4 ⇒ T+3*4 ⇒ F+3*4 ⇒ 2+3*4
```

Ambas aplican **las mismas 8 producciones**, solo en distinto orden. Es la misma idea de la Unidad 2: **ambas derivaciones corresponden al mismo árbol de derivación**. El árbol no depende del orden en que se reemplazaron los no terminales, solo de **qué** producciones se aplicaron. En cambio, una cadena con **dos árboles distintos** (gramática ambigua) sí tiene derivaciones por la izquierda distintas.

La clase `Gramatica` de 4.1.1 produce justamente la derivación **por la izquierda** (siempre expande `primerNoTerminal`):

```java
Gramatica expr = new Gramatica('E');
expr.agregar('E', "E+T", "T");
expr.agregar('T', "T*F", "F");
expr.agregar('F', "(E)", "2", "3", "4");

System.out.println(expr.derivacion("2+3*4"));
// → [E, E+T, T+T, F+T, 2+T, 2+T*F, 2+F*F, 2+3*F, 2+3*4]
```

**Por qué importa fijar un orden.** Sin una regla de orden, una misma cadena tendría muchísimas derivaciones distintas que solo difieren en el momento en que se reemplaza cada no terminal, y compararlas sería un caos. Fijar "siempre el de más a la izquierda" deja **una derivación por cada árbol**. Por eso la definición de ambigüedad se puede decir de dos formas equivalentes: "más de un árbol" o "más de una derivación por la izquierda".

| Orden | Quién lo usa | Relación con el árbol |
|---|---|---|
| Por la izquierda | Analizadores **descendentes** (construyen el árbol de la raíz hacia las hojas) | Recorre el árbol en preorden |
| Por la derecha | Analizadores **ascendentes** (construyen el árbol de las hojas hacia la raíz), en orden inverso | Sus pasos, leídos al revés, reducen la cadena hasta `E` |

#### La jerarquía de Chomsky

En 1956 Noam Chomsky clasificó las gramáticas en **cuatro tipos**, según qué tan libres sean sus producciones. Cada tipo está **contenido** en el anterior y se asocia con un tipo de máquina:

| Tipo | Gramática | Forma de las producciones (`α → β`) | Máquina que reconoce el lenguaje | Ejemplo |
|---|---|---|---|---|
| **3** | **Regular** | `A → aB`, `A → a`, `A → ε` | **Autómata finito** (AFD / AFN) | Identificadores, `(a\|b)*ab` |
| **2** | **Libre de contexto** | `A → β`: lado izquierdo = **un** no terminal | **Autómata de pila** | `aⁿbⁿ`, paréntesis balanceados, expresiones aritméticas |
| **1** | Sensible al contexto | `\|α\| ≤ \|β\|` (la forma no se acorta) | Autómata lineal acotado | `aⁿbⁿcⁿ` |
| **0** | Sin restricciones | Cualquier `α → β` | **Máquina de Turing** | Todo lo que puede calcularse |

```
┌─────────────────────────────────────────────────────┐
│  Tipo 0: sin restricciones          (Máq. de Turing) │
│  ┌───────────────────────────────────────────────┐  │
│  │  Tipo 1: sensible al contexto                 │  │
│  │  ┌─────────────────────────────────────────┐  │  │
│  │  │  Tipo 2: libre de contexto    (pila)    │  │  │
│  │  │  ┌───────────────────────────────────┐  │  │  │
│  │  │  │  Tipo 3: regular   (aut. finito)  │  │  │  │
│  │  │  └───────────────────────────────────┘  │  │  │
│  │  └─────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

Cada anillo **contiene estrictamente** al interior: todo lenguaje regular es libre de contexto, pero `aⁿbⁿ` es libre de contexto y **no** es regular; `aⁿbⁿcⁿ` es sensible al contexto y **no** es libre de contexto. A mayor tipo, **más poder expresivo** pero **más difícil** de analizar.

La diferencia entre las máquinas es, en el fondo, **qué memoria tienen**:

| Tipo | Máquina | Memoria | Qué le permite |
|---|---|---|---|
| 3 | Autómata finito | Solo el estado actual (cantidad fija) | Reconocer patrones sin contar |
| 2 | Autómata de pila | Estado + una pila sin límite, accesible solo por arriba | Emparejar estructuras anidadas |
| 1 | Autómata lineal acotado | Una cinta del tamaño de la entrada, que puede leer y escribir en cualquier posición | Comparar varias cantidades a la vez |
| 0 | Máquina de Turing | Una cinta sin límite | Cualquier cálculo que pueda hacer una computadora |

Por qué una gramática regular corresponde a un autómata finito: en una regla `A → aB`, el no terminal `B` hace el papel de **estado**, y la regla dice "estando en A, con la `a` paso a B". La derivación siempre tiene **un solo** no terminal, al final, igual que un autómata siempre está en un solo estado. Por ejemplo, `S → aS | b` genera `b, ab, aab, …`, y corresponde al autómata que da vueltas con `a` en S y termina con `b`.

> En este curso se estudian a fondo los tipos **3** (Unidad 3) y **2** (esta unidad). Los tipos 1 y 0 solo se presentan como panorama: son el tema de cursos posteriores de teoría de la computación.

#### Clasificar una gramática (en Java)

El tipo de una gramática se decide mirando la forma de **cada** producción: la gramática tiene el tipo más alto (el más restringido) que **todas** sus producciones cumplen. Cada producción se representa como un arreglo `{ izquierda, derecha }`:

```java
static int tipoChomsky(String[][] producciones) {
    boolean regular = true, libre = true, sensible = true;

    for (String[] p : producciones) {
        String izq = p[0];
        String der = p[1];
        boolean unNoTerminal = izq.length() == 1 && Character.isUpperCase(izq.charAt(0));

        if (!unNoTerminal) libre = false;

        // Regular (lineal por la derecha): A → ε | a | aB
        boolean formaRegular = unNoTerminal && (
            der.isEmpty() ||
            (der.length() == 1 && !Character.isUpperCase(der.charAt(0))) ||
            (der.length() == 2 && !Character.isUpperCase(der.charAt(0)) && Character.isUpperCase(der.charAt(1))));
        if (!formaRegular) regular = false;

        // Sensible al contexto: la forma no se acorta (simplificación: se ignora S → ε)
        if (der.length() < izq.length()) sensible = false;
    }

    if (regular)  return 3;
    if (libre)    return 2;
    if (sensible) return 1;
    return 0;
}
```

La función empieza suponiendo que la gramática cumple los tres tipos (`true`) y **apaga** cada bandera en cuanto una producción la incumple. Basta con **una** producción que no sea regular para que la gramática deje de ser tipo 3.

```java
String[][] g1 = { { "S", "aS" }, { "S", "b" } };                                      // S → aS | b
String[][] g2 = { { "S", "aSb" }, { "S", "" } };                                      // S → aSb | ε
String[][] g3 = { { "S", "aSBC" }, { "S", "aBC" }, { "CB", "BC" }, { "aB", "ab" },   // aⁿbⁿcⁿ
                  { "bB", "bb" }, { "bC", "bc" }, { "cC", "cc" } };
String[][] g4 = { { "S", "aA" }, { "aA", "b" } };                                     // aA → b se acorta

System.out.println(tipoChomsky(g1));   // → 3
System.out.println(tipoChomsky(g2));   // → 2
System.out.println(tipoChomsky(g3));   // → 1
System.out.println(tipoChomsky(g4));   // → 0
```

| Gramática | Producción que decide | Bandera que se apaga | Tipo |
|---|---|---|---|
| g1 | Todas son `A → aB` o `A → a` | Ninguna | 3 |
| g2 | `S → aSb`: el lado derecho tiene un terminal **después** del no terminal | `regular` | 2 |
| g3 | `CB → BC`: dos símbolos a la izquierda | `regular` y `libre` | 1 |
| g4 | `aA → b`: la forma pasa de 2 símbolos a 1 | `regular`, `libre` y `sensible` | 0 |

> **No confundir la gramática con el lenguaje.** El tipo de una *gramática* se ve en la forma de sus reglas; el tipo de un *lenguaje* es el de la gramática **más simple** que lo genera. Una gramática complicada puede generar un lenguaje que en realidad es regular.

```java
String[][] g5 = { { "S", "aSa" }, { "S", "aa" }, { "S", "" } };   // genera las cadenas con un número par de a
System.out.println(tipoChomsky(g5));   // → 2
```

`g5` es tipo 2 por la forma de `S → aSa`, pero su lenguaje (cantidad par de `a`: ε, `aa`, `aaaa`, …) es regular: lo describe la expresión `(aa)*` o la gramática regular `S → aA | ε`, `A → aS`.

---

## 4.2. Autómatas de pila

### 4.2.1. Definición y funcionamiento de un autómata de pila

Un **autómata de pila** (AP, *pushdown automaton*) es un autómata finito al que se le agrega una **pila**. Con ella puede **contar**: apila un símbolo por cada `a` que lee y desapila uno por cada `b`; si al final la pila quedó vacía, había igual número de ambas.

| Característica | Descripción |
|---|---|
| Control finito | Tiene estados y transiciones, como un autómata finito |
| Memoria extra | Una pila **sin límite** de tamaño |
| Acceso restringido | Solo puede ver y cambiar el **tope**; para llegar a lo de abajo tiene que desapilar |
| Lectura | La entrada se lee de izquierda a derecha, sin regresar; algunas transiciones no leen nada (ε) |
| No determinismo | Puede tener varias transiciones posibles a la vez, y aquí **sí** le da más poder |
| Poder | Reconoce exactamente los lenguajes **libres de contexto** |

**Por qué una pila y no otra estructura.** Las estructuras anidadas tienen una propiedad: **lo último que se abrió es lo primero que se cierra**. En `( [ { } ] )`, el `{` fue el último en abrirse y es el primero en cerrarse. Esa es exactamente la disciplina de una pila. Una memoria más poderosa (poder leer cualquier posición) reconocería más lenguajes, pero sería más difícil de analizar; la pila es justo lo necesario para el anidamiento.

#### La pila

Una **pila** es una estructura de datos **LIFO** (*last in, first out*): el último elemento que entra es el primero que sale. Solo se puede acceder al elemento de **arriba** (el **tope**).

| Operación | Qué hace | En Java con `ArrayDeque<Character>` | En la pila enlazada (`NodoPila`) |
|---|---|---|---|
| **Apilar** (*push*) | Pone un elemento arriba | `pila.push('A')` | `tope = new NodoPila('A', tope)` |
| **Desapilar** (*pop*) | Quita el elemento de arriba y lo devuelve | `pila.pop()` | `tope = tope.getSiguiente()` |
| **Consultar el tope** (*peek*) | Ve el de arriba sin quitarlo | `pila.peek()` | `tope.getSimbolo()` |

```
   tope →  │ A │
           │ A │
fondo  →   │ $ │        el símbolo $ marca el fondo de la pila
           └───┘
```

**La pila como lista enlazada.** La forma más natural de programar una pila es con nodos enlazados: cada nodo guarda un símbolo y una referencia al nodo que tiene **debajo**. La pila completa es solo una referencia al nodo del tope.

```java
public class NodoPila {

    private char simbolo;
    private NodoPila siguiente = null;      // el nodo que está debajo (null = fondo de la lista)

    public NodoPila(char simbolo, NodoPila siguiente) {
        this.simbolo = simbolo;
        this.siguiente = siguiente;
    }

    public char getSimbolo() {
        return simbolo;
    }

    public NodoPila getSiguiente() {
        return siguiente;
    }

    // Pone los símbolos de gamma encima de 'base'; el PRIMER carácter de gamma queda en el tope
    public static NodoPila apilar(String gamma, NodoPila base) {
        NodoPila tope = base;
        for (int i = gamma.length() - 1; i >= 0; i--)
            tope = new NodoPila(gamma.charAt(i), tope);
        return tope;
    }

    // Contenido de la pila como texto, del tope al fondo
    public static String texto(NodoPila tope) {
        String resultado = "";
        for (NodoPila n = tope; n != null; n = n.getSiguiente())
            resultado += n.getSimbolo();
        return resultado;
    }
}
```

`apilar` recorre `gamma` **de derecha a izquierda** para que el **primer** carácter quede arriba: para poner `XY` sobre una base, primero se apila `Y` y después `X`. Es la convención de los autómatas de pila: en `γ = XY`, el tope es `X`.

```java
NodoPila fondo = new NodoPila('$', null);
NodoPila p1 = NodoPila.apilar("A", fondo);      // A$
NodoPila p2 = NodoPila.apilar("A", p1);         // AA$
NodoPila p3 = NodoPila.apilar("XY", p2);        // XYAA$

System.out.println(NodoPila.texto(p2));                  // → AA$
System.out.println(NodoPila.texto(p3));                  // → XYAA$
System.out.println(NodoPila.texto(p2.getSiguiente()));   // → A$     (desapilar)
System.out.println(p2.getSiguiente() == p1);             // → true   (es el mismo objeto, no una copia)
```

En memoria, las cuatro "pilas" comparten nodos. Ninguna operación copia la pila; solo se crean nodos nuevos encima:

```
 p3 ──▶ [X] ──▶ [Y] ──┐
                      ▼
               p2 ──▶ [A] ──▶ [A] ──▶ [$] ──▶ null
                               ▲       ▲
                        p1 ────┘       └──── fondo
```

| Propiedad de la pila enlazada | Por qué importa en el autómata |
|---|---|
| Apilar y desapilar cuestan O(1): crear un nodo o mover una referencia | Cada transición es un paso barato, sin importar el tamaño de la pila |
| No tiene tamaño máximo: cada `new` agrega un nodo | Es la "memoria sin límite" de la definición |
| Desapilar no destruye nada: la pila anterior sigue existiendo | Un AP no determinista explora varias ramas a la vez, y cada rama tiene **su propia** pila sin tener que copiarla |

> **Ojo con desapilar una pila vacía.** Si el tope es `null`, `tope.getSimbolo()` lanza `NullPointerException` (con `ArrayDeque`, `pop()` lanzaría `NoSuchElementException`). Por eso los autómatas usan el símbolo `$` en el fondo: mientras se vea `$`, se sabe que la pila está "vacía" de símbolos útiles sin intentar leer más abajo.

#### Definición formal

Un AP es una séptupla **P = (Q, Σ, Γ, δ, q₀, Z₀, F)**:

| Componente | Significado |
|---|---|
| **Q** | Conjunto finito de estados |
| **Σ** | Alfabeto de entrada |
| **Γ** | Alfabeto de la pila (los símbolos que se pueden apilar) |
| **δ** | Función de transición: `δ(q, a, X)` = conjunto de pares `(p, γ)` |
| **q₀** | Estado inicial |
| **Z₀** | Símbolo con el que empieza la pila (aquí, `$`) |
| **F** | Estados de aceptación |

Cada transición se lee: **"estando en el estado `q`, leyendo el símbolo `a` y con `X` en el tope: pasa al estado `p`, quita `X` de la pila y pon `γ` en su lugar"**. Se escribe `a, X → γ`:

| Transición | Efecto sobre la pila |
|---|---|
| `a, $ → A$` | Quita `$` y pone `A$`: **apila** una `A` (el primer símbolo de `γ` queda arriba) |
| `b, A → ε` | Quita `A` y no pone nada: **desapila** |
| `a, X → X` | Quita `X` y vuelve a poner `X`: la pila **no cambia** |
| `ε, X → …` | Se mueve **sin leer** ningún símbolo de la entrada |

Todas las transiciones siguen el mismo patrón, **quitar el tope y poner γ**. Así, apilar, desapilar y "no tocar" son casos de una sola operación. Por eso para apilar una `A` sobre `$` se escribe `$ → A$`: se quita el `$` y se vuelve a poner, con la `A` encima.

El autómata **acepta** una cadena si, tras leerla completa, está en un **estado de aceptación**.

Una **configuración** describe la situación completa en un instante: **(estado, entrada que falta por leer, contenido de la pila)**. Cada transición lleva de una configuración a otra.

| | Autómata finito | Autómata de pila |
|---|---|---|
| Situación en un instante | (estado, entrada que falta) | (estado, entrada que falta, **pila**) |
| Cuántas situaciones distintas hay | Finitas | **Infinitas**: la pila puede crecer sin límite |
| La transición depende de | Estado y símbolo | Estado, símbolo **y tope** |

Ese número infinito de configuraciones es la memoria que le faltaba al AFD: dos prefijos `aⁱ` y `aʲ` llevan a pilas distintas (`Aⁱ$` y `Aʲ$`) aunque el estado sea el mismo.

> **Ojo con el modo de aceptación.** En este material un AP acepta si termina la entrada en un **estado de aceptación**. Algunos libros usan otra definición: aceptar si la **pila queda vacía**. Las dos son equivalentes en poder (cualquier AP de un tipo se puede convertir al otro), pero un mismo autómata puede aceptar cosas distintas según la definición que se use.

#### Determinismo

Los APs pueden ser **no deterministas** (varias transiciones posibles en una misma configuración, como en un AFN). A diferencia de lo que pasa con los autómatas finitos, aquí el no determinismo **sí aumenta el poder**: hay lenguajes libres de contexto (como los palíndromos) que solo reconoce un AP no determinista.

**Por qué aquí sí y en los autómatas finitos no.** Con un AFN, la construcción de subconjuntos funciona porque el conjunto de estados posibles es **finito**. En un AP, cada rama tiene su propia **pila**, y el conjunto de pilas posibles es infinito: no se puede juntar en un solo estado determinista. Por eso no existe una "construcción de subconjuntos" para autómatas de pila.

#### Ejemplo: `aⁿbⁿ`

Estrategia: apilar una `A` por cada `a`; desapilar una por cada `b`; si el fondo `$` queda a la vista al terminar, aceptar.

| # | Transición | Significado |
|---|---|---|
| 1 | `q0, ε, $ → q2, $` | Cadena vacía: la pila no tiene `A` pendientes |
| 2 | `q1, ε, $ → q2, $` | Ya se desapilaron todas las `A`: aceptar |
| 3 | `q0, a, $ → q0, A$` | Primera `a`: apilar `A` |
| 4 | `q0, a, A → q0, AA` | Cada `a` siguiente apila otra `A` |
| 5 | `q0, b, A → q1, ε` | Primera `b`: desapilar y pasar a la fase de las `b` |
| 6 | `q1, b, A → q1, ε` | Cada `b` desapila una `A` |

```
 a, $ → A$                    b, A → ε
 a, A → AA
   ┌───┐                       ┌───┐
   ▼   │                       ▼   │
 ──▶ (q0) ───── b, A → ε ────▶ (q1) ──── ε, $ → $ ────▶ ((q2))
      │                                                     ▲
      └──────────────────── ε, $ → $ ───────────────────────┘
```

Los estados guardan la **fase** (q0: leyendo `a`; q1: leyendo `b`) y la pila guarda la **cuenta**. Separar las fases en dos estados es lo que impide aceptar `abab`: una vez en q1 no hay ninguna transición que lea una `a`.

Computación de `aabb`:

```
(q0, aabb, $)  ──a──▶  (q0, abb, A$)  ──a──▶  (q0, bb, AA$)  ──b──▶  (q1, b, A$)
               ──b──▶  (q1, ε, $)  ──ε──▶  (q2, ε, $)        q2 ∈ F  →  ACEPTADA
```

| Paso | Configuración | Transición aplicada | Efecto en la pila |
|---|---|---|---|
| 0 | (q0, aabb, $) | 3: `a, $ → A$` | Apila A |
| 1 | (q0, abb, A$) | 4: `a, A → AA` | Apila otra A |
| 2 | (q0, bb, AA$) | 5: `b, A → ε` | Desapila; pasa a q1 |
| 3 | (q1, b, A$) | 6: `b, A → ε` | Desapila |
| 4 | (q1, ε, $) | 2: `ε, $ → $` | No cambia; pasa a q2 |
| 5 | (q2, ε, $) | — | Entrada leída y q2 ∈ F: **aceptada** |

Con `aab` queda `(q1, ε, A$)`: sobra una `A` en la pila, el tope no es `$` y no se puede pasar a q2: **rechazada**. Con `abab` el autómata llega a `(q1, ab, $)` y desde ahí no existe ninguna transición que lea otra `a`: **rechazada**.

#### El autómata de pila en Java

Cada transición es un objeto `TransicionPila`; cada configuración, un objeto `Configuracion` que guarda el estado, cuántos símbolos se han leído y una referencia al **tope** de su pila enlazada. Como el AP puede ser no determinista, el simulador **explora todas las configuraciones alcanzables** y acepta si alguna llega a un estado final con toda la entrada leída.

```java
public class TransicionPila {

    private String desde = "";
    private Character lee = null;     // null significa ε (no lee nada de la entrada)
    private char tope;
    private String hasta = "";
    private String apila = "";        // reemplaza al tope; "" = solo desapilar

    public TransicionPila(String desde, Character lee, char tope, String hasta, String apila) {
        this.desde = desde;
        this.lee = lee;
        this.tope = tope;
        this.hasta = hasta;
        this.apila = apila;
    }

    public String getDesde() { return desde; }
    public Character getLee() { return lee; }
    public char getTope() { return tope; }
    public String getHasta() { return hasta; }
    public String getApila() { return apila; }
}
```

```java
public class Configuracion {

    private String estado = "";
    private int posicion = 0;          // cuántos símbolos de la entrada ya se leyeron
    private NodoPila pila = null;      // tope de la pila

    public Configuracion(String estado, int posicion, NodoPila pila) {
        this.estado = estado;
        this.posicion = posicion;
        this.pila = pila;
    }

    public String getEstado() { return estado; }
    public int getPosicion() { return posicion; }
    public NodoPila getPila() { return pila; }
}
```

El símbolo leído es `Character` (con mayúscula) y no `char` porque necesita poder valer `null` para representar ε; un `char` siempre tiene algún valor. Al llamar `agregar("q0", 'a', ...)`, Java convierte el `'a'` en `Character` automáticamente (*autoboxing*).

```java
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class AutomataDePila implements Reconocedor {

    private ArrayList<TransicionPila> transiciones = new ArrayList<TransicionPila>();
    private String inicial = "";
    private char fondo = '$';
    private HashSet<String> finales = new HashSet<String>();

    public AutomataDePila(String inicial, char fondo, String... finales) {
        this.inicial = inicial;
        this.fondo = fondo;
        for (String f : finales) this.finales.add(f);
    }

    public void agregar(String desde, Character lee, char tope, String hasta, String apila) {
        transiciones.add(new TransicionPila(desde, lee, tope, hasta, apila));
    }

    public boolean acepta(String cadena) {
        return acepta(cadena, false);
    }

    public boolean acepta(String cadena, boolean traza) {
        HashSet<String> visitadas = new HashSet<String>();
        LinkedList<Configuracion> pendientes = new LinkedList<Configuracion>();   // usada como pila (push/pop)
        pendientes.push(new Configuracion(inicial, 0, new NodoPila(fondo, null)));

        while (!pendientes.isEmpty()) {
            Configuracion c = pendientes.pop();
            String pila = NodoPila.texto(c.getPila());

            if (!visitadas.add(c.getEstado() + "|" + c.getPosicion() + "|" + pila)) continue;   // ya explorada
            if (pila.length() > 2 * cadena.length() + 4) continue;   // protección contra bucles de transiciones ε

            if (traza) {
                String resto = cadena.substring(c.getPosicion());
                System.out.println("  (" + c.getEstado() + ", " + (resto.isEmpty() ? "ε" : resto) + ", " + pila + ")");
            }

            if (c.getPosicion() == cadena.length() && finales.contains(c.getEstado())) return true;
            if (c.getPila() == null) continue;                       // pila vacía: no hay tope que consultar

            char tope = c.getPila().getSimbolo();

            for (TransicionPila t : transiciones) {
                if (!t.getDesde().equals(c.getEstado()) || t.getTope() != tope) continue;

                int nuevaPos = c.getPosicion();
                if (t.getLee() != null) {                             // la transición lee un símbolo
                    if (nuevaPos >= cadena.length() || cadena.charAt(nuevaPos) != t.getLee()) continue;
                    nuevaPos++;
                }

                // Quitar el tope (quedarse con el de abajo) y poner γ encima
                NodoPila nuevaPila = NodoPila.apilar(t.getApila(), c.getPila().getSiguiente());
                pendientes.push(new Configuracion(t.getHasta(), nuevaPos, nuevaPila));
            }
        }

        return false;
    }
}
```

**Qué pasa por dentro de `acepta`.** Las configuraciones forman un grafo (una configuración lleva a otras), y `acepta` lo recorre en **profundidad**: `pendientes` es una `LinkedList` usada como **pila** (`push` mete al frente, `pop` saca del frente). La línea clave es la que construye la pila nueva:

```
 NodoPila.apilar(t.getApila(), c.getPila().getSiguiente())
                               └──── quita el tope: se queda con el nodo de abajo
              └──── pone γ encima de lo que quedó
```

La configuración anterior **no se modifica**: su pila sigue intacta, y la nueva solo agrega nodos encima de la parte compartida. Gracias a eso, varias ramas del no determinismo pueden convivir en `pendientes` sin estorbarse.

| Pieza | Para qué sirve |
|---|---|
| `visitadas` | Una misma configuración puede alcanzarse por caminos distintos; se explora una sola vez |
| Límite `2 * cadena.length() + 4` | Una transición ε que apile sin leer podría hacer crecer la pila para siempre; las pilas demasiado grandes se descartan |
| `c.getPila() == null` | Si la pila quedó totalmente vacía (ni siquiera `$`), no hay tope que comparar y ninguna transición aplica |
| Regresar `true` en cuanto una rama acepta | Basta **una** computación que acepte (igual que en un AFN) |

> **Ojo con el límite de la pila.** El límite `2 * cadena.length() + 4` es una simplificación práctica para los ejemplos de este material, no parte de la definición formal: en teoría la pila no tiene tope. Alcanza para autómatas en los que cada símbolo leído apila a lo más un par de símbolos.

El autómata de `aⁿbⁿ`:

```java
AutomataDePila anbnAP = new AutomataDePila("q0", '$', "q2");
anbnAP.agregar("q0", null, '$', "q2", "$");     // 1
anbnAP.agregar("q1", null, '$', "q2", "$");     // 2
anbnAP.agregar("q0", 'a',  '$', "q0", "A$");    // 3  apilar A
anbnAP.agregar("q0", 'a',  'A', "q0", "AA");    // 4
anbnAP.agregar("q0", 'b',  'A', "q1", "");      // 5  desapilar
anbnAP.agregar("q1", 'b',  'A', "q1", "");      // 6

String[] pruebas = { "", "ab", "aabb", "aaabbb", "aab", "abab", "ba" };
for (String w : pruebas)
    System.out.printf("%-7s -> %b%n", w.isEmpty() ? "ε" : w, anbnAP.acepta(w));

// → ε       -> true
// → ab      -> true
// → aabb    -> true
// → aaabbb  -> true
// → aab     -> false
// → abab    -> false
// → ba      -> false

anbnAP.acepta("aabb", true);
// →   (q0, aabb, $)
// →   (q0, abb, A$)
// →   (q0, bb, AA$)
// →   (q1, b, A$)
// →   (q1, ε, $)
// →   (q2, ε, $)
```

Con una cadena rechazada, la traza muestra cómo el simulador **agota todas las ramas** antes de responder `false`:

```java
anbnAP.acepta("aab", true);
// →   (q0, aab, $)
// →   (q0, ab, A$)
// →   (q0, b, AA$)
// →   (q1, ε, A$)
// →   (q2, aab, $)
```

En `(q1, ε, A$)` ya no hay transiciones: sobra una `A`. Entonces el simulador saca de `pendientes` la otra rama que había dejado al principio: la transición 1 (`ε, $ → $`) desde la configuración inicial, que lleva a `(q2, aab, $)`. Esa rama está en un estado de aceptación, pero **sin haber leído la entrada**, así que tampoco acepta. Ya no queda nada pendiente: **rechazada**.

#### Ejemplo: paréntesis balanceados

La misma idea: apilar con `(`, desapilar con `)`. Si se intenta desapilar con el fondo `$` a la vista (un `)` de más), no hay transición y la cadena se rechaza.

```java
AutomataDePila parentesisAP = new AutomataDePila("q0", '$', "q1");
parentesisAP.agregar("q0", null, '$', "q1", "$");   // el fondo está a la vista: todo quedó emparejado
parentesisAP.agregar("q0", '(',  '$', "q0", "X$");
parentesisAP.agregar("q0", '(',  'X', "q0", "XX");
parentesisAP.agregar("q0", ')',  'X', "q0", "");

String[] pruebas = { "", "()", "(())", "(()())", "(", ")(", "(()", "())" };
for (String w : pruebas)
    System.out.printf("%-7s -> %b%n", w.isEmpty() ? "ε" : w, parentesisAP.acepta(w));

// → ε       -> true
// → ()      -> true
// → (())    -> true
// → (()())  -> true
// → (       -> false
// → )(      -> false
// → (()     -> false
// → ())     -> false
```

Cada tipo de error se detecta en un lugar distinto:

| Cadena | Qué falla | Dónde se detecta |
|---|---|---|
| `(`, `(()` | Sobran `(`: al terminar quedan `X` en la pila | El tope no es `$`, no se puede pasar a q1 |
| `)(` | Un `)` llega con la pila en `$` | No hay transición `)` con tope `$`: la rama muere de inmediato |
| `())` | El segundo `)` llega con `$` a la vista | Igual que el anterior, pero a mitad de la cadena |

A diferencia de `aⁿbⁿ`, aquí basta con **un estado** de lectura (q0): los paréntesis se pueden abrir y cerrar en cualquier orden mientras el tope lo permita. La pila sola lleva toda la información.

#### Ejemplo: palíndromos (no determinista)

Para reconocer `w wᴿ` (una cadena seguida de su reverso) hay que apilar la primera mitad y **comparar** con la segunda. El problema: el autómata **no sabe dónde está el centro**. La solución es **adivinarlo**: en cada posición tiene la opción de "empezar a comparar aquí". Si alguna apuesta acierta, la cadena se acepta.

```java
AutomataDePila palindromosAP = new AutomataDePila("q0", '$', "q2");

for (char s : "ab".toCharArray())
    for (char tope : "ab$".toCharArray()) {
        palindromosAP.agregar("q0", s, tope, "q0", "" + s + tope);   // apilar s sobre el tope
        palindromosAP.agregar("q0", s, tope, "q1", "" + tope);       // s es el símbolo central (longitud impar)
    }

for (char tope : "ab$".toCharArray())
    palindromosAP.agregar("q0", null, tope, "q1", "" + tope);        // el centro está entre dos símbolos (longitud par)

for (char s : "ab".toCharArray())
    palindromosAP.agregar("q1", s, s, "q1", "");                     // comparar con el tope y desapilar

palindromosAP.agregar("q1", null, '$', "q2", "$");                   // todo coincidió: aceptar

String[] pruebas = { "", "a", "abba", "aba", "aabaa", "ab", "abab" };
for (String w : pruebas)
    System.out.printf("%-7s -> %b%n", w.isEmpty() ? "ε" : w, palindromosAP.acepta(w));

// → ε       -> true
// → a       -> true
// → abba    -> true
// → aba     -> true
// → aabaa   -> true
// → ab      -> false
// → abab    -> false
```

Para `abba` el autómata prueba todos los centros posibles. En la tabla están los centros "entre dos símbolos" (longitud par); las apuestas de longitud impar, que toman un símbolo como centro, también mueren. Solo una rama funciona:

| Centro que se apuesta | Primera mitad apilada | Se compara con | Resultado de la rama |
|---|---|---|---|
| Antes de todo | (nada) | `abba` contra `$` | Muere: `a` no coincide con `$` |
| Después de `a` | `a` | `bba` | Muere: `b` no coincide con `a` |
| Después de `ab` | `ab` (tope `b`) | `ba` | `b`=`b`, `a`=`a`, aparece `$`: **acepta** |
| Después de `abb` | `abb` | `a` | Muere: `a` no coincide con `b` |

Ningún AP **determinista** puede reconocer los palíndromos: al leer la cadena de izquierda a derecha, en ningún momento hay información suficiente para saber que ya se llegó a la mitad. Es el ejemplo clásico de que, en los autómatas de pila, el no determinismo sí agrega poder.

### 4.2.2. Relación entre autómata de pila y lenguaje libre de contexto

> **Teorema.** Un lenguaje es **libre de contexto** si y solo si existe un **autómata de pila** que lo acepta.

Es el equivalente, para el tipo 2, de lo que el teorema de Kleene decía para el tipo 3:

| Tipo | Se *genera* con… | Se *reconoce* con… |
|---|---|---|
| 3 (regular) | Gramática regular / expresión regular | Autómata **finito** |
| 2 (libre de contexto) | Gramática libre de contexto | Autómata **de pila** |

**Por qué importa.** El teorema une las dos mitades de la unidad: la gramática es la forma cómoda de **describir** un lenguaje (así se publica la sintaxis de un lenguaje de programación), y el autómata de pila es la forma de **reconocerlo** con un programa. Saber que siempre se puede pasar de una a otra es lo que permite construir analizadores sintácticos a partir de la gramática del lenguaje.

#### De gramática a autómata de pila

La construcción es sorprendentemente simple: el autómata **simula la derivación por la izquierda** usando la pila como forma sentencial.

| Situación (tope de la pila) | Transición |
|---|---|
| Al inicio | Apilar el símbolo inicial: `ε, $ → S$` |
| Tope = **no terminal** `A`, para cada producción `A → α` | Reemplazarlo por `α`, **sin leer entrada**: `ε, A → α` |
| Tope = **terminal** `a` | Si coincide con el símbolo de entrada, **desapilarlo**: `a, a → ε` |
| Fondo `$` a la vista | Aceptar: `ε, $ → $` |

Es decir: la pila **conserva lo que falta por producir**. Cada vez que arriba hay un no terminal, se "adivina" qué producción aplicar; cada vez que arriba hay un terminal, se **verifica** contra la entrada. Esto vuelve a ser no determinista: en `S → aSb | ε` hay que elegir entre dos reglas.

En todo momento se cumple esta relación, que explica por qué la construcción es correcta:

> **(lo ya leído de la entrada) + (contenido de la pila sin el `$`) = una forma sentencial de la derivación por la izquierda**

Por ejemplo, en la configuración `(q1, b, Sb$)` de abajo ya se leyó `a` y la pila tiene `Sb`: juntos forman `aSb`, que es justo un paso de la derivación de `ab`. Al vaciarse la pila con toda la entrada leída, la forma sentencial es la cadena completa.

Con `S → aSb | ε`, el autómata resultante tiene solo estas transiciones:

| Transición | Origen |
|---|---|
| `q0, ε, $ → q1, S$` | Apilar el símbolo inicial |
| `q1, ε, S → q1, aSb` | Producción `S → aSb` |
| `q1, ε, S → q1, ε` | Producción `S → ε` |
| `q1, a, a → q1, ε` | Verificar el terminal `a` |
| `q1, b, b → q1, ε` | Verificar el terminal `b` |
| `q1, ε, $ → q2, $` | Aceptar |

Computación de `ab` (la pila siempre contiene lo que **falta** derivar):

| Configuración | Transición que se aplica a continuación |
|---|---|
| `(q0, ab, $)` | Apilar el símbolo inicial |
| `(q1, ab, S$)` | Producción `S → aSb` |
| `(q1, ab, aSb$)` | Verificar `a` contra la entrada y desapilar |
| `(q1, b, Sb$)` | Producción `S → ε` |
| `(q1, b, b$)` | Verificar `b` contra la entrada y desapilar |
| `(q1, ε, $)` | Fondo a la vista: aceptar |
| `(q2, ε, $)` | Estado de aceptación con toda la entrada leída: **ACEPTADA** |

#### La construcción en Java

Se agrega este método a la clase `AutomataDePila` (con `import java.util.Map;`); recibe cualquier `Gramatica` y devuelve el autómata equivalente:

```java
public static AutomataDePila desdeGramatica(Gramatica g) {
    AutomataDePila ap = new AutomataDePila("q0", '$', "q2");

    ap.agregar("q0", null, '$', "q1", g.getInicial() + "$");          // apilar el símbolo inicial

    for (Map.Entry<Character, ArrayList<String>> par : g.getProducciones().entrySet())
        for (String alfa : par.getValue())
            ap.agregar("q1", null, par.getKey(), "q1", alfa);          // A → α: reemplazar el tope

    for (char a : g.terminales())
        ap.agregar("q1", a, a, "q1", "");                              // terminal en el tope: verificar y desapilar

    ap.agregar("q1", null, '$', "q2", "$");                            // fondo a la vista: aceptar
    return ap;
}
```

Al construir la transición `ε, A → α`, la cadena `alfa` (por ejemplo `"aSb"`) se apila completa con `NodoPila.apilar`: queda `a` en el tope, luego `S`, luego `b`. Es justo el orden en que hay que seguir derivando: primero se verifica la `a`, después se expande `S`, y la `b` espera abajo su turno.

Se comprueba que el autómata y la gramática describen el **mismo lenguaje** (hasta longitud 8), usando `Equivalencia.equivalentes` de la Unidad 3. El autómata implementa `Reconocedor`; el lenguaje generado se adapta con una lambda que pregunta si la cadena está en el conjunto:

```java
AutomataDePila apDeAnbn = AutomataDePila.desdeGramatica(anbn);
TreeSet<String> lenguajeAnbn = anbn.genera(8);

System.out.println(apDeAnbn.acepta("aabb"));    // → true
System.out.println(apDeAnbn.acepta("aab"));     // → false
System.out.println(Equivalencia.equivalentes(apDeAnbn, w -> lenguajeAnbn.contains(w), "ab", 8));            // → true

AutomataDePila apDePalindromos = AutomataDePila.desdeGramatica(palindromos);
TreeSet<String> lenguajePalindromos = palindromos.genera(8);

System.out.println(Equivalencia.equivalentes(apDePalindromos, w -> lenguajePalindromos.contains(w), "ab", 8));   // → true
```

La comprobación revisa 2⁹ − 1 = 511 cadenas sobre {a, b} de longitud 0 a 8. De ellas, la gramática de palíndromos genera 61 (1 + 2 + 2 + 4 + 4 + 8 + 8 + 16 + 16, según la longitud), y el autómata acepta exactamente esas mismas.

La traza de `ab` muestra el no determinismo de la construcción: el simulador prueba primero `S → ε`, se atora y regresa a probar `S → aSb`:

```java
apDeAnbn.acepta("ab", true);
// →   (q0, ab, $)
// →   (q1, ab, S$)
// →   (q1, ab, $)        ← probó S → ε
// →   (q2, ab, $)        ← llegó a q2, pero sin leer la entrada: no acepta
// →   (q1, ab, aSb$)     ← regresa y prueba S → aSb
// →   (q1, b, Sb$)
// →   (q1, b, b$)
// →   (q1, ε, $)
// →   (q2, ε, $)
```

> **Ojo con el orden de exploración.** Que se pruebe primero `S → ε` es consecuencia de usar una pila de pendientes: la transición de `S → ε` se agregó **después** que la de `S → aSb`, así que queda arriba y sale primero. El resultado final no depende de ese orden (si alguna rama acepta, se encuentra), pero la traza sí.

#### De autómata de pila a gramática

El sentido contrario existe (todo AP puede convertirse en una GLC), pero la construcción es larga: los no terminales de la gramática representan "ir del estado `p` al estado `q` dejando la pila como estaba". Para el curso basta con saber que **el teorema asegura que siempre es posible**.

#### Lo que un autómata de pila no puede hacer

Con **una sola pila** solo se puede comparar **dos** cantidades encadenadas. Por eso `aⁿbⁿ` sí se puede, pero `aⁿbⁿcⁿ` **no**: al contar las `b` contra las `a` la pila se vacía, y ya no queda con qué comparar las `c`. Para ese lenguaje se necesita una máquina con más poder (tipo 1 de la jerarquía).

| Lenguaje | ¿Lo reconoce un AP? | Por qué |
|---|---|---|
| `aⁿbⁿ` | Sí | Apila con `a`, desapila con `b`: una sola comparación |
| `aⁿbᵐcᵐ` | Sí | Las `a` se leen sin apilar; se compara `b` contra `c` |
| `aⁿbⁿcᵐ` | Sí | Se compara `a` contra `b`; las `c` se leen sin apilar |
| `aⁿbⁿcⁿ` | **No** | Hay que comparar `a` con `b` **y** `b` con `c`; la primera comparación vacía la pila |
| `w w` (una cadena repetida, como `abab`) | **No** | La pila devuelve la primera mitad **al revés**, y aquí hay que compararla en el mismo orden |

> **Ojo con el último caso.** `w wᴿ` (palíndromos de longitud par) **sí** es libre de contexto, pero `w w` **no**. La diferencia es solo el orden de la segunda mitad, y es decisiva: la pila invierte el orden (LIFO), así que sirve para comparar contra el reverso, no contra una copia.
