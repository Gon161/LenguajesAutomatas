# Unidad 2. Fundamentos de árboles

Un **árbol** es un grafo con una forma muy específica: sin ciclos y de una sola pieza. Esa restricción lo vuelve ideal para representar **jerarquías**: carpetas dentro de carpetas, el organigrama de una empresa, el índice de un libro.

En esta materia el árbol cumple una función concreta: el **árbol de derivación** muestra, paso a paso, cómo una gramática construye una cadena. Quien sabe leer un árbol de derivación entiende por qué una cadena pertenece (o no) a un lenguaje, y entiende cómo un compilador analiza un programa.

> En esta unidad se reutiliza la clase `Grafo` (con `Nodo`) de la Unidad 1 y se construye una clase nueva, `NodoArbol`: un nodo que guarda **referencias** a sus hijos. Igual que el grafo, el árbol es una **estructura enlazada** de objetos. Los ejemplos usan `java.util` (`ArrayList`, `HashMap`, `LinkedList`, `Arrays`).

## 2.1. Estructura básica de un árbol

### 2.1.1. Árbol como caso particular de grafo (acíclico y conexo)

Un **árbol** es un grafo **no dirigido** que cumple dos condiciones a la vez:

| Condición | Significa que… | Si falla |
|---|---|---|
| **Conexo** | Desde cualquier nodo se puede llegar a cualquier otro | El grafo está "partido" en varios pedazos (un **bosque**) |
| **Acíclico** | No existe ningún camino que regrese a su punto de partida | Hay un ciclo: ya no es árbol |

Comparación de tres grafos de 4 nodos:

```
  (1) Árbol          (2) Con ciclo        (3) Desconexo

   A ── B              A ── B              A ── B
   │                   │    │
   C ── D              C ── D              C ── D   (sin unión con A, B)
  conexo y             conexo pero
  sin ciclos           con ciclo A-B-D-C
```

Solo el (1) es un árbol.

| Característica | Descripción |
|---|---|
| Conexo | Ningún nodo queda aislado del resto |
| Acíclico | No hay forma de salir de un nodo y regresar a él sin repetir una arista |
| Camino único | Entre dos nodos cualesquiera hay **exactamente un** camino simple |
| Mínimamente conexo | Si se quita **cualquier** arista, deja de ser conexo |
| Maximalmente acíclico | Si se agrega **cualquier** arista nueva, aparece un ciclo |
| Aristas | Siempre **n − 1**, con n nodos |

**Por qué interesa esta forma.** Las dos condiciones juntas garantizan que entre dos nodos hay **un solo camino**. Eso es lo que hace útil al árbol para representar jerarquías y análisis: cada elemento tiene un lugar único y una sola forma de llegar a él. En un grafo con ciclos habría varias rutas y, en el caso de una gramática, varias interpretaciones de la misma cadena (se ve al final de la unidad).

Un **bosque** es un grafo acíclico pero no necesariamente conexo: un conjunto de árboles separados. El grafo (3) de arriba es un bosque de dos árboles.

#### Propiedades que se derivan

- Entre **cualquier par de nodos existe exactamente un camino**. Si hubiera dos, habría un ciclo.
- Un árbol de **n** nodos tiene siempre **n − 1** aristas. Si se quita una, se parte en dos; si se agrega una, aparece un ciclo.
- Un grafo conexo con **n** nodos y **n − 1** aristas es un árbol.

| Nodos | Aristas de un árbol |
|---|---|
| 1 | 0 |
| 2 | 1 |
| 5 | 4 |
| 10 | 9 |

**Por qué siempre n − 1.** Un árbol se puede construir empezando por un solo nodo y agregando, uno por uno, nodos nuevos colgados de alguno que ya está. Cada nodo nuevo llega con **exactamente una** arista (la que lo une al árbol): si llegara con dos, cerraría un ciclo; si llegara con cero, quedaría suelto.

| Paso | Se agrega | Nodos | Aristas |
|---|---|---|---|
| 0 | El nodo A, solo | 1 | 0 |
| 1 | B colgado de A | 2 | 1 |
| 2 | C colgado de A | 3 | 2 |
| 3 | D colgado de B | 4 | 3 |
| n − 1 | El último nodo | n | n − 1 |

Siempre hay una arista menos que nodos, porque el primer nodo es el único que no trajo arista.

Las tres propiedades dan varias **definiciones equivalentes**: basta con comprobar dos de estas tres condiciones para saber que un grafo de n nodos es un árbol.

| Si el grafo es… | …y además… | entonces es un árbol |
|---|---|---|
| Conexo | Acíclico | Sí (es la definición) |
| Conexo | Tiene n − 1 aristas | Sí |
| Acíclico | Tiene n − 1 aristas | Sí |

> **Ojo con contar solo aristas.** Tener n − 1 aristas **no basta** por sí solo. Un triángulo A─B─C más un nodo D suelto tiene 4 nodos y 3 aristas, pero no es árbol: tiene un ciclo y además está partido. Hace falta la segunda condición (conexo o acíclico).

#### ¿Un grafo dado es un árbol? (en Java)

La última propiedad da un criterio directo: se comprueba que el grafo sea conexo y que tenga `n − 1` aristas. Se usa la clase `Grafo` (no dirigida) de la Unidad 1; como cada arista está guardada dos veces (una por cada extremo), el total de aristas es la suma de los tamaños de las listas entre 2. Es el **lema del apretón de manos** de 1.1.2: la suma de grados es el doble del número de aristas.

```java
static boolean esArbol(Grafo grafo) {
    ArrayList<Nodo> nodos = grafo.getNodos();
    if (nodos.isEmpty()) return false;

    int sumaGrados = 0;
    for (Nodo n : nodos)
        sumaGrados += n.getVecinos().size();
    int aristas = sumaGrados / 2;                  // cada arista está guardada dos veces

    String primero = nodos.get(0).getId();
    for (Nodo n : nodos)
        if (!grafo.alcanzable(primero, n.getId())) return false;   // no es conexo

    return aristas == nodos.size() - 1;
}
```

Para saber si es conexo basta con comprobar que **desde un nodo cualquiera** (el primero) se alcanzan todos los demás. No hace falta probar todos los pares: si todos se alcanzan desde el primero, en un grafo no dirigido cualquier par se une pasando por él.

```java
Grafo arbol = new Grafo(false);
arbol.agregarArista("A", "B");
arbol.agregarArista("A", "C");
arbol.agregarArista("B", "D");
arbol.agregarArista("B", "E");
System.out.println(esArbol(arbol));       // → true    (5 nodos, 4 aristas, conexo)

Grafo conCiclo = new Grafo(false);
conCiclo.agregarArista("A", "B");
conCiclo.agregarArista("B", "C");
conCiclo.agregarArista("C", "A");
System.out.println(esArbol(conCiclo));    // → false   (3 nodos, 3 aristas)

Grafo desconexo = new Grafo(false);
desconexo.agregarArista("A", "B");
desconexo.agregarArista("C", "D");
System.out.println(esArbol(desconexo));   // → false   (no se llega de A a C)

Grafo engano = new Grafo(false);
engano.agregarArista("A", "B");
engano.agregarArista("B", "C");
engano.agregarArista("C", "A");
engano.agregarNodo("D");
System.out.println(esArbol(engano));      // → false   (4 nodos y 3 aristas, pero no es conexo)
```

Traza del primer caso:

| Dato | Valor | Cálculo |
|---|---|---|
| Nodos | A, B, C, D, E | n = 5 |
| Tamaños de las listas | A: 2, B: 3, C: 1, D: 1, E: 1 | suma = 8 |
| Aristas | 4 | 8 / 2 |
| ¿Se alcanzan todos desde A? | Sí | conexo |
| ¿aristas == n − 1? | 4 == 4 | → `true` |

Este método resuelve también el problema que quedó pendiente en la Unidad 1 (no se pueden buscar ciclos en un no dirigido con `tieneCiclo`, porque cada arista parece un ciclo A → B → A): en lugar de buscar ciclos, se **cuentan aristas**, y la cuenta no se deja engañar por la doble flecha.

> Esta función supone un grafo **simple**: sin lazos y sin dos aristas repetidas entre los mismos nodos. Un lazo `{A, A}` se guarda una sola vez (por la condición `a != b` de `agregarArista`), así que la suma entre 2 ya no daría el número real de aristas.

#### Árbol con raíz

En los árboles que se usan en computación se **elige un nodo como raíz** y se dibuja arriba; todas las aristas pasan a tener un sentido natural: del nodo de arriba (**padre**) hacia los de abajo (**hijos**). Así el árbol deja de ser un grafo no dirigido y se convierte en un **grafo dirigido sin ciclos** donde cada nodo, salvo la raíz, tiene exactamente un padre.

```
        A              Se escoge A como raíz y se dibuja hacia abajo:
       / \             las aristas ahora van de padre a hijo.
      B   C
     / \
    D   E
```

**Cualquier** nodo puede elegirse como raíz; cada elección da un árbol con raíz distinto a partir del mismo árbol no dirigido. Si en el ejemplo se tomara B como raíz, B quedaría arriba con hijos A, D y E, y C pasaría a ser hijo de A.

```
  Raíz A               Raíz B (mismo árbol, otra raíz)

      A                       B
     / \                    / | \
    B   C                  A  D  E
   / \                     |
  D   E                    C
```

Que cada nodo tenga **un único padre** es consecuencia directa del camino único: el padre de un nodo es el siguiente nodo en el (único) camino hacia la raíz.

De aquí en adelante, "árbol" significa **árbol con raíz**.

### 2.1.2. Raíz, nodo hoja, nodo interno y nivel

#### Vocabulario

Todos los términos se explican sobre este árbol:

```
              A                 nivel 0
           /  |  \
          B   C   D             nivel 1
         / \       \
        E   F       G           nivel 2
                    |
                    H           nivel 3
```

| Término | Significado | En el ejemplo |
|---|---|---|
| **Raíz** | Único nodo sin padre; de él parte todo el árbol | A |
| **Padre** | Nodo del que cuelga otro | A es padre de B, C y D |
| **Hijo** | Nodo que cuelga de otro | E y F son hijos de B |
| **Hermanos** | Hijos del mismo padre | B, C y D |
| **Hoja** | Nodo **sin hijos** | C, E, F, H |
| **Nodo interno** | Nodo **con al menos un hijo** (la raíz también lo es) | A, B, D, G |
| **Grado de un nodo** | Número de hijos que tiene | B tiene grado 2; C, grado 0 |
| **Ancestro** | Cualquier nodo en el camino hacia la raíz | De H: G, D y A |
| **Descendiente** | Cualquier nodo debajo de otro | De D: G y H |
| **Subárbol** | Un nodo junto con todos sus descendientes | El subárbol de B es {B, E, F} |
| **Nivel** | Distancia (en aristas) desde la raíz | A: 0; B: 1; E: 2; H: 3 |
| **Altura** | Nivel más profundo del árbol | 3 |

> **Ojo con el nivel de la raíz.** Depende del libro. En este material la raíz está en el nivel **0** (el nivel de un nodo es el número de aristas que lo separan de la raíz). Algunos textos empiezan en 1; si usas otra fuente, confirma con cuál cuenta.

Las **hojas** son importantes en esta materia: en un árbol de derivación, las hojas leídas de izquierda a derecha forman la cadena resultante.

Hay dos medidas que se confunden con frecuencia porque se miden en sentidos opuestos:

| Medida | Se cuenta… | Ejemplo con el nodo D |
|---|---|---|
| **Nivel** (o profundidad) de un nodo | **Hacia arriba**: aristas desde la raíz hasta el nodo | D está en el nivel 1 |
| **Altura** de un nodo | **Hacia abajo**: aristas desde el nodo hasta su hoja más lejana | D tiene altura 2 (D → G → H) |

La altura del árbol completo es la altura de la raíz, y coincide con el nivel de la hoja más profunda.

Dos cuentas sirven para comprobar un árbol sin dibujarlo:

| Relación | Por qué se cumple | En el ejemplo |
|---|---|---|
| Nodos = 1 + suma de los grados | Todo nodo, salvo la raíz, es hijo de **exactamente un** padre, así que se cuenta una sola vez como hijo | Grados: A 3, B 2, D 1, G 1 → 1 + 7 = **8** nodos |
| Aristas = nodos − 1 | Cada arista une a un hijo con su padre | 8 − 1 = **7** aristas |

Un **árbol binario** es aquel en que cada nodo tiene a lo más 2 hijos. En él, el nivel k puede tener como máximo **2ᵏ** nodos: 1 en el nivel 0, 2 en el 1, 4 en el 2, 8 en el 3. Los árboles de derivación no son binarios en general: un nodo tiene tantos hijos como símbolos tenga el lado derecho de la producción.

#### El árbol en Java

Un árbol con raíz se programa con un nodo que guarda su valor y la lista de sus hijos. Es la misma idea de la clase `Nodo` de la Unidad 1 (cada nodo guarda referencias a otros nodos), pero ahora las referencias van **solo de padre a hijo**:

```java
import java.util.ArrayList;

public class NodoArbol {

    private String valor = "";
    private ArrayList<NodoArbol> hijos = new ArrayList<NodoArbol>();   // de izquierda a derecha

    public NodoArbol(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public ArrayList<NodoArbol> getHijos() {
        return hijos;
    }

    // Crea un hijo nuevo y lo devuelve, para poder seguir colgando nodos de él
    public NodoArbol agregar(String valor) {
        NodoArbol hijo = new NodoArbol(valor);
        hijos.add(hijo);
        return hijo;
    }

    // Cuelga un nodo (o un subárbol completo) ya construido
    public void agregarHijo(NodoArbol hijo) {
        hijos.add(hijo);
    }

    public boolean esHoja() {
        return hijos.isEmpty();
    }

    // Nivel más profundo del subárbol que cuelga de este nodo
    public int altura() {
        if (esHoja()) return 0;

        int mayor = 0;
        for (NodoArbol h : hijos)
            mayor = Math.max(mayor, h.altura());
        return 1 + mayor;
    }

    public int contarHojas() {
        if (esHoja()) return 1;

        int total = 0;
        for (NodoArbol h : hijos)
            total += h.contarHojas();
        return total;
    }

    // Muestra el árbol con sangría según el nivel
    public void imprimir(int nivel) {
        String sangria = "";
        for (int i = 0; i < nivel; i++) sangria += "   ";

        System.out.println(sangria + valor + "  (nivel " + nivel + ")");
        for (NodoArbol h : hijos)
            h.imprimir(nivel + 1);
    }
}
```

| Diferencia con el `Nodo` del grafo | Por qué |
|---|---|
| Las referencias van solo hacia abajo (padre → hijos) | En un árbol con raíz, el sentido natural es de padre a hijo |
| El **orden** de la lista importa (izquierda a derecha) | En un árbol de derivación, el orden de los hijos es el orden de los símbolos en la cadena |
| No hace falta un conjunto de visitados | Al no haber ciclos, un recorrido desde la raíz **nunca** llega dos veces al mismo nodo |
| No hay referencia al padre | Para estos algoritmos no se necesita subir; si se necesitara, se agregaría un atributo `padre` |

Se construye el árbol del ejemplo:

```java
NodoArbol a = new NodoArbol("A");
NodoArbol b = a.agregar("B");
a.agregar("C");
NodoArbol d = a.agregar("D");
b.agregar("E");
b.agregar("F");
NodoArbol g = d.agregar("G");
g.agregar("H");

a.imprimir(0);
// → A  (nivel 0)
// →    B  (nivel 1)
// →       E  (nivel 2)
// →       F  (nivel 2)
// →    C  (nivel 1)
// →    D  (nivel 1)
// →       G  (nivel 2)
// →          H  (nivel 3)

System.out.println(a.altura());        // → 3
System.out.println(a.contarHojas());   // → 4  (C, E, F, H)
System.out.println(b.altura());        // → 1  (subárbol de B)
```

En memoria, la variable `a` es la única puerta de entrada al árbol: desde ella, siguiendo referencias, se llega a cualquier nodo. Las variables `b`, `d` y `g` son solo referencias **adicionales** a nodos que ya están colgados; no son copias. Por eso `b.agregar("E")` modifica el árbol que cuelga de `a`.

```
 a ──▶ [A | hijos: ●, ●, ●]
                │  │  └──▶ [D | ●] ──▶ [G | ●] ──▶ [H | vacía]
                │  └─────▶ [C | vacía]
                └────────▶ [B | ●, ●] ──▶ [E | vacía]
                                 └─────▶ [F | vacía]
 b ─────────────────────────▲  (la misma B, no otra)
```

La función `altura` es **recursiva**: la altura de un nodo es 0 si es hoja, y 1 más la altura de su hijo más profundo si no lo es. Casi todo lo que se hace con árboles se resuelve con esta misma idea: tratar a cada hijo como un árbol más pequeño.

Traza de `a.altura()` (las llamadas se resuelven de abajo hacia arriba: cada nodo espera la respuesta de sus hijos):

| Llamada | ¿Hoja? | Alturas de los hijos | Resultado |
|---|---|---|---|
| `E.altura()` | Sí | — | 0 |
| `F.altura()` | Sí | — | 0 |
| `B.altura()` | No | E: 0, F: 0 | 1 + 0 = **1** |
| `C.altura()` | Sí | — | 0 |
| `H.altura()` | Sí | — | 0 |
| `G.altura()` | No | H: 0 | 1 + 0 = **1** |
| `D.altura()` | No | G: 1 | 1 + 1 = **2** |
| `A.altura()` | No | B: 1, C: 0, D: 2 | 1 + 2 = **3** |

**Por qué la recursión siempre termina.** Cada llamada se hace sobre un subárbol **estrictamente más pequeño** (un hijo) y las hojas no llaman a nadie. Como el árbol no tiene ciclos, tarde o temprano todas las ramas llegan a una hoja. En un grafo con ciclos, la misma recursión sin control nunca terminaría (ver la nota sobre `toString` en la Unidad 1).

`imprimir` y `altura` recorren el árbol en órdenes distintos, y la diferencia importará en 2.2.2:

| Orden | Cuándo se procesa el nodo | Ejemplo en esta clase |
|---|---|---|
| **Preorden** | **Antes** que sus hijos | `imprimir`: escribe el nodo y luego baja a los hijos (A, B, E, F, C, D, G, H) |
| **Postorden** | **Después** que sus hijos | `altura`: necesita la respuesta de los hijos para calcular la suya (E, F, B, C, H, G, D, A) |

> **Ojo con la hoja vacía.** `esHoja()` pregunta si la lista de hijos está **vacía**, no si es `null`. La lista se crea en la declaración del atributo, así que nunca es `null`; si se dejara sin inicializar, el primer `hijos.isEmpty()` lanzaría `NullPointerException`.

---

## 2.2. Árboles de derivación

### 2.2.1. Concepto y construcción de un árbol de derivación

Una **gramática** es un conjunto de reglas que describe cómo se forman las cadenas de un lenguaje. Se define con detalle en la Unidad 4; por ahora basta conocer cuatro piezas:

| Pieza | Qué es | Ejemplo |
|---|---|---|
| **Símbolo terminal** | Símbolo que aparece en la cadena final; ya no se reemplaza | `a`, `b`, `+` |
| **Símbolo no terminal** | Variable que se reemplaza usando una regla | `S`, `E` |
| **Producción** | Regla `A → α`: el no terminal `A` se puede reemplazar por la secuencia `α` | `S → aSb` |
| **Símbolo inicial** | No terminal con el que empieza toda derivación | `S` |

Una **derivación** es la secuencia de reemplazos que va de la cadena `S` hasta una cadena formada solo por terminales. El **árbol de derivación** (o *árbol sintáctico*) es el dibujo de esa derivación.

**Por qué existe el árbol si ya está la derivación.** La derivación es una lista plana de pasos, y no deja ver qué símbolo salió de cuál. El árbol guarda justo esa información: cada símbolo cuelga del no terminal que lo produjo. Gracias a eso se puede responder "¿qué parte de la cadena es la suma y qué parte es la multiplicación?", que es lo que necesita un compilador para entender un programa.

| Característica | Descripción |
|---|---|
| Raíz | Siempre el símbolo inicial |
| Nodos internos | Siempre no terminales |
| Hojas | Terminales o ε (si la derivación está completa) |
| Orden de los hijos | El mismo orden que en el lado derecho de la producción |
| Frontera | Las hojas de izquierda a derecha: la cadena derivada |

#### Gramática de ejemplo

```
S → a S b | ε
```

(`ε` es la cadena vacía.) Se lee: "`S` puede reemplazarse por `aSb`, o por nada". Esta gramática genera las cadenas con tantas `a` como `b`, en ese orden: `ε`, `ab`, `aabb`, `aaabbb`, …

La regla `S → aSb` es **recursiva**: `S` aparece en su propio lado derecho. Cada vez que se aplica, agrega una `a` a la izquierda y una `b` a la derecha, y deja un `S` en medio para seguir. La regla `S → ε` es el **caso base**: detiene la recursión. Sin ella, la derivación nunca terminaría y la gramática no generaría ninguna cadena.

#### Cómo se construye el árbol

1. La **raíz** es el símbolo inicial.
2. Se elige un nodo hoja que sea no terminal y una producción `A → X₁ X₂ … Xₖ`. Al nodo se le agregan como **hijos**, de izquierda a derecha, los símbolos `X₁ … Xₖ`. (Si la producción es `A → ε`, se agrega un solo hijo `ε`.)
3. Se repite el paso 2 hasta que **todas las hojas sean terminales** (o `ε`).
4. Las hojas leídas de izquierda a derecha, ignorando `ε`, forman la **cadena derivada** (también llamada *frontera* del árbol).

Derivación de `aabb`:

```
S ⇒ aSb ⇒ aaSbb ⇒ aabb
     │       │        └─ S → ε
     │       └────────── S → aSb
     └────────────────── S → aSb
```

Y el árbol que le corresponde:

```
            S
         /  |  \
        a   S   b
          / | \
         a  S  b
            |
            ε
```

Hojas de izquierda a derecha: `a`, `a`, `ε`, `b`, `b` → la cadena `aabb`.

El árbol se construye "creciendo hacia abajo" a la par que la derivación avanza:

| Paso de la derivación | Producción | Lo que se agrega al árbol | Hojas en ese momento |
|---|---|---|---|
| `S` | — | La raíz `S` | `S` |
| `⇒ aSb` | `S → aSb` | Tres hijos bajo la raíz: `a`, `S`, `b` | `a S b` |
| `⇒ aaSbb` | `S → aSb` | Tres hijos bajo el `S` del nivel 1 | `a a S b b` |
| `⇒ aabb` | `S → ε` | Un hijo `ε` bajo el `S` del nivel 2 | `a a ε b b` |

En cada paso, las hojas leídas de izquierda a derecha son **exactamente** la forma intermedia de la derivación. El árbol nunca pierde información: la derivación se puede reconstruir a partir de él.

#### Correspondencia entre el árbol y la gramática

| En el árbol | En la gramática |
|---|---|
| Raíz | Símbolo inicial |
| Nodo interno | Un no terminal |
| Hoja | Un terminal (o `ε`) |
| Los hijos de un nodo, de izquierda a derecha | Lado derecho de la producción que se aplicó |
| Hojas de izquierda a derecha | Cadena derivada |

#### Construir el árbol en Java

La gramática se guarda como un `HashMap`: a cada no terminal le corresponde la lista de sus producciones, y cada producción es un arreglo de símbolos (un arreglo vacío es `ε`). Para no escribir los arreglos a mano, la función `lados` convierte un texto como `"a S b"` en `{"a", "S", "b"}`. A la clase `NodoArbol` de 2.1.2 se le agrega un método para leer la frontera:

```java
// Se agrega a la clase NodoArbol: hojas leídas de izquierda a derecha, ignorando ε
public String frontera() {
    if (esHoja()) return valor.equals("ε") ? "" : valor;

    String resultado = "";
    for (NodoArbol h : hijos)
        resultado += h.frontera();
    return resultado;
}
```

```java
// Convierte "a S b" en {"a", "S", "b"}; la cadena vacía representa ε
static ArrayList<String[]> lados(String... textos) {
    ArrayList<String[]> resultado = new ArrayList<String[]>();
    for (String t : textos)
        resultado.add(t.isEmpty() ? new String[0] : t.split(" "));
    return resultado;
}
```

La función `derivar` construye el árbol aplicando las producciones elegidas (por índice) en el orden en que se necesitan, de arriba hacia abajo y de izquierda a derecha. Las elecciones se guardan en una `LinkedList<Integer>` usada como **cola**: cada vez que hace falta expandir un no terminal, se saca la siguiente elección del frente con `removeFirst()`.

```java
static NodoArbol derivar(HashMap<String, ArrayList<String[]>> reglas, String simbolo,
                         LinkedList<Integer> elecciones) {
    NodoArbol nodo = new NodoArbol(simbolo);
    if (!reglas.containsKey(simbolo)) return nodo;          // terminal: es una hoja

    String[] produccion = reglas.get(simbolo).get(elecciones.removeFirst());
    if (produccion.length == 0) nodo.agregar("ε");          // producción vacía

    for (String s : produccion)
        nodo.agregarHijo(derivar(reglas, s, elecciones));

    return nodo;
}
```

```java
// S → a S b | ε
HashMap<String, ArrayList<String[]>> gramatica = new HashMap<String, ArrayList<String[]>>();
gramatica.put("S", lados("a S b", ""));        // [0] S → aSb    [1] S → ε

// Para aabb: S→aSb, S→aSb, S→ε
LinkedList<Integer> elecciones = new LinkedList<Integer>(Arrays.asList(0, 0, 1));
NodoArbol arbolDerivacion = derivar(gramatica, "S", elecciones);

arbolDerivacion.imprimir(0);
// → S  (nivel 0)
// →    a  (nivel 1)
// →    S  (nivel 1)
// →       a  (nivel 2)
// →       S  (nivel 2)
// →          ε  (nivel 3)
// →       b  (nivel 2)
// →    b  (nivel 1)

System.out.println(arbolDerivacion.frontera());   // → aabb
```

**Cómo decide qué es terminal.** Un símbolo es no terminal si tiene reglas en el mapa (`reglas.containsKey(simbolo)`); si no las tiene, es terminal y se convierte en hoja. Así la función no necesita saber de antemano cuáles son los terminales.

Traza de las llamadas (cada llamada que expande un no terminal consume **una** elección de la cola):

| Llamada | ¿Tiene reglas? | Elección que saca | Producción | Cola después |
|---|---|---|---|---|
| `derivar("S")` (raíz) | Sí | 0 | `a S b` | [0, 1] |
| `derivar("a")` | No: hoja | — | — | [0, 1] |
| `derivar("S")` (nivel 1) | Sí | 0 | `a S b` | [1] |
| `derivar("a")` | No: hoja | — | — | [1] |
| `derivar("S")` (nivel 2) | Sí | 1 | ε: se cuelga una hoja `ε` | [ ] |
| `derivar("b")` | No: hoja | — | — | [ ] |
| `derivar("b")` | No: hoja | — | — | [ ] |

El orden en que se consumen las elecciones es el **preorden** del árbol: un nodo se expande antes que sus hijos, y los hijos de izquierda a derecha. Por eso la lista de elecciones se escribe en ese orden.

> **Ojo con el número de elecciones.** Si la cola trae menos elecciones de las que el árbol necesita, `removeFirst()` sobre una lista vacía lanza `NoSuchElementException`; si trae de más, las sobrantes se ignoran sin aviso. Y si un índice no existe para ese no terminal (por ejemplo 2 cuando `S` solo tiene las reglas 0 y 1), `get` lanza `IndexOutOfBoundsException`.

> **Ojo con las hojas no terminales.** Todo nodo que **tiene hijos** es un no terminal; todo nodo **hoja** es un terminal (o `ε`). Si en un árbol de derivación ves una hoja que es un no terminal, la derivación **está incompleta**.

### 2.2.2. Relación entre jerarquía del árbol y reglas de producción

#### Un nodo interno = una producción

Cada nodo interno del árbol representa **una producción aplicada**: su etiqueta es el lado izquierdo y sus hijos, de izquierda a derecha, son el lado derecho. Por eso:

> **Número de nodos internos = número de producciones aplicadas.**

En el árbol de `aabb` hay 3 nodos internos (los tres `S`), y la derivación usó 3 producciones.

Leer un árbol equivale a leer la lista de producciones que se usaron: es la forma de **comprobar** que una cadena pertenece al lenguaje.

Un matiz importante: el árbol dice **qué** producciones se aplicaron y **dónde**, pero no **en qué orden** temporal. Si una forma intermedia tiene dos no terminales, se puede reemplazar primero cualquiera de los dos y el árbol final es el mismo. Por eso un mismo árbol puede corresponder a varias derivaciones distintas; la Unidad 4 (4.1.2) le pone nombre a dos órdenes estándar.

#### La jerarquía del árbol decide el orden de las operaciones

Cuando una gramática se escribe **por niveles**, su estructura obliga a que ciertas operaciones queden **más abajo** en el árbol. Lo que está más abajo se resuelve **primero**. Ejemplo con expresiones aritméticas:

```
E → E + T | T            suma:            nivel más alto
T → T * F | F            multiplicación:  un nivel más abajo
F → ( E ) | 2 | 3 | 4    números y paréntesis: el nivel más bajo
```

**Por qué funciona.** Desde `E` solo se puede introducir un `+`. Para que aparezca un `*`, hay que bajar primero a `T`. Y un `T` nunca puede producir un `+` directamente: para eso tendría que bajar hasta `F` y usar paréntesis. Eso obliga a que todo `*` quede **dentro** de un operando de la suma, nunca al revés. La gramática no "sabe" qué es la precedencia: la forma de las reglas la impone.

| Nivel | No terminal | Operador que introduce | Se evalúa |
|---|---|---|---|
| Alto | `E` (expresión) | `+` | Al final |
| Medio | `T` (término) | `*` | Antes que la suma |
| Bajo | `F` (factor) | Números o `( E )` | Primero; los paréntesis "reinician" desde `E` |

El árbol de `2 + 3 * 4`:

```
                E
             /  |  \
            E   +   T
            |     / | \
            T    T  *  F
            |    |     |
            F    F     4
            |    |
            2    3
```

El `*` queda **más profundo** que el `+`: primero se multiplica `3 * 4` y después se suma `2`. La precedencia de operadores no se escribe como una regla aparte, sino que **está codificada en la forma de la gramática**.

Producciones usadas (leídas en preorden, de arriba hacia abajo y de izquierda a derecha):

| # | Producción | Nodo del árbol |
|---|---|---|
| 1 | `E → E + T` | raíz |
| 2 | `E → T` | hijo izquierdo de la raíz |
| 3 | `T → F` | |
| 4 | `F → 2` | |
| 5 | `T → T * F` | hijo derecho de la raíz |
| 6 | `T → F` | |
| 7 | `F → 3` | |
| 8 | `F → 4` | |

Comprobación con la regla "nodos internos = producciones": el árbol tiene **8** nodos internos (dos `E`, tres `T`, tres `F`) y se aplicaron **8** producciones.

La regla `E → E + T` también decide la **asociatividad**. Como el `E` recursivo está a la **izquierda**, en `2 + 3 + 4` el primer `+` queda más abajo: se agrupa `(2 + 3) + 4`. Con la suma no cambia el resultado, pero con una resta sí: una regla `E → E − T` agrupa `8 − 3 − 2` como `(8 − 3) − 2` = **3**, que es lo correcto; si la recursión estuviera a la derecha (`E → T − E`), agruparía `8 − (3 − 2)` = **7**.

#### Evaluar recorriendo el árbol (en Java)

Como cada nodo conoce la regla que lo creó, calcular el valor de la expresión es recorrer el árbol de abajo hacia arriba (en **postorden**: un nodo se calcula después de sus hijos). Se reutilizan `lados` y `derivar`; cada producción se elige por su índice dentro de la lista:

```java
// E → E + T | T         T → T * F | F         F → ( E ) | 2 | 3 | 4
HashMap<String, ArrayList<String[]>> expresiones = new HashMap<String, ArrayList<String[]>>();
expresiones.put("E", lados("E + T", "T"));
expresiones.put("T", lados("T * F", "F"));
expresiones.put("F", lados("( E )", "2", "3", "4"));

// Producciones de la tabla, por índice: E→E+T(0) E→T(1) T→F(1) F→2(1) T→T*F(0) T→F(1) F→3(2) F→4(3)
LinkedList<Integer> elecciones = new LinkedList<Integer>(Arrays.asList(0, 1, 1, 1, 0, 1, 2, 3));
NodoArbol arbolExp = derivar(expresiones, "E", elecciones);

System.out.println(arbolExp.frontera());   // → 2+3*4
```

```java
static int evaluar(NodoArbol n) {
    ArrayList<NodoArbol> h = n.getHijos();
    boolean tresHijos = h.size() == 3;

    switch (n.getValor()) {
        case "E": return tresHijos ? evaluar(h.get(0)) + evaluar(h.get(2)) : evaluar(h.get(0));
        case "T": return tresHijos ? evaluar(h.get(0)) * evaluar(h.get(2)) : evaluar(h.get(0));
        case "F": return tresHijos ? evaluar(h.get(1)) : Integer.parseInt(h.get(0).getValor());
        default:  throw new IllegalStateException("Símbolo inesperado: " + n.getValor());
    }
}
```

```java
System.out.println(evaluar(arbolExp));     // → 14   (2 + (3 * 4), no (2 + 3) * 4 = 20)
```

**Cómo decide qué regla se aplicó.** El nodo no guarda el índice de la producción, pero no hace falta: basta con contar sus hijos. Un `E` con tres hijos vino de `E → E + T`; con uno, de `E → T`. Un `F` con tres hijos vino de `F → ( E )`, y su valor es el del hijo del centro (el de en medio de los paréntesis); con uno, es un número.

Traza de `evaluar` (los valores suben desde las hojas):

| Nodo | Regla (por número de hijos) | Cálculo | Valor |
|---|---|---|---|
| `F` sobre `2` | `F → 2` | `parseInt("2")` | 2 |
| `T` izquierdo | `T → F` | valor del `F` | 2 |
| `E` izquierdo | `E → T` | valor del `T` | 2 |
| `F` sobre `3` | `F → 3` | `parseInt("3")` | 3 |
| `T` interno | `T → F` | valor del `F` | 3 |
| `F` sobre `4` | `F → 4` | `parseInt("4")` | 4 |
| `T` derecho | `T → T * F` | 3 * 4 | 12 |
| `E` raíz | `E → E + T` | 2 + 12 | **14** |

Con paréntesis, la misma gramática produce otro árbol. Para `(2 + 3) * 4` la raíz es `E → T` y la multiplicación queda **arriba**, porque el paréntesis obliga a volver a empezar desde `E` dentro del factor:

```java
// E→T(1) T→T*F(0) T→F(1) F→(E)(0) E→E+T(0) E→T(1) T→F(1) F→2(1) T→F(1) F→3(2) F→4(3)
LinkedList<Integer> otras = new LinkedList<Integer>(Arrays.asList(1, 0, 1, 0, 0, 1, 1, 1, 1, 2, 3));
NodoArbol arbolParentesis = derivar(expresiones, "E", otras);

System.out.println(arbolParentesis.frontera());   // → (2+3)*4
System.out.println(evaluar(arbolParentesis));     // → 20
```

> Si el árbol hubiera agrupado `(2 + 3)` más abajo, el resultado sería 20. **La forma del árbol cambia el significado de la cadena.**

#### Cuando la gramática no tiene niveles

Con la gramática `E → E + E | E * E | 2 | 3 | 4`, la cadena `2 + 3 * 4` se puede derivar con **dos árboles distintos**: uno que agrupa `2 + 3` primero (resultado 20) y otro que agrupa `3 * 4` primero (resultado 14). A una gramática donde una cadena tiene más de un árbol de derivación se le llama **ambigua**; los lenguajes de programación evitan esto escribiendo la gramática por niveles, como la de arriba.

```
   Árbol 1: (2 + 3) * 4 = 20            Árbol 2: 2 + (3 * 4) = 14

            E                                   E
          / | \                               / | \
         E  *  E                             E  +  E
       / | \   |                             |   / | \
      E  +  E  4                             2  E  *  E
      |     |                                   |     |
      2     3                                   3     4
```

Las dos tienen la misma frontera, `2+3*4`, pero significan cosas distintas. Un compilador que usara esta gramática no podría decidir cuál calcular.

| | Gramática por niveles (`E`, `T`, `F`) | Gramática ambigua (`E → E+E \| E*E \| …`) |
|---|---|---|
| Árboles para `2+3*4` | Exactamente uno | Dos |
| Precedencia | Impuesta por la forma de las reglas | No existe: depende del árbol elegido |
| Número de no terminales | Tres | Uno |
| Uso real | Compiladores e intérpretes | Solo para describir el lenguaje, no para analizarlo |

> **Ojo con confundir lenguaje y gramática.** Las dos gramáticas generan **exactamente las mismas cadenas**. La ambigüedad no es una propiedad del lenguaje, sino de la gramática: el mismo lenguaje se puede describir con una gramática ambigua y con una que no lo es.
