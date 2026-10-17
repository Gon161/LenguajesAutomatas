# Unidad 1. Fundamentos de grafos

Un **grafo** es una forma de dibujar y estudiar **cosas** y las **conexiones** entre ellas: ciudades y carreteras, personas y amistades, páginas web y enlaces. Es la herramienta más simple para describir relaciones, y por eso es la base de toda la materia.

La razón de empezar aquí es esta: **un autómata es un grafo**. Los estados son los nodos y las transiciones son las aristas. Si entiendes bien el vocabulario de esta unidad (nodo, arista, camino, ciclo, alcanzabilidad), los diagramas de autómatas de la Unidad 3 serán un caso particular de lo que ya sabes.

La idea de fondo es la **abstracción**: un grafo se queda solo con *quién está conectado con quién* y olvida todo lo demás (distancias, nombres, colores). Gracias a eso, un mismo algoritmo, por ejemplo "¿puedo llegar de X a Y?", sirve igual para un mapa, una red social o un autómata. Si no existiera esta abstracción, habría que inventar un programa distinto para cada problema.

> En esta unidad el código es **Java**. Las clases usan `java.util` (`ArrayList`, `HashSet`, `HashMap`, `LinkedList`), así que cada archivo empieza con los `import` correspondientes. Las clases siguen el estilo de `ejemplos/Nodo.java`: atributos privados inicializados en la declaración y métodos públicos para usarlos. El grafo se guarda como una **estructura enlazada**: cada objeto nodo guarda **referencias** a los nodos a los que apunta.

## 1.1. Conceptos básicos de teoría de grafos

### 1.1.1. Definición de grafo dirigido y no dirigido

Formalmente, un grafo es un par **G = (V, E)**:

- **V** es un conjunto finito de **vértices** (también llamados **nodos**).
- **E** es un conjunto de **aristas**: conexiones entre dos nodos.

Ejemplo: **G = ({A, B, C, D}, {(A,B), (B,C), (C,A), (C,D)})**

Es un grafo de 4 nodos y 4 aristas.

| Característica | Descripción |
|---|---|
| Nodos (V) | Conjunto **finito** de elementos; en esta materia serán estados |
| Aristas (E) | Pares de nodos; dicen **quién está conectado con quién** |
| Orden | Número de nodos, \|V\| |
| Tamaño | Número de aristas, \|E\| |
| Grafo **simple** | Sin lazos y sin dos aristas repetidas entre los mismos nodos |
| Lo que **no** guarda | Posición en el dibujo, distancias, colores: dos dibujos distintos pueden ser el mismo grafo |

> **Ojo con el dibujo.** Un grafo no es su dibujo. Si se mueven los nodos de lugar o se curvan las líneas, mientras se conserven las mismas conexiones sigue siendo **el mismo grafo**. Lo único que importa es el conjunto de aristas.

#### Grafo no dirigido

Las aristas **no tienen sentido**: si A está conectado con B, entonces B está conectado con A. La arista se escribe como un conjunto `{A, B}` y se dibuja como una línea simple.

```
Ana ─────── Beto
 │
Carla ───── Diego
```

Ejemplo real: una red de amistades. Si Ana es amiga de Beto, Beto es amigo de Ana.

Como `{A, B}` es un **conjunto**, el orden de los elementos no existe: `{A, B}` y `{B, A}` son exactamente la misma arista. Por eso un grafo no dirigido simple de `n` nodos tiene, a lo más, **n(n − 1) / 2** aristas (cada par de nodos distintos se puede unir una sola vez). Con 4 nodos: 4 · 3 / 2 = **6** aristas como máximo.

#### Grafo dirigido (dígrafo)

Cada arista **tiene un sentido**: va de un nodo **origen** a un nodo **destino**. Se escribe como un par ordenado `(A, B)` y se dibuja como una flecha.

```
 A ───▶ B
 ▲      │
 │      ▼
 └───── C ───▶ D
```

Ejemplo real: calles de un solo sentido. Que puedas ir de A a B no significa que puedas regresar de B a A.

Aquí `(A, B)` y `(B, A)` son **dos aristas distintas**, y pueden existir las dos a la vez (una calle de doble sentido se modela con dos flechas). Por eso un dígrafo sin lazos de `n` nodos tiene, a lo más, **n(n − 1)** aristas: con 4 nodos, 4 · 3 = **12**. Si además se permiten lazos `(A, A)`, el máximo sube a **n²** = 16.

#### Comparación

| Característica | No dirigido | Dirigido |
|---|---|---|
| Se dibuja con | Líneas | Flechas |
| Arista | Conjunto `{A, B}` | Par ordenado `(A, B)` |
| `{A,B}` = `{B,A}` | Sí | No: `(A,B)` ≠ `(B,A)` |
| Máximo de aristas (simple, `n` nodos) | n(n − 1) / 2 | n(n − 1) |
| Grado de un nodo | Un solo número: **grado** | Dos números: grado de **entrada** y de **salida** |
| Ejemplo | Amistades | Calles de un solo sentido, **autómatas** |

> En esta materia casi siempre trabajaremos con grafos **dirigidos**: una transición de un autómata va de un estado a otro, nunca al revés.

Un detalle útil: **todo grafo no dirigido se puede ver como uno dirigido** en el que cada línea `{A, B}` se reemplaza por dos flechas, `(A, B)` y `(B, A)`. Es exactamente lo que hace el código de esta unidad: guarda siempre flechas y, si el grafo es no dirigido, guarda las dos.

```
 No dirigido            Equivale a (dirigido)

  A ───── B              A ───▶ B
                         A ◀─── B
```

#### Cómo se guarda un grafo en memoria

La forma más común es la **lista de adyacencia**: para cada nodo se guarda la lista de nodos a los que llega.

En Java la forma más natural de hacerlo es con **objetos enlazados**: cada nodo es un objeto, y su lista de vecinos guarda **referencias** a otros objetos nodo. Es la clase de `ejemplos/Nodo.java`:

```java
import java.util.ArrayList;

public class Nodo {

    private String id = "";
    private ArrayList<Nodo> vecinos = new ArrayList<Nodo>();   // nodos a los que apunta (aristas de salida)

    public Nodo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Nodo> getVecinos() {
        return vecinos;
    }

    public void apuntarA(Nodo destino) {
        vecinos.add(destino);
    }
}
```

```java
// Grafo dirigido: A→B, B→C, C→A, C→D
Nodo a = new Nodo("A");
Nodo b = new Nodo("B");
Nodo c = new Nodo("C");
Nodo d = new Nodo("D");

a.apuntarA(b);   // A → B
b.apuntarA(c);   // B → C
c.apuntarA(a);   // C → A
c.apuntarA(d);   // C → D

for (Nodo vecino : c.getVecinos())
    System.out.print(vecino.getId() + " ");    // → A D
System.out.println();
System.out.println(d.getVecinos().size());    // → 0   (D no apunta a nadie)
```

**Qué pasa por dentro.** `new Nodo("A")` crea un objeto en la memoria dinámica (el *heap*) y la variable `a` guarda solo su **referencia** (su dirección), no el objeto. Cuando se ejecuta `c.apuntarA(a)`, la lista de `C` guarda **una copia de esa referencia**, no una copia del nodo A. Por eso los cuatro nodos quedan enlazados entre sí, igual que en el dibujo:

```
 a ──▶ [Nodo "A" | vecinos: ●] ──▶ [Nodo "B" | vecinos: ●] ──▶ [Nodo "C" | vecinos: ●, ●]
             ▲                                                                 │  │
             └─────────────────────────────────────────────────────────────────┘  │
                                                   [Nodo "D" | vecinos: vacía] ◀───┘
```

Las flechas del dibujo del grafo **son literalmente referencias** en memoria. Esto tiene consecuencias importantes:

| Hecho | Consecuencia |
|---|---|
| La lista guarda referencias, no copias | Si se modifica el nodo A (por ejemplo, se le agrega un vecino), **todos** los que apuntan a A ven el cambio |
| Puede haber referencias circulares (A → B → C → A) | Java no tiene problema en guardarlas, pero un recorrido sin control **nunca termina** (se ve en 1.1.2) |
| Un nodo sin vecinos tiene una lista **vacía**, no `null` | `d.getVecinos().size()` da 0 y se puede recorrer sin error; por eso la lista se inicializa en la declaración |

La otra forma es la **matriz de adyacencia**: una tabla donde la fila es el origen, la columna es el destino y un `1` indica que existe la arista.

| origen \ destino | A | B | C | D |
|---|---|---|---|---|
| **A** | 0 | 1 | 0 | 0 |
| **B** | 0 | 0 | 1 | 0 |
| **C** | 1 | 0 | 0 | 1 |
| **D** | 0 | 0 | 0 | 0 |

En Java se guarda como un arreglo bidimensional; cada nodo se identifica por su **posición** (A = 0, B = 1, C = 2, D = 3):

```java
String[] nombres = { "A", "B", "C", "D" };
int[][] ady = {
    // A  B  C  D
    {  0, 1, 0, 0 },   // A
    {  0, 0, 1, 0 },   // B
    {  1, 0, 0, 1 },   // C
    {  0, 0, 0, 0 },   // D
};

System.out.println(ady[2][3]);   // → 1   (existe C → D)
System.out.println(ady[3][2]);   // → 0   (no existe D → C)

for (int j = 0; j < nombres.length; j++)
    if (ady[2][j] == 1) System.out.print(nombres[j] + " ");   // → A D
System.out.println();
```

| | Lista de adyacencia | Matriz de adyacencia |
|---|---|---|
| Espacio | Solo guarda las aristas que existen | Siempre n × n |
| Espacio, en notación O | O(n + m) (n nodos, m aristas) | O(n²) |
| Ver si existe `(A,B)` | Recorre la lista de A: O(grado de A) | Acceso directo `ady[i][j]`: O(1) |
| Recorrer los vecinos de un nodo | Solo los que existen: O(grado) | Toda la fila: O(n) |
| Agregar un nodo nuevo | Crear un objeto y listo | Hay que crear una matriz más grande |
| Conviene cuando | Hay pocas aristas (lo habitual) | Hay muchas aristas |

Un ejemplo numérico muestra por qué la lista suele ganar: un grafo de **1 000** nodos donde cada nodo tiene **3** vecinos tiene 3 000 aristas. La lista guarda 3 000 referencias; la matriz guarda 1 000 × 1 000 = **1 000 000** casillas, de las cuales 997 000 son ceros. Los autómatas se parecen a este caso: cada estado tiene una flecha por símbolo, no una flecha hacia cada estado.

En un grafo **no dirigido** la matriz es simétrica (la mitad de arriba es espejo de la de abajo): `ady[i][j] == ady[j][i]` para todo par.

Para el resto de la unidad usaremos esta clase, que guarda el grafo como una lista de objetos `Nodo` enlazados:

```java
import java.util.ArrayList;

public class Grafo {

    private boolean dirigido = true;
    private ArrayList<Nodo> nodos = new ArrayList<Nodo>();

    public Grafo(boolean dirigido) {
        this.dirigido = dirigido;
    }

    public ArrayList<Nodo> getNodos() {
        return nodos;
    }

    public Nodo buscar(String id) {
        for (Nodo n : nodos)
            if (n.getId().equals(id)) return n;
        return null;                                   // no existe un nodo con ese id
    }

    // Devuelve el nodo con ese id; si no existe, lo crea
    public Nodo agregarNodo(String id) {
        Nodo existente = buscar(id);
        if (existente != null) return existente;

        Nodo nuevo = new Nodo(id);
        nodos.add(nuevo);
        return nuevo;
    }

    public void agregarArista(String origen, String destino) {
        Nodo a = agregarNodo(origen);
        Nodo b = agregarNodo(destino);
        a.apuntarA(b);

        // En un grafo no dirigido la arista se guarda en ambos sentidos
        if (!dirigido && a != b)
            b.apuntarA(a);
    }
}
```

Paso a paso, `agregarArista("A", "B")` en un grafo vacío hace esto:

| Paso | Instrucción | Estado de la memoria |
|---|---|---|
| 1 | `agregarNodo("A")` | No existe: se crea `Nodo "A"` y se agrega a `nodos` |
| 2 | `agregarNodo("B")` | No existe: se crea `Nodo "B"` y se agrega a `nodos` |
| 3 | `a.apuntarA(b)` | La lista de A guarda una referencia a B: `A → B` |
| 4 | `!dirigido && a != b` | Solo si es no dirigido: la lista de B guarda una referencia a A |

> **Ojo con `==` en cadenas.** Dentro de `buscar` se compara con `n.getId().equals(id)` y **no** con `n.getId() == id`. En Java, `==` entre objetos compara **referencias** (si son el mismo objeto en memoria), no el contenido. `new String("A") == "A"` da `false` aunque ambas digan "A"; `new String("A").equals("A")` da `true`. En cambio, en `a != b` sí se quiere comparar referencias: interesa saber si es **el mismo nodo**, para no guardar dos veces un lazo.

> **Ojo con el costo de `buscar`.** Buscar un nodo recorre toda la lista: es O(n). Para grafos chicos (como los de esta materia) no importa; en un grafo enorme convendría un `HashMap<String, Nodo>` que encuentra el nodo por su nombre en tiempo casi constante.

> **Grafo no es gráfica.** Una *gráfica* es un diagrama de barras o de pastel. Un *grafo* es un conjunto de nodos y aristas. En inglés ambos son distintos: *chart* contra *graph*.

### 1.1.2. Nodo, arista, camino, ciclo y alcanzabilidad

#### Vocabulario

| Término | Significado | En el ejemplo `A→B, B→C, C→A, C→D` |
|---|---|---|
| **Nodo** (vértice) | Cada elemento del grafo | A, B, C, D |
| **Arista** | Conexión entre dos nodos | (A,B), (B,C), (C,A), (C,D) |
| **Adyacentes** | Nodos unidos por una arista | B es adyacente a A |
| **Grado de salida** | Aristas que salen de un nodo | C tiene grado de salida 2 |
| **Grado de entrada** | Aristas que llegan a un nodo | D tiene grado de entrada 1 |
| **Lazo** (*loop*) | Arista de un nodo hacia sí mismo | `(A,A)` (no existe en este ejemplo) |
| **Camino** | Secuencia de nodos donde cada par consecutivo está unido por una arista | A → B → C → D |
| **Longitud** de un camino | Número de aristas que recorre | A → B → C → D mide 3 |
| **Camino simple** | Camino que no repite nodos | A → B → C → D sí; A → B → C → A → B no |
| **Ciclo** | Camino que empieza y termina en el mismo nodo | A → B → C → A |
| **Grafo acíclico** | Grafo sin ningún ciclo | No es el caso: tiene A → B → C → A |
| **Alcanzable** | El nodo destino se puede llegar siguiendo aristas desde el origen | D es alcanzable desde A |

En un grafo **no dirigido** se habla simplemente de **grado** (el número de aristas que tiene el nodo).

Un camino de longitud 0 (un solo nodo) también cuenta: todo nodo es alcanzable desde sí mismo.

Hay una relación que siempre se cumple y sirve para comprobar cuentas, llamada **lema del apretón de manos**:

| Tipo de grafo | Relación | En el ejemplo |
|---|---|---|
| Dirigido | Suma de grados de salida = suma de grados de entrada = número de aristas | Salidas: 1 + 1 + 2 + 0 = 4. Entradas: 1 + 1 + 1 + 1 = 4. Aristas: 4 |
| No dirigido | Suma de todos los grados = **2** × número de aristas | En `Ana─Beto, Ana─Carla, Carla─Diego`: 2 + 1 + 2 + 1 = 6 = 2 × 3 |

El porqué es simple: cada flecha sale de un nodo y llega a otro, así que se cuenta **una vez** como salida y **una vez** como entrada. En un no dirigido, cada línea toca dos nodos y suma 1 al grado de cada uno. Esta propiedad se usa en la Unidad 2 para contar aristas.

#### La dirección importa

En el ejemplo anterior:

- D **es alcanzable** desde A (A → B → C → D).
- A **no es alcanzable** desde D: D no tiene aristas de salida.

Alcanzar a B desde A no garantiza poder volver. Por eso en un grafo dirigido hay que preguntar siempre "**desde** qué nodo **hasta** cuál".

Formalmente, la alcanzabilidad es una relación **reflexiva** (todo nodo se alcanza a sí mismo) y **transitiva** (si llego de X a Y y de Y a Z, llego de X a Z), pero en un grafo dirigido **no es simétrica**. En un grafo no dirigido sí lo es: si llego de X a Y, recorriendo el mismo camino al revés llego de Y a X.

> **Ojo con confundir "adyacente" y "alcanzable".** Adyacente significa que hay **una** arista directa; alcanzable significa que hay **un camino** de cualquier longitud. D es alcanzable desde A, pero no es adyacente a A.

#### Alcanzabilidad en Java

La pregunta "¿puedo llegar de X a Y?" se responde recorriendo el grafo con una **búsqueda en anchura** (*BFS*): se parte de X, se visitan sus vecinos, luego los vecinos de los vecinos, y así hasta agotar los nodos o encontrar Y. Un conjunto `HashSet` evita visitar dos veces el mismo nodo (y que el programa se cicle).

**Por qué una cola.** La BFS necesita atender los nodos **en el orden en que se descubrieron**: primero todos los que están a distancia 1, luego los de distancia 2, etcétera. Esa disciplina es la de una **cola** (*FIFO*: el primero que entra es el primero que sale). En Java se usa `LinkedList`, que es una **lista doblemente enlazada**: agregar al final (`addLast`) y sacar del principio (`removeFirst`) solo mueven un par de referencias, sin recorrer la lista, así que ambas operaciones cuestan O(1).

```
 cola (LinkedList):   frente ──▶ [ B ] ⇄ [ C ] ⇄ [ D ] ◀── final
                        removeFirst() saca B         addLast(x) agrega aquí
```

**Por qué el conjunto de visitados.** En el grafo de ejemplo existe el ciclo A → B → C → A. Sin recordar qué nodos ya se vieron, el recorrido metería A otra vez en la cola, luego B, luego C, luego A… y nunca terminaría.

Se agregan estos métodos a la clase `Grafo` (con `import java.util.HashSet;` y `import java.util.LinkedList;`):

```java
public boolean alcanzable(String desde, String hasta) {
    Nodo inicio = buscar(desde);
    Nodo meta = buscar(hasta);
    if (inicio == null || meta == null) return false;

    HashSet<Nodo> visitados = new HashSet<Nodo>();
    LinkedList<Nodo> cola = new LinkedList<Nodo>();   // lista enlazada usada como cola
    visitados.add(inicio);
    cola.addLast(inicio);

    while (!cola.isEmpty()) {
        Nodo actual = cola.removeFirst();             // sale el más antiguo
        if (actual == meta) return true;

        for (Nodo vecino : actual.getVecinos()) {
            if (visitados.add(vecino))                // add devuelve false si ya estaba
                cola.addLast(vecino);
        }
    }

    return false;
}

public int gradoSalida(String id) {
    return buscar(id).getVecinos().size();
}

public int gradoEntrada(String id) {
    Nodo objetivo = buscar(id);
    int total = 0;
    for (Nodo n : nodos)
        for (Nodo vecino : n.getVecinos())
            if (vecino == objetivo) total++;
    return total;
}
```

Traza de `alcanzable("A", "D")`:

| Paso | Sale de la cola | ¿Es D? | Vecinos nuevos que entran | Cola después | Visitados |
|---|---|---|---|---|---|
| 0 | — | — | A (el inicio) | [A] | {A} |
| 1 | A | No | B | [B] | {A, B} |
| 2 | B | No | C | [C] | {A, B, C} |
| 3 | C | No | D (A ya estaba: no entra) | [D] | {A, B, C, D} |
| 4 | D | **Sí** | — | — | → `true` |

En el paso 3 se ve el papel del conjunto: el vecino A de C ya estaba visitado, así que `visitados.add(A)` devuelve `false` y A no vuelve a entrar a la cola.

Uso:

```java
Grafo g = new Grafo(true);
g.agregarArista("A", "B");
g.agregarArista("B", "C");
g.agregarArista("C", "A");
g.agregarArista("C", "D");

System.out.println(g.alcanzable("A", "D"));   // → true
System.out.println(g.alcanzable("D", "A"));   // → false

for (Nodo n : g.getNodos())
    System.out.println(n.getId() + ": entrada=" + g.gradoEntrada(n.getId())
                       + ", salida=" + g.gradoSalida(n.getId()));

// → A: entrada=1, salida=1
// → B: entrada=1, salida=1
// → C: entrada=1, salida=2
// → D: entrada=1, salida=0
```

Con un grafo no dirigido, la misma clase da resultados simétricos, porque cada línea se guardó como dos flechas:

```java
Grafo amistades = new Grafo(false);
amistades.agregarArista("Ana", "Beto");
System.out.println(amistades.alcanzable("Beto", "Ana"));   // → true
System.out.println(amistades.gradoSalida("Ana"));         // → 1   (en un no dirigido, es el grado)
```

**Costo.** Cada nodo entra a la cola a lo más una vez y cada arista se revisa a lo más una vez, así que la BFS cuesta **O(n + m)**. `gradoEntrada`, en cambio, tiene que revisar **todas** las aristas del grafo para contar las que llegan a un nodo: con listas de adyacencia, saber "quién me apunta" es caro, porque cada nodo solo conoce a sus vecinos de salida.

> **Ojo con el `HashSet<Nodo>`.** La clase `Nodo` no redefine `equals` ni `hashCode`, así que el conjunto distingue los nodos por **identidad** (si son el mismo objeto). Es justo lo que se quiere: dos nodos distintos nunca se confunden aunque tuvieran el mismo `id`. Si algún día se redefine `equals` comparando solo el `id`, hay que redefinir `hashCode` también, o el `HashSet` dejará de funcionar bien.

| Recorrido | Estructura | Orden en que visita | Sirve para |
|---|---|---|---|
| **BFS** (anchura) | Cola (`LinkedList`) | Por distancia: primero los vecinos, luego los vecinos de los vecinos | Alcanzabilidad, camino con menos aristas |
| **DFS** (profundidad) | Pila o recursión | Avanza lo más lejos posible y luego regresa | Alcanzabilidad, detección de ciclos |

Para saber **si** se llega, cualquiera de los dos sirve. La diferencia aparece cuando importa **cómo** se llega: la BFS encuentra primero el camino con menos aristas.

> Esta misma idea aparece después en los autómatas: un estado de aceptación es **inútil** si no es alcanzable desde el estado inicial.

#### Detección de ciclos (grafo dirigido)

Para saber si un grafo dirigido tiene ciclos se recorre en profundidad (*DFS*) marcando cada nodo como **sin visitar**, **en curso** o **terminado**. Si durante el recorrido se encuentra una arista hacia un nodo que está **en curso**, se volvió a un nodo del camino actual: hay un ciclo.

**Por qué tres estados y no dos.** Con un simple "visitado / no visitado" se detectarían ciclos falsos. Considera este grafo en forma de rombo:

```
      A
     ↙ ↘
    B   C
     ↘ ↙
      D
```

El recorrido va A → B → D, termina D, regresa, y después va A → C → D. D ya está "visitado", pero **no hay ciclo**: no se puede volver a A. La diferencia es que D ya está **terminado** (salió del camino actual), no **en curso**. Solo una flecha hacia un nodo **en curso** cierra un ciclo, porque ese nodo sigue en el camino que se está recorriendo.

| Estado | Valor | Significado |
|---|---|---|
| Sin visitar | 0 | Todavía no se llega a él |
| En curso | 1 | Está en el camino actual (la recursión no ha regresado de él) |
| Terminado | 2 | Ya se exploró todo lo que sale de él; no forma parte del camino actual |

Se agregan estos métodos a la clase `Grafo` (con `import java.util.HashMap;`):

```java
public boolean tieneCiclo() {                         // solo para grafos dirigidos
    // 0 = sin visitar, 1 = en curso, 2 = terminado
    HashMap<Nodo, Integer> estado = new HashMap<Nodo, Integer>();
    for (Nodo n : nodos) estado.put(n, 0);

    for (Nodo n : nodos)
        if (estado.get(n) == 0 && visitar(n, estado)) return true;

    return false;
}

private boolean visitar(Nodo n, HashMap<Nodo, Integer> estado) {
    estado.put(n, 1);
    for (Nodo vecino : n.getVecinos()) {
        if (estado.get(vecino) == 1) return true;                       // volvimos a un nodo del camino
        if (estado.get(vecino) == 0 && visitar(vecino, estado)) return true;
    }
    estado.put(n, 2);
    return false;
}
```

Traza de `tieneCiclo()` en el grafo `A→B, B→C, C→A, C→D`:

| Llamada | Nodo | Acción | Estados (A, B, C, D) |
|---|---|---|---|
| `visitar(A)` | A | A pasa a en curso; su vecino B está sin visitar → se visita | 1, 0, 0, 0 |
| `visitar(B)` | B | B en curso; su vecino C sin visitar → se visita | 1, 1, 0, 0 |
| `visitar(C)` | C | C en curso; su primer vecino es **A, que está en curso** | 1, 1, 1, 0 |
| | | → hay ciclo: se regresa `true` hasta arriba | |

El camino actual en ese momento era A → B → C, y la flecha C → A regresa a su principio: es el ciclo A → B → C → A.

```java
Grafo g = new Grafo(true);
g.agregarArista("A", "B");
g.agregarArista("B", "C");
g.agregarArista("C", "A");
g.agregarArista("C", "D");
System.out.println(g.tieneCiclo());          // → true   (A → B → C → A)

Grafo sinCiclo = new Grafo(true);
sinCiclo.agregarArista("A", "B");
sinCiclo.agregarArista("B", "C");
System.out.println(sinCiclo.tieneCiclo());   // → false

Grafo rombo = new Grafo(true);
rombo.agregarArista("A", "B");
rombo.agregarArista("A", "C");
rombo.agregarArista("B", "D");
rombo.agregarArista("C", "D");
System.out.println(rombo.tieneCiclo());      // → false  (D se alcanza dos veces, pero no hay ciclo)

Grafo lazo = new Grafo(true);
lazo.agregarArista("A", "A");
System.out.println(lazo.tieneCiclo());       // → true   (un lazo es un ciclo de longitud 1)
```

Matices del método:

- El `for` de `tieneCiclo` arranca un recorrido desde **cada** nodo sin visitar, no solo desde el primero. Si el grafo tiene partes que no se alcanzan desde el primer nodo, un ciclo escondido ahí también se encuentra.
- La recursión de `visitar` usa la **pila de llamadas** de Java como pila del DFS. En grafos con miles de nodos encadenados esa pila se puede agotar (`StackOverflowError`); para los grafos de esta materia no es un problema.
- `estado.get(n) == 0` funciona porque Java convierte el `Integer` guardado en el mapa a `int` antes de comparar (*unboxing*).

> **Ojo con el grafo no dirigido.** En un grafo **no dirigido** esta función no sirve: la arista `{A,B}` se guarda en ambos sentidos y siempre parecería un ciclo A → B → A. Un ciclo en un no dirigido debe tener al menos tres nodos distintos; eso se retoma en la Unidad 2.

> **Ojo con imprimir estructuras con ciclos.** Si a `Nodo` se le escribiera un `toString()` que imprimiera también a sus vecinos (`return id + vecinos;`), imprimir A llamaría al `toString()` de B, que llamaría al de C, que llamaría otra vez al de A… hasta un `StackOverflowError`. Con estructuras enlazadas que pueden tener ciclos, todo recorrido, incluido el de imprimir, necesita recordar qué ya visitó.

---

## 1.2. Diagramas de transición como grafos dirigidos

### 1.2.1. Representación de estados y transiciones mediante grafos etiquetados

Un **grafo etiquetado** es un grafo dirigido donde cada arista lleva además una **etiqueta** (un símbolo, una acción o una condición). Así se puede describir no solo *a dónde* se puede ir, sino *qué hace falta* para ir.

Un **diagrama de transición** es justo eso, interpretado como un sistema que cambia de situación:

| Elemento del diagrama | Se dibuja como | Representa |
|---|---|---|
| **Estado** | Círculo con un nombre | Una situación en la que puede estar el sistema |
| **Transición** | Flecha entre dos estados | Un cambio de situación |
| **Etiqueta** de la flecha | Símbolo sobre la flecha | Qué evento o símbolo provoca el cambio |
| **Estado inicial** | Flecha de entrada sin origen (`──▶`) | Donde empieza el sistema |
| **Estado final / de aceptación** | Doble círculo | Una situación considerada "correcta" o "terminada" |

| Característica | Descripción |
|---|---|
| Un estado **resume el pasado** | No guarda toda la historia de eventos, solo lo necesario para saber qué hacer después |
| Hay un estado **actual** | En cada momento el sistema está en exactamente un estado |
| Los cambios son **discretos** | Se pasa de un estado a otro de golpe, con cada evento; no hay estados "a medias" |
| La etiqueta **decide** la flecha | Desde un mismo estado, eventos distintos llevan a lugares distintos |
| Número **finito** de estados | Por eso se puede dibujar completo; es el "finito" de *autómata finito* |

**Por qué existe.** Un grafo sin etiquetas solo dice "de aquí se puede pasar allá". Para describir un sistema que reacciona a eventos (un torniquete, una máquina expendedora, un analizador de texto) hace falta saber **qué evento** provoca cada cambio. La etiqueta convierte el grafo en una **regla de comportamiento**: dado dónde estoy y qué ocurrió, sé exactamente a dónde voy.

Convención de los diagramas de texto de este material:

```
 ──▶ (Inicio)          estado inicial
     ((Fin))           estado de aceptación (doble círculo)
     (X) ──a──▶ (Y)    transición de X a Y con el símbolo a
```

Puede haber **varias flechas entre los mismos dos estados** con distinta etiqueta (se dibujan como una sola flecha con las etiquetas separadas por coma) y **lazos** (una flecha del estado hacia sí mismo: "con este símbolo no cambia de situación").

Lo que **no** debe haber, por ahora, son dos flechas que salgan del **mismo** estado con la **misma** etiqueta hacia estados distintos: el sistema no sabría cuál seguir. Esa situación tiene nombre (**no determinismo**) y se estudia a propósito en la Unidad 3.

#### Ejemplo: un torniquete

Un torniquete de metro tiene dos estados: **Bloqueado** y **Libre**. Los eventos posibles son insertar una **moneda** o **empujar**.

| Estado actual | Símbolo | Estado siguiente |
|---|---|---|
| Bloqueado | moneda | Libre |
| Bloqueado | empujar | Bloqueado |
| Libre | moneda | Libre |
| Libre | empujar | Bloqueado |

```
   empujar                         moneda
  ┌─────┐                         ┌─────┐
  ▼     │                         ▼     │
 ──▶ (Bloqueado) ───── moneda ───▶ (Libre)
        ▲                           │
        └──────── empujar ──────────┘
```

Lectura: estando **Bloqueado**, un `empujar` no cambia nada (lazo); una `moneda` lo pasa a **Libre**. Estando **Libre**, otra `moneda` no cambia nada (lazo) y un `empujar` lo regresa a **Bloqueado**.

Observa que la tabla tiene **una fila por cada combinación** (estado, símbolo): 2 estados × 2 símbolos = 4 filas. Una tabla así es la descripción completa del sistema; el diagrama es solo su dibujo.

#### El diagrama en Java

Un diagrama de transición se guarda igual que el grafo de 1.1.1, con objetos enlazados, pero ahora cada flecha es un objeto que guarda su **etiqueta** y una **referencia** al estado destino:

```java
public class Transicion {

    private String simbolo = "";
    private Estado destino = null;

    public Transicion(String simbolo, Estado destino) {
        this.simbolo = simbolo;
        this.destino = destino;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public Estado getDestino() {
        return destino;
    }
}
```

```java
import java.util.ArrayList;

public class Estado {

    private String nombre = "";
    private boolean aceptacion = false;
    private ArrayList<Transicion> transiciones = new ArrayList<Transicion>();   // flechas que salen de este estado

    public Estado(String nombre, boolean aceptacion) {
        this.nombre = nombre;
        this.aceptacion = aceptacion;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean esAceptacion() {
        return aceptacion;
    }

    public ArrayList<Transicion> getTransiciones() {
        return transiciones;
    }

    public void conectar(String simbolo, Estado destino) {
        transiciones.add(new Transicion(simbolo, destino));
    }

    // Estado al que lleva el símbolo, o null si no hay flecha con esa etiqueta
    public Estado siguiente(String simbolo) {
        for (Transicion t : transiciones)
            if (t.getSimbolo().equals(simbolo)) return t.getDestino();
        return null;
    }
}
```

En memoria, cada estado apunta a sus transiciones y cada transición apunta a otro estado. El diagrama completo es una red de objetos enlazados:

```
 ┌─────────────────┐        ┌──────────────────────┐
 │ Estado          │        │ Transicion           │
 │ "Bloqueado"     │──────▶ │ simbolo = "moneda"   │──── destino ───▶ Estado "Libre"
 │ transiciones:[●,●]       └──────────────────────┘
 └──────────────┼──┘        ┌──────────────────────┐
                └─────────▶ │ simbolo = "empujar"  │──── destino ───▶ (el mismo "Bloqueado")
                            └──────────────────────┘
```

El lazo `empujar` es una transición cuyo destino es **una referencia al mismo objeto** que la contiene. Para Java no tiene nada de especial.

```java
Estado bloqueado = new Estado("Bloqueado", false);
Estado libre = new Estado("Libre", false);

bloqueado.conectar("moneda", libre);
bloqueado.conectar("empujar", bloqueado);   // lazo
libre.conectar("moneda", libre);            // lazo
libre.conectar("empujar", bloqueado);

Estado actual = bloqueado;                  // estado inicial
String[] eventos = { "empujar", "moneda", "empujar", "empujar" };

for (String simbolo : eventos) {
    actual = actual.siguiente(simbolo);
    System.out.printf("%-8s -> %s%n", simbolo, actual.getNombre());
}

// → empujar  -> Bloqueado
// → moneda   -> Libre
// → empujar  -> Bloqueado
// → empujar  -> Bloqueado
```

Traza del recorrido (la variable `actual` es una **referencia que se mueve** de un objeto a otro; ningún estado se copia ni se crea durante la simulación):

| Paso | `actual` antes | Símbolo | Transición que se sigue | `actual` después |
|---|---|---|---|---|
| 1 | Bloqueado | empujar | lazo `empujar` | Bloqueado |
| 2 | Bloqueado | moneda | `moneda` → Libre | Libre |
| 3 | Libre | empujar | `empujar` → Bloqueado | Bloqueado |
| 4 | Bloqueado | empujar | lazo `empujar` | Bloqueado |

Si se pide un símbolo para el que no hay flecha, `siguiente` devuelve `null`:

```java
System.out.println(bloqueado.siguiente("patada"));   // → null
```

> **Ojo con dos flechas con la misma etiqueta.** Si por error se conecta `bloqueado.conectar("moneda", libre)` y también `bloqueado.conectar("moneda", bloqueado)`, el método `siguiente` devuelve **la primera** que encuentra y la otra queda ignorada sin aviso. El programa no falla, pero el diagrama ya no describe un sistema con una única respuesta por evento.

Este programa ya es, en esencia, un **autómata**: un conjunto de estados, un estado inicial y una tabla que dice a dónde ir con cada símbolo. En la Unidad 3 se usan estas mismas clases, `Estado` y `Transicion`, con los estados de aceptación, y se formaliza.

### 1.2.2. Ejercicio de traducción de un problema a notación de grafo

Traducir un problema a un grafo es la habilidad central de la materia. Se hace siempre en cinco pasos:

| Paso | Pregunta | Resultado |
|---|---|---|
| 1 | ¿En qué **situaciones** puede estar el sistema? | Los nodos (estados) |
| 2 | ¿Qué **eventos o símbolos** pueden ocurrir? | Las etiquetas |
| 3 | Para cada situación y cada evento, ¿a qué situación se pasa? | Las aristas |
| 4 | ¿Dónde empieza? ¿Qué situación es la meta? | Estado inicial y estado final |
| 5 | ¿Falta alguna combinación o sobra algún nodo inalcanzable? | Revisión con recorridos |

El paso más difícil es el 1. Un estado no es "lo que pasó", sino **lo que hay que recordar** de lo que pasó para poder decidir después. Dos historias distintas que llevan a la misma decisión futura deben ser **el mismo estado**: por ejemplo, en una máquina de café, "metí una moneda de $5" y "metí dos de $5 y me devolvieron una" dejan la máquina igual.

| Error típico | Por qué ocurre | Cómo se corrige |
|---|---|---|
| Usar eventos como estados ("Moneda5", "Moneda10") | Se confunde lo que **pasa** con la **situación** en que queda el sistema | Preguntar "¿cómo está el sistema **después** del evento?" |
| Crear un estado por cada historia posible | Se intenta recordar todo el pasado | Fusionar los estados desde los cuales el futuro se comporta igual |
| Olvidar combinaciones (estado, símbolo) | Solo se piensa en el "camino feliz" | Llenar la tabla completa: estados × símbolos |
| Estados que nunca se alcanzan | Se dibujaron "por si acaso" | Revisar con `alcanzable` desde el estado inicial (paso 5) |
| No marcar el estado final | Se asume que "terminar de leer" es suficiente | Decidir explícitamente qué situaciones cuentan como éxito |

#### Problema

> Una máquina vende café a **$10**. Acepta monedas de **$5** y de **$10**. Cuando se completan $10, entrega el café. Si ya hay $5 depositados, no acepta una moneda de $10 (la devuelve).

**Paso 1. Estados.** ¿Cuánto dinero hay dentro de la máquina?

- `S0`: $0 depositados (inicio)
- `S5`: $5 depositados
- `Listo`: $10 completos, se entrega el café (final)

No hacen falta más estados: la máquina nunca necesita recordar **qué** monedas entraron, solo **cuánto** suman. Que la suma sea lo único importante es lo que mantiene el diagrama pequeño.

**Paso 2. Símbolos.** `5` y `10` (el valor de la moneda).

**Paso 3. Transiciones.**

| Estado actual | Moneda | Estado siguiente |
|---|---|---|
| S0 | 5 | S5 |
| S0 | 10 | Listo |
| S5 | 5 | Listo |
| S5 | 10 | *(no definida: la devuelve)* |
| Listo | 5, 10 | *(no definida: el proceso terminó)* |

Son 3 estados × 2 símbolos = 6 combinaciones; la tabla las revisa todas, y en tres de ellas decide conscientemente que **no hay transición**.

**Paso 4. Diagrama.**

```
        ┌────────────── 10 ──────────────┐
        │                                ▼
 ──▶ (S0) ──── 5 ────▶ (S5) ──── 5 ────▶ ((Listo))
```

**Paso 5. Revisión con recorridos.** Con lo visto en 1.1.2 se comprueba que la meta sea alcanzable y se cuentan los caminos:

```java
Grafo maquina = new Grafo(true);
maquina.agregarArista("S0", "S5");      // 5
maquina.agregarArista("S0", "Listo");   // 10
maquina.agregarArista("S5", "Listo");   // 5

System.out.println(maquina.alcanzable("S0", "Listo"));   // → true
System.out.println(maquina.alcanzable("S5", "S0"));      // → false  (no se puede "des-depositar")
System.out.println(maquina.tieneCiclo());                // → false  (el proceso siempre avanza)
```

Que no haya ciclos tiene un significado concreto: cada moneda aceptada **acerca** a la meta, así que la máquina nunca puede quedarse dando vueltas. Caminos de `S0` a `Listo` hay exactamente dos: `10` directo, o `5` y luego `5`.

Y se simula con los objetos `Estado` de 1.2.1. Cuando una combinación no está definida, `siguiente` devuelve `null` y el programa lo avisa en lugar de fallar:

```java
Estado s0 = new Estado("S0", false);
Estado s5 = new Estado("S5", false);
Estado listo = new Estado("Listo", true);     // estado de aceptación

s0.conectar("5", s5);
s0.conectar("10", listo);
s5.conectar("5", listo);

Estado actual = s0;
int[] monedas = { 5, 10, 5 };

for (int moneda : monedas) {
    Estado siguiente = actual.siguiente(String.valueOf(moneda));
    if (siguiente != null) {
        System.out.println("Moneda de $" + moneda + ": " + actual.getNombre() + " -> " + siguiente.getNombre());
        actual = siguiente;
    } else {
        System.out.println("Moneda de $" + moneda + ": se devuelve (no hay transición desde " + actual.getNombre() + ")");
    }
}

System.out.println(actual.esAceptacion() ? "Se entrega el café" : "Falta dinero");

// → Moneda de $5: S0 -> S5
// → Moneda de $10: se devuelve (no hay transición desde S5)
// → Moneda de $5: S5 -> Listo
// → Se entrega el café
```

| Moneda | `actual` antes | `siguiente(...)` | Acción | `actual` después |
|---|---|---|---|---|
| 5 | S0 | S5 | Avanza | S5 |
| 10 | S5 | `null` | Se devuelve la moneda; no cambia de estado | S5 |
| 5 | S5 | Listo | Avanza | Listo |
| — | Listo | — | `esAceptacion()` es `true` | Se entrega el café |

> **Ojo con la transición no definida.** Si para un estado y un símbolo no hay flecha, el sistema "se atora". En un diagrama de autómata esto significa que la cadena **no se acepta**. En Java, `siguiente` devuelve `null` y hay que revisarlo **antes** de usar el resultado: escribir `actual.siguiente("10").getNombre()` cuando no hay flecha lanza `NullPointerException`.

Qué hacer con una combinación no definida depende de lo que modela el problema:

```
 ¿Qué debe pasar si llega un símbolo sin flecha?
 │
 ├── El evento se ignora o se rechaza, y el sistema sigue igual
 │       → no cambiar de estado (como la moneda de $10 en S5)
 │
 ├── El error ya no tiene remedio
 │       → mandar a un estado "trampa" del que no se sale (Unidad 3)
 │
 └── Es una situación imposible por diseño
         → detener el programa con un mensaje de error
```

#### Ahora tú

Traduce a grafo dirigido etiquetado y simula con Java (usando las clases `Estado` y `Transicion`) este problema:

> Una caja fuerte se abre con la secuencia de botones **A, B**. Si se presiona un botón equivocado, la secuencia se reinicia. Al presionar A y luego B, la caja queda **Abierta**.

Pistas: tres estados (nada correcto todavía, llevo la `A`, abierta); dos símbolos (`A` y `B`); cada estado tiene una flecha por cada símbolo salvo el estado final.
