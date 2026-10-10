# Unidad 2. Fundamentos de árboles

Un **árbol** es un grafo con una forma muy específica: sin ciclos y de una sola pieza. Esa restricción lo vuelve ideal para representar **jerarquías**: carpetas dentro de carpetas, el organigrama de una empresa, el índice de un libro.

En esta materia el árbol cumple una función concreta: el **árbol de derivación** muestra, paso a paso, cómo una gramática construye una cadena. Quien sabe leer un árbol de derivación entiende por qué una cadena pertenece (o no) a un lenguaje, y entiende cómo un compilador analiza un programa.

> En esta unidad se reutiliza la clase `Grafo` de la Unidad 1. Los ejemplos con `Select`, `Sum` o `All` usan LINQ (`System.Linq`), que ya viene incluido de forma implícita en .NET 6 o superior.

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

#### ¿Un grafo dado es un árbol? (en C#)

La última propiedad da un criterio directo: se comprueba que el grafo sea conexo y que tenga `n − 1` aristas. Se usa la clase `Grafo` (no dirigida) de la Unidad 1; como cada arista está guardada dos veces (una por cada extremo), el total de aristas es la suma de los tamaños de las listas entre 2.

```csharp
bool EsArbol(Grafo grafo)
{
    var nodos = grafo.Nodos.ToList();
    if (nodos.Count == 0) return false;

    int aristas = nodos.Sum(n => grafo.Vecinos(n).Count) / 2;
    bool conexo = nodos.All(n => grafo.Alcanzable(nodos[0], n));

    return conexo && aristas == nodos.Count - 1;
}
```

```csharp
var arbol = new Grafo(dirigido: false);
arbol.AgregarArista("A", "B");
arbol.AgregarArista("A", "C");
arbol.AgregarArista("B", "D");
arbol.AgregarArista("B", "E");
Console.WriteLine(EsArbol(arbol));      // → True   (5 nodos, 4 aristas, conexo)

var conCiclo = new Grafo(dirigido: false);
conCiclo.AgregarArista("A", "B");
conCiclo.AgregarArista("B", "C");
conCiclo.AgregarArista("C", "A");
Console.WriteLine(EsArbol(conCiclo));   // → False  (3 nodos, 3 aristas)

var desconexo = new Grafo(dirigido: false);
desconexo.AgregarArista("A", "B");
desconexo.AgregarArista("C", "D");
Console.WriteLine(EsArbol(desconexo));  // → False  (no se llega de A a C)
```

> Esta función supone un grafo **simple**: sin lazos y sin dos aristas repetidas entre los mismos nodos.

#### Árbol con raíz

En los árboles que se usan en computación se **elige un nodo como raíz** y se dibuja arriba; todas las aristas pasan a tener un sentido natural: del nodo de arriba (**padre**) hacia los de abajo (**hijos**). Así el árbol deja de ser un grafo no dirigido y se convierte en un **grafo dirigido sin ciclos** donde cada nodo, salvo la raíz, tiene exactamente un padre.

```
        A              Se escoge A como raíz y se dibuja hacia abajo:
       / \             las aristas ahora van de padre a hijo.
      B   C
     / \
    D   E
```

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

> ⚠️ **El nivel de la raíz depende del libro.** En este material la raíz está en el nivel **0** (el nivel de un nodo es el número de aristas que lo separan de la raíz). Algunos textos empiezan en 1; si usas otra fuente, confirma con cuál cuenta.

Las **hojas** son importantes en esta materia: en un árbol de derivación, las hojas leídas de izquierda a derecha forman la cadena resultante.

#### El árbol en C#

Un árbol con raíz se programa con un nodo que guarda su valor y la lista de sus hijos. Es la misma lista de adyacencia de la Unidad 1, pero cada nodo apunta directamente a sus hijos:

```csharp
class Nodo
{
    public string Valor { get; }
    public List<Nodo> Hijos { get; } = new();

    public Nodo(string valor) => Valor = valor;

    // Agrega un hijo y lo devuelve, para poder seguir colgando nodos de él
    public Nodo Agregar(string valor)
    {
        var hijo = new Nodo(valor);
        Hijos.Add(hijo);
        return hijo;
    }

    public bool EsHoja => Hijos.Count == 0;

    // Nivel más profundo del subárbol que cuelga de este nodo
    public int Altura()
    {
        if (EsHoja) return 0;
        return 1 + Hijos.Max(h => h.Altura());
    }

    public int ContarHojas() => EsHoja ? 1 : Hijos.Sum(h => h.ContarHojas());

    // Muestra el árbol con sangría según el nivel
    public void Imprimir(int nivel = 0)
    {
        Console.WriteLine($"{new string(' ', nivel * 3)}{Valor}  (nivel {nivel})");
        foreach (var hijo in Hijos)
            hijo.Imprimir(nivel + 1);
    }
}
```

Se construye el árbol del ejemplo:

```csharp
var a = new Nodo("A");
var b = a.Agregar("B");
a.Agregar("C");
var d = a.Agregar("D");
b.Agregar("E");
b.Agregar("F");
var nodoG = d.Agregar("G");
nodoG.Agregar("H");

a.Imprimir();
// → A  (nivel 0)
// →    B  (nivel 1)
// →       E  (nivel 2)
// →       F  (nivel 2)
// →    C  (nivel 1)
// →    D  (nivel 1)
// →       G  (nivel 2)
// →          H  (nivel 3)

Console.WriteLine(a.Altura());        // → 3
Console.WriteLine(a.ContarHojas());   // → 4  (C, E, F, H)
Console.WriteLine(b.Altura());        // → 1  (subárbol de B)
```

La función `Altura` es **recursiva**: la altura de un nodo es 0 si es hoja, y 1 más la altura de su hijo más profundo si no lo es. Casi todo lo que se hace con árboles se resuelve con esta misma idea: tratar a cada hijo como un árbol más pequeño.

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

#### Gramática de ejemplo

```
S → a S b | ε
```

(`ε` es la cadena vacía.) Se lee: "`S` puede reemplazarse por `aSb`, o por nada". Esta gramática genera las cadenas con tantas `a` como `b`, en ese orden: `ε`, `ab`, `aabb`, `aaabbb`, …

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

#### Correspondencia entre el árbol y la gramática

| En el árbol | En la gramática |
|---|---|
| Raíz | Símbolo inicial |
| Nodo interno | Un no terminal |
| Hoja | Un terminal (o `ε`) |
| Los hijos de un nodo, de izquierda a derecha | Lado derecho de la producción que se aplicó |
| Hojas de izquierda a derecha | Cadena derivada |

#### Construir el árbol en C#

La gramática se guarda como un diccionario: a cada no terminal le corresponde la lista de sus producciones, y cada producción es un arreglo de símbolos (un arreglo vacío es `ε`). Al nodo de la Unidad 2.1.2 se le agrega un método para leer la frontera:

```csharp
// Se agrega a la clase Nodo
public string Frontera()
{
    if (EsHoja) return Valor == "ε" ? "" : Valor;
    return string.Concat(Hijos.Select(h => h.Frontera()));
}
```

La función `Derivar` construye el árbol aplicando las producciones elegidas (por índice) en el orden en que se necesitan, de arriba hacia abajo y de izquierda a derecha:

```csharp
// S → a S b | ε
var gramatica = new Dictionary<string, List<string[]>>
{
    ["S"] = new() { new[] { "a", "S", "b" }, new string[] { } }   // [0] = aSb, [1] = ε
};

Nodo Derivar(Dictionary<string, List<string[]>> reglas, string simbolo, Queue<int> elecciones)
{
    var nodo = new Nodo(simbolo);
    if (!reglas.ContainsKey(simbolo)) return nodo;      // terminal: es una hoja

    string[] produccion = reglas[simbolo][elecciones.Dequeue()];
    if (produccion.Length == 0) nodo.Agregar("ε");      // producción vacía

    foreach (string s in produccion)
        nodo.Hijos.Add(Derivar(reglas, s, elecciones));

    return nodo;
}

// Para aabb: S→aSb, S→aSb, S→ε
var arbolDerivacion = Derivar(gramatica, "S", new Queue<int>(new[] { 0, 0, 1 }));

arbolDerivacion.Imprimir();
// → S  (nivel 0)
// →    a  (nivel 1)
// →    S  (nivel 1)
// →       a  (nivel 2)
// →       S  (nivel 2)
// →          ε  (nivel 3)
// →       b  (nivel 2)
// →    b  (nivel 1)

Console.WriteLine(arbolDerivacion.Frontera());   // → aabb
```

> ⚠️ Todo nodo que **tiene hijos** es un no terminal; todo nodo **hoja** es un terminal (o `ε`). Si en un árbol de derivación ves una hoja que es un no terminal, la derivación **está incompleta**.

### 2.2.2. Relación entre jerarquía del árbol y reglas de producción

#### Un nodo interno = una producción

Cada nodo interno del árbol representa **una producción aplicada**: su etiqueta es el lado izquierdo y sus hijos, de izquierda a derecha, son el lado derecho. Por eso:

> **Número de nodos internos = número de producciones aplicadas.**

En el árbol de `aabb` hay 3 nodos internos (los tres `S`), y la derivación usó 3 producciones.

Leer un árbol equivale a leer la lista de producciones que se usaron: es la forma de **comprobar** que una cadena pertenece al lenguaje.

#### La jerarquía del árbol decide el orden de las operaciones

Cuando una gramática se escribe **por niveles**, su estructura obliga a que ciertas operaciones queden **más abajo** en el árbol. Lo que está más abajo se resuelve **primero**. Ejemplo con expresiones aritméticas:

```
E → E + T | T            suma:            nivel más alto
T → T * F | F            multiplicación:  un nivel más abajo
F → ( E ) | 2 | 3 | 4    números y paréntesis: el nivel más bajo
```

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

#### Evaluar recorriendo el árbol (en C#)

Como cada nodo conoce la regla que lo creó, calcular el valor de la expresión es recorrer el árbol de abajo hacia arriba. Se reutiliza `Derivar`; cada producción se elige por su índice dentro de la lista:

```csharp
// E → E + T | T         T → T * F | F         F → ( E ) | 2 | 3 | 4
var expresiones = new Dictionary<string, List<string[]>>
{
    ["E"] = new() { new[] { "E", "+", "T" }, new[] { "T" } },
    ["T"] = new() { new[] { "T", "*", "F" }, new[] { "F" } },
    ["F"] = new() { new[] { "(", "E", ")" }, new[] { "2" }, new[] { "3" }, new[] { "4" } }
};

// Producciones de la tabla, por índice: E→E+T(0) E→T(1) T→F(1) F→2(1) T→T*F(0) T→F(1) F→3(2) F→4(3)
var arbolExp = Derivar(expresiones, "E", new Queue<int>(new[] { 0, 1, 1, 1, 0, 1, 2, 3 }));

Console.WriteLine(arbolExp.Frontera());   // → 2+3*4

int Evaluar(Nodo n)
{
    bool tresHijos = n.Hijos.Count == 3;
    switch (n.Valor)
    {
        case "E": return tresHijos ? Evaluar(n.Hijos[0]) + Evaluar(n.Hijos[2]) : Evaluar(n.Hijos[0]);
        case "T": return tresHijos ? Evaluar(n.Hijos[0]) * Evaluar(n.Hijos[2]) : Evaluar(n.Hijos[0]);
        case "F": return tresHijos ? Evaluar(n.Hijos[1]) : int.Parse(n.Hijos[0].Valor);
        default:  throw new InvalidOperationException($"Símbolo inesperado: {n.Valor}");
    }
}

Console.WriteLine(Evaluar(arbolExp));     // → 14   (2 + (3 * 4), no (2 + 3) * 4 = 20)
```

> Si el árbol hubiera agrupado `(2 + 3)` más abajo, el resultado sería 20. **La forma del árbol cambia el significado de la cadena.**

#### Cuando la gramática no tiene niveles

Con la gramática `E → E + E | E * E | 2 | 3 | 4`, la cadena `2 + 3 * 4` se puede derivar con **dos árboles distintos**: uno que agrupa `2 + 3` primero (resultado 20) y otro que agrupa `3 * 4` primero (resultado 14). A una gramática donde una cadena tiene más de un árbol de derivación se le llama **ambigua**; los lenguajes de programación evitan esto escribiendo la gramática por niveles, como la de arriba.
