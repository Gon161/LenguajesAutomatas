# Unidad 1. Fundamentos de grafos

Un **grafo** es una forma de dibujar y estudiar **cosas** y las **conexiones** entre ellas: ciudades y carreteras, personas y amistades, páginas web y enlaces. Es la herramienta más simple para describir relaciones, y por eso es la base de toda la materia.

La razón de empezar aquí es esta: **un autómata es un grafo**. Los estados son los nodos y las transiciones son las aristas. Si entiendes bien el vocabulario de esta unidad (nodo, arista, camino, ciclo, alcanzabilidad), los diagramas de autómatas de la Unidad 3 serán un caso particular de lo que ya sabes.

> En esta unidad el código usa el espacio de nombres `System.Collections.Generic` (`Dictionary`, `List`, `HashSet`, `Queue`). En proyectos de .NET 6 o superior ya viene incluido de forma implícita.

## 1.1. Conceptos básicos de teoría de grafos

### 1.1.1. Definición de grafo dirigido y no dirigido

Formalmente, un grafo es un par **G = (V, E)**:

- **V** es un conjunto finito de **vértices** (también llamados **nodos**).
- **E** es un conjunto de **aristas**: conexiones entre dos nodos.

Ejemplo: **G = ({A, B, C, D}, {(A,B), (B,C), (C,A), (C,D)})**

Es un grafo de 4 nodos y 4 aristas.

#### Grafo no dirigido

Las aristas **no tienen sentido**: si A está conectado con B, entonces B está conectado con A. La arista se escribe como un conjunto `{A, B}` y se dibuja como una línea simple.

```
Ana ─────── Beto
 │
Carla ───── Diego
```

Ejemplo real: una red de amistades. Si Ana es amiga de Beto, Beto es amigo de Ana.

#### Grafo dirigido (dígrafo)

Cada arista **tiene un sentido**: va de un nodo **origen** a un nodo **destino**. Se escribe como un par ordenado `(A, B)` y se dibuja como una flecha.

```
 A ───▶ B
 ▲      │
 │      ▼
 └───── C ───▶ D
```

Ejemplo real: calles de un solo sentido. Que puedas ir de A a B no significa que puedas regresar de B a A.

#### Comparación

| Característica | No dirigido | Dirigido |
|---|---|---|
| Se dibuja con | Líneas | Flechas |
| Arista | Conjunto `{A, B}` | Par ordenado `(A, B)` |
| `{A,B}` = `{B,A}` | Sí | No: `(A,B)` ≠ `(B,A)` |
| Ejemplo | Amistades | Calles de un solo sentido, **autómatas** |

> En esta materia casi siempre trabajaremos con grafos **dirigidos**: una transición de un autómata va de un estado a otro, nunca al revés.

#### Cómo se guarda un grafo en memoria

La forma más común es la **lista de adyacencia**: para cada nodo se guarda la lista de nodos a los que llega.

```csharp
// Grafo dirigido: A→B, B→C, C→A, C→D
var grafo = new Dictionary<string, List<string>>
{
    ["A"] = new() { "B" },
    ["B"] = new() { "C" },
    ["C"] = new() { "A", "D" },
    ["D"] = new() { }
};

Console.WriteLine(string.Join(", ", grafo["C"]));   // → A, D
```

La otra forma es la **matriz de adyacencia**: una tabla donde la fila es el origen, la columna es el destino y un `1` indica que existe la arista.

| origen \ destino | A | B | C | D |
|---|---|---|---|---|
| **A** | 0 | 1 | 0 | 0 |
| **B** | 0 | 0 | 1 | 0 |
| **C** | 1 | 0 | 0 | 1 |
| **D** | 0 | 0 | 0 | 0 |

| | Lista de adyacencia | Matriz de adyacencia |
|---|---|---|
| Espacio | Solo guarda las aristas que existen | Siempre n × n |
| Ver si existe `(A,B)` | Recorre la lista de A | Acceso directo |
| Conviene cuando | Hay pocas aristas (lo habitual) | Hay muchas aristas |

En un grafo **no dirigido** la matriz es simétrica (la mitad de arriba es espejo de la de abajo).

Para el resto de la unidad usaremos esta clase, que guarda el grafo como lista de adyacencia:

```csharp
class Grafo
{
    private readonly Dictionary<string, List<string>> _ady = new();

    public bool Dirigido { get; }

    public Grafo(bool dirigido) => Dirigido = dirigido;

    public void AgregarNodo(string nodo)
    {
        if (!_ady.ContainsKey(nodo))
            _ady[nodo] = new List<string>();
    }

    public void AgregarArista(string origen, string destino)
    {
        AgregarNodo(origen);
        AgregarNodo(destino);
        _ady[origen].Add(destino);

        // En un grafo no dirigido la arista se guarda en ambos sentidos
        if (!Dirigido && origen != destino)
            _ady[destino].Add(origen);
    }

    public IEnumerable<string> Nodos => _ady.Keys;

    public IReadOnlyList<string> Vecinos(string nodo) => _ady[nodo];
}
```

> ⚠️ **Grafo no es gráfica.** Una *gráfica* es un diagrama de barras o de pastel. Un *grafo* es un conjunto de nodos y aristas. En inglés ambos son distintos: *chart* contra *graph*.

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
| **Ciclo** | Camino que empieza y termina en el mismo nodo | A → B → C → A |
| **Alcanzable** | El nodo destino se puede llegar siguiendo aristas desde el origen | D es alcanzable desde A |

En un grafo **no dirigido** se habla simplemente de **grado** (el número de aristas que tiene el nodo).

Un camino de longitud 0 (un solo nodo) también cuenta: todo nodo es alcanzable desde sí mismo.

#### La dirección importa

En el ejemplo anterior:

- D **es alcanzable** desde A (A → B → C → D).
- A **no es alcanzable** desde D: D no tiene aristas de salida.

Alcanzar a B desde A no garantiza poder volver. Por eso en un grafo dirigido hay que preguntar siempre "**desde** qué nodo **hasta** cuál".

#### Alcanzabilidad en C#

La pregunta "¿puedo llegar de X a Y?" se responde recorriendo el grafo con una **búsqueda en anchura** (*BFS*): se parte de X, se visitan sus vecinos, luego los vecinos de los vecinos, y así hasta agotar los nodos o encontrar Y. Un conjunto `HashSet` evita visitar dos veces el mismo nodo (y que el programa se cicle).

Se agregan estos métodos a la clase `Grafo`:

```csharp
public bool Alcanzable(string desde, string hasta)
{
    var visitados = new HashSet<string> { desde };
    var cola = new Queue<string>();
    cola.Enqueue(desde);

    while (cola.Count > 0)
    {
        string actual = cola.Dequeue();
        if (actual == hasta) return true;

        foreach (string vecino in _ady[actual])
        {
            if (visitados.Add(vecino))      // Add devuelve false si ya estaba
                cola.Enqueue(vecino);
        }
    }

    return false;
}

public int GradoSalida(string nodo) => _ady[nodo].Count;

public int GradoEntrada(string nodo)
{
    int total = 0;
    foreach (var lista in _ady.Values)
        foreach (string destino in lista)
            if (destino == nodo) total++;
    return total;
}
```

Uso:

```csharp
var g = new Grafo(dirigido: true);
g.AgregarArista("A", "B");
g.AgregarArista("B", "C");
g.AgregarArista("C", "A");
g.AgregarArista("C", "D");

Console.WriteLine(g.Alcanzable("A", "D"));   // → True
Console.WriteLine(g.Alcanzable("D", "A"));   // → False

foreach (string n in g.Nodos)
    Console.WriteLine($"{n}: entrada={g.GradoEntrada(n)}, salida={g.GradoSalida(n)}");

// → A: entrada=1, salida=1
// → B: entrada=1, salida=1
// → C: entrada=1, salida=2
// → D: entrada=1, salida=0
```

> Esta misma idea aparece después en los autómatas: un estado de aceptación es **inútil** si no es alcanzable desde el estado inicial.

#### Detección de ciclos (grafo dirigido)

Para saber si un grafo dirigido tiene ciclos se recorre en profundidad (*DFS*) marcando cada nodo como **sin visitar**, **en curso** o **terminado**. Si durante el recorrido se encuentra una arista hacia un nodo que está **en curso**, se volvió a un nodo del camino actual: hay un ciclo.

```csharp
public bool TieneCiclo()   // solo para grafos dirigidos
{
    // 0 = sin visitar, 1 = en curso, 2 = terminado
    var estado = new Dictionary<string, int>();
    foreach (string n in _ady.Keys) estado[n] = 0;

    bool Visitar(string n)
    {
        estado[n] = 1;
        foreach (string vecino in _ady[n])
        {
            if (estado[vecino] == 1) return true;                  // volvimos a un nodo del camino
            if (estado[vecino] == 0 && Visitar(vecino)) return true;
        }
        estado[n] = 2;
        return false;
    }

    foreach (string n in _ady.Keys)
        if (estado[n] == 0 && Visitar(n)) return true;

    return false;
}
```

```csharp
Console.WriteLine(g.TieneCiclo());   // → True  (A → B → C → A)

var sinCiclo = new Grafo(dirigido: true);
sinCiclo.AgregarArista("A", "B");
sinCiclo.AgregarArista("B", "C");
Console.WriteLine(sinCiclo.TieneCiclo());   // → False
```

> ⚠️ En un grafo **no dirigido** esta función no sirve: la arista `{A,B}` se guarda en ambos sentidos y siempre parecería un ciclo A → B → A. Un ciclo en un no dirigido debe tener al menos tres nodos distintos; eso se retoma en la Unidad 2.

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

Convención de los diagramas de texto de este material:

```
 ──▶ (Inicio)          estado inicial
     ((Fin))           estado de aceptación (doble círculo)
     (X) ──a──▶ (Y)    transición de X a Y con el símbolo a
```

Puede haber **varias flechas entre los mismos dos estados** con distinta etiqueta (se dibujan como una sola flecha con las etiquetas separadas por coma) y **lazos** (una flecha del estado hacia sí mismo: "con este símbolo no cambia de situación").

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

#### El diagrama en C#

Una tabla de transiciones se guarda naturalmente como un diccionario cuya **llave** es la pareja (estado actual, símbolo) y cuyo **valor** es el estado siguiente:

```csharp
var transiciones = new Dictionary<(string Estado, string Simbolo), string>
{
    [("Bloqueado", "moneda")]  = "Libre",
    [("Bloqueado", "empujar")] = "Bloqueado",
    [("Libre", "moneda")]      = "Libre",
    [("Libre", "empujar")]     = "Bloqueado",
};

string estado = "Bloqueado";   // estado inicial

foreach (string simbolo in new[] { "empujar", "moneda", "empujar", "empujar" })
{
    estado = transiciones[(estado, simbolo)];
    Console.WriteLine($"{simbolo,-8} → {estado}");
}

// → empujar  → Bloqueado
// → moneda   → Libre
// → empujar  → Bloqueado
// → empujar  → Bloqueado
```

Este programa ya es, en esencia, un **autómata**: un conjunto de estados, un estado inicial y una tabla que dice a dónde ir con cada símbolo. En la Unidad 3 se le añaden los estados de aceptación y se formaliza.

### 1.2.2. Ejercicio de traducción de un problema a notación de grafo

Traducir un problema a un grafo es la habilidad central de la materia. Se hace siempre en cinco pasos:

| Paso | Pregunta | Resultado |
|---|---|---|
| 1 | ¿En qué **situaciones** puede estar el sistema? | Los nodos (estados) |
| 2 | ¿Qué **eventos o símbolos** pueden ocurrir? | Las etiquetas |
| 3 | Para cada situación y cada evento, ¿a qué situación se pasa? | Las aristas |
| 4 | ¿Dónde empieza? ¿Qué situación es la meta? | Estado inicial y estado final |
| 5 | ¿Falta alguna combinación o sobra algún nodo inalcanzable? | Revisión con recorridos |

#### Problema

> Una máquina vende café a **$10**. Acepta monedas de **$5** y de **$10**. Cuando se completan $10, entrega el café. Si ya hay $5 depositados, no acepta una moneda de $10 (la devuelve).

**Paso 1. Estados.** ¿Cuánto dinero hay dentro de la máquina?

- `S0`: $0 depositados (inicio)
- `S5`: $5 depositados
- `Listo`: $10 completos, se entrega el café (final)

**Paso 2. Símbolos.** `5` y `10` (el valor de la moneda).

**Paso 3. Transiciones.**

| Estado actual | Moneda | Estado siguiente |
|---|---|---|
| S0 | 5 | S5 |
| S0 | 10 | Listo |
| S5 | 5 | Listo |
| S5 | 10 | *(no definida: la devuelve)* |
| Listo | 5, 10 | *(no definida: el proceso terminó)* |

**Paso 4. Diagrama.**

```
        ┌────────────── 10 ──────────────┐
        │                                ▼
 ──▶ (S0) ──── 5 ────▶ (S5) ──── 5 ────▶ ((Listo))
```

**Paso 5. Revisión con recorridos.** Con lo visto en 1.1.2 se comprueba que la meta sea alcanzable y se cuentan los caminos:

```csharp
var maquina = new Grafo(dirigido: true);
maquina.AgregarArista("S0", "S5");      // 5
maquina.AgregarArista("S0", "Listo");   // 10
maquina.AgregarArista("S5", "Listo");   // 5

Console.WriteLine(maquina.Alcanzable("S0", "Listo"));   // → True
Console.WriteLine(maquina.Alcanzable("S5", "S0"));      // → False  (no se puede "des-depositar")
Console.WriteLine(maquina.TieneCiclo());                // → False  (el proceso siempre avanza)
```

Y se simula con la tabla de transiciones. Cuando una combinación no está definida, el programa lo avisa en lugar de fallar:

```csharp
var tabla = new Dictionary<(string, int), string>
{
    [("S0", 5)]  = "S5",
    [("S0", 10)] = "Listo",
    [("S5", 5)]  = "Listo",
};

string actual = "S0";
foreach (int moneda in new[] { 5, 10, 5 })
{
    if (tabla.TryGetValue((actual, moneda), out string? siguiente))
    {
        Console.WriteLine($"Moneda de ${moneda}: {actual} → {siguiente}");
        actual = siguiente;
    }
    else
    {
        Console.WriteLine($"Moneda de ${moneda}: se devuelve (no hay transición desde {actual})");
    }
}

// → Moneda de $5: S0 → S5
// → Moneda de $10: se devuelve (no hay transición desde S5)
// → Moneda de $5: S5 → Listo
```

> ⚠️ **Transición no definida.** Si para un estado y un símbolo no hay flecha, el sistema "se atora". En un diagrama de autómata esto significa que la cadena **no se acepta**. En C# usa siempre `TryGetValue` (o `ContainsKey`) antes de leer del diccionario; el índice `tabla[(...)]` lanza `KeyNotFoundException`.

#### Ahora tú

Traduce a grafo dirigido etiquetado y simula con C# este problema:

> Una caja fuerte se abre con la secuencia de botones **A, B**. Si se presiona un botón equivocado, la secuencia se reinicia. Al presionar A y luego B, la caja queda **Abierta**.

Pistas: tres estados (nada correcto todavía, llevo la `A`, abierta); dos símbolos (`A` y `B`); cada estado tiene una flecha por cada símbolo salvo el estado final.
