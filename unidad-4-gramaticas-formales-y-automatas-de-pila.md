# Unidad 4. Gramáticas formales y autómatas de pila

Los autómatas finitos de la Unidad 3 tienen un límite claro: **no pueden contar**. Un AFD que lee `aaa…abbb…` no puede recordar cuántas `a` llevó, porque solo recuerda "en qué estado estoy" y tiene un número finito de estados. Por eso no existe un AFD para el lenguaje `{ aⁿbⁿ : n ≥ 0 }` ni para los **paréntesis balanceados**, que son justo lo que hace falta para analizar un programa.

Esta unidad resuelve el problema por dos caminos que resultan equivalentes:

- Una **gramática libre de contexto** *genera* las cadenas del lenguaje mediante reglas de reemplazo (el árbol de derivación de la Unidad 2 es su dibujo).
- Un **autómata de pila** *reconoce* las cadenas añadiendo a un autómata finito una **pila**: una memoria sin límite de tamaño, pero que solo se puede leer por arriba.

La unidad cierra uniendo todo el curso: del grafo al autómata, del árbol a la gramática.

> Los ejemplos de esta unidad usan la clase `Gramatica` que se construye en 4.1.1, la clase `AutomataDePila` de 4.2.1 y las funciones `Cadenas` y `Equivalentes` de la Unidad 3 (3.3.2). Las clases se declaran **después** de las instrucciones de nivel superior del archivo.

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

#### ¿Por qué "libre de contexto"?

Porque el lado izquierdo de cada producción es **un solo no terminal**: `A` se puede reemplazar por `α` **sin importar qué símbolos tenga alrededor** (el contexto). Una regla como `aAb → aab` ("`A` solo se reemplaza si está entre una `a` y una `b`") **no** es libre de contexto.

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

#### Ejemplos de gramáticas

| Lenguaje | Gramática |
|---|---|
| `aⁿbⁿ` (n ≥ 0): mismas `a` que `b`, en ese orden | `S → aSb \| ε` |
| Paréntesis balanceados | `S → (S)S \| ε` |
| Palíndromos sobre {a, b} | `S → aSa \| bSb \| a \| b \| ε` |
| Expresiones aritméticas con `+` y `*` (Unidad 2) | `E → E+T \| T`   `T → T*F \| F`   `F → (E) \| n` |

> Los palíndromos funcionan así: cada `aSa` o `bSb` agrega el **mismo símbolo a ambos extremos**, y el caso base (`a`, `b` o `ε`) pone el centro (longitud impar o par).

#### La gramática en C#

La gramática se guarda como un diccionario: a cada no terminal (un `char` mayúscula) le corresponde la lista de lados derechos de sus producciones (la cadena vacía `""` representa `ε`).

```csharp
class Gramatica
{
    public char Inicial { get; }
    public Dictionary<char, List<string>> Producciones { get; }

    public Gramatica(char inicial, Dictionary<char, List<string>> producciones)
    {
        Inicial = inicial;
        Producciones = producciones;
    }

    public bool EsNoTerminal(char c) => Producciones.ContainsKey(c);

    public IEnumerable<char> Terminales => Producciones.Values
        .SelectMany(lados => lados)
        .SelectMany(lado => lado)
        .Where(c => !EsNoTerminal(c))
        .Distinct();

    private int PrimerNoTerminal(string forma)
    {
        for (int i = 0; i < forma.Length; i++)
            if (EsNoTerminal(forma[i])) return i;
        return -1;
    }

    // Todas las cadenas del lenguaje de longitud <= maxLargo (derivando siempre el no terminal más a la izquierda)
    public SortedSet<string> Genera(int maxLargo)
    {
        var resultado = new SortedSet<string>(Comparer<string>.Create((x, y) =>
            x.Length != y.Length ? x.Length.CompareTo(y.Length) : string.CompareOrdinal(x, y)));

        var vistas = new HashSet<string> { Inicial.ToString() };
        var cola = new Queue<string>();
        cola.Enqueue(Inicial.ToString());

        while (cola.Count > 0)
        {
            string forma = cola.Dequeue();
            int pos = PrimerNoTerminal(forma);

            if (pos < 0) { resultado.Add(forma); continue; }     // solo terminales: es una cadena del lenguaje

            foreach (string lado in Producciones[forma[pos]])
            {
                string nueva = forma.Substring(0, pos) + lado + forma.Substring(pos + 1);
                int terminales = nueva.Count(c => !EsNoTerminal(c));

                if (terminales <= maxLargo && vistas.Add(nueva))  // poda: ya no cabe en la longitud pedida
                    cola.Enqueue(nueva);
            }
        }

        return resultado;
    }

    // Una derivación por la izquierda de 'objetivo', o null si la cadena no pertenece al lenguaje
    public List<string>? Derivacion(string objetivo)
    {
        var padre = new Dictionary<string, string?> { [Inicial.ToString()] = null };
        var cola = new Queue<string>();
        cola.Enqueue(Inicial.ToString());

        while (cola.Count > 0)
        {
            string forma = cola.Dequeue();
            int pos = PrimerNoTerminal(forma);

            if (pos < 0)
            {
                if (forma != objetivo) continue;

                var camino = new List<string>();
                for (string? f = forma; f != null; f = padre[f]) camino.Add(f);
                camino.Reverse();
                return camino;
            }

            foreach (string lado in Producciones[forma[pos]])
            {
                string nueva = forma.Substring(0, pos) + lado + forma.Substring(pos + 1);

                // Poda: los terminales iniciales de la forma deben coincidir con el inicio del objetivo
                int p = PrimerNoTerminal(nueva);
                string prefijo = p < 0 ? nueva : nueva.Substring(0, p);
                if (!objetivo.StartsWith(prefijo)) continue;
                if (nueva.Count(c => !EsNoTerminal(c)) > objetivo.Length) continue;

                if (padre.TryAdd(nueva, forma)) cola.Enqueue(nueva);
            }
        }

        return null;
    }
}
```

Generar las cadenas de un lenguaje:

```csharp
var anbn = new Gramatica('S', new() { ['S'] = new() { "aSb", "" } });

foreach (string w in anbn.Genera(6))
    Console.WriteLine(w == "" ? "ε" : w);
// → ε
// → ab
// → aabb
// → aaabbb

var parentesis = new Gramatica('S', new() { ['S'] = new() { "(S)S", "" } });

Console.WriteLine(string.Join("  ", parentesis.Genera(6).Select(w => w == "" ? "ε" : w)));
// → ε  ()  (())  ()()  ((()))  (()())  (())()  ()(())  ()()()
```

Y encontrar la derivación de una cadena concreta:

```csharp
var palindromos = new Gramatica('S', new() { ['S'] = new() { "aSa", "bSb", "a", "b", "" } });

foreach (string w in new[] { "aabb", "abba", "abab" })
{
    var d = (w == "aabb" ? anbn : palindromos).Derivacion(w);
    Console.WriteLine(d == null
        ? $"{w}: no pertenece al lenguaje"
        : $"{w}: " + string.Join(" ⇒ ", d.Select(f => f == "" ? "ε" : f)));
}

// → aabb: S ⇒ aSb ⇒ aaSbb ⇒ aabb
// → abba: S ⇒ aSa ⇒ abSba ⇒ abba
// → abab: no pertenece al lenguaje
```

> ⚠️ En la gramática `S → aSb | ε` la cadena `aabb` **sí** pertenece, pero `aab` y `abab` **no**. Una cadena pertenece al lenguaje si **existe al menos una** derivación desde `S`; para demostrar que *no* pertenece hay que mostrar que ninguna derivación la produce.

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

> En este curso se estudian a fondo los tipos **3** (Unidad 3) y **2** (esta unidad). Los tipos 1 y 0 solo se presentan como panorama: son el tema de cursos posteriores de teoría de la computación.

#### Clasificar una gramática (en C#)

El tipo de una gramática se decide mirando la forma de **cada** producción: la gramática tiene el tipo más alto (el más restringido) que **todas** sus producciones cumplen.

```csharp
int TipoChomsky(List<(string Izq, string Der)> producciones)
{
    bool NoTerminal(char c) => char.IsUpper(c);

    bool regular = true, libre = true, sensible = true;

    foreach (var (izq, der) in producciones)
    {
        bool unNoTerminal = izq.Length == 1 && NoTerminal(izq[0]);

        if (!unNoTerminal) libre = false;

        // Regular (lineal por la derecha): A → ε | a | aB
        bool formaRegular = unNoTerminal && (
            der == "" ||
            (der.Length == 1 && !NoTerminal(der[0])) ||
            (der.Length == 2 && !NoTerminal(der[0]) && NoTerminal(der[1])));
        if (!formaRegular) regular = false;

        // Sensible al contexto: la forma no se acorta (simplificación: se ignora S → ε)
        if (der.Length < izq.Length) sensible = false;
    }

    if (regular)  return 3;
    if (libre)    return 2;
    if (sensible) return 1;
    return 0;
}
```

```csharp
var g1 = new List<(string, string)> { ("S", "aS"), ("S", "b") };                       // S → aS | b
var g2 = new List<(string, string)> { ("S", "aSb"), ("S", "") };                       // S → aSb | ε
var g3 = new List<(string, string)> {                                                   // aⁿbⁿcⁿ
    ("S", "aSBC"), ("S", "aBC"), ("CB", "BC"), ("aB", "ab"),
    ("bB", "bb"), ("bC", "bc"), ("cC", "cc") };
var g4 = new List<(string, string)> { ("S", "aA"), ("aA", "b") };                      // aA → b se acorta

Console.WriteLine(TipoChomsky(g1));   // → 3
Console.WriteLine(TipoChomsky(g2));   // → 2
Console.WriteLine(TipoChomsky(g3));   // → 1
Console.WriteLine(TipoChomsky(g4));   // → 0
```

> ⚠️ **No confundir la gramática con el lenguaje.** El tipo de una *gramática* se ve en la forma de sus reglas; el tipo de un *lenguaje* es el de la gramática **más simple** que lo genera. Una gramática complicada puede generar un lenguaje que en realidad es regular.

---

## 4.2. Autómatas de pila

### 4.2.1. Definición y funcionamiento de un autómata de pila

Un **autómata de pila** (AP, *pushdown automaton*) es un autómata finito al que se le agrega una **pila**. Con ella puede **contar**: apila un símbolo por cada `a` que lee y desapila uno por cada `b`; si al final la pila quedó vacía, había igual número de ambas.

#### La pila

Una **pila** es una estructura de datos **LIFO** (*last in, first out*): el último elemento que entra es el primero que sale. Solo se puede acceder al elemento de **arriba** (el **tope**).

| Operación | Qué hace | En C# (`Stack<char>`) |
|---|---|---|
| **Apilar** (*push*) | Pone un elemento arriba | `pila.Push('A')` |
| **Desapilar** (*pop*) | Quita el elemento de arriba y lo devuelve | `pila.Pop()` |
| **Consultar el tope** (*peek*) | Ve el de arriba sin quitarlo | `pila.Peek()` |

```
   tope →  │ A │
           │ A │
fondo  →   │ $ │        el símbolo $ marca el fondo de la pila
           └───┘
```

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

El autómata **acepta** una cadena si, tras leerla completa, está en un **estado de aceptación**.

Una **configuración** describe la situación completa en un instante: **(estado, entrada que falta por leer, contenido de la pila)**. Cada transición lleva de una configuración a otra.

#### Determinismo

Los APs pueden ser **no deterministas** (varias transiciones posibles en una misma configuración, como en un AFN). A diferencia de lo que pasa con los autómatas finitos, aquí el no determinismo **sí aumenta el poder**: hay lenguajes libres de contexto (como los palíndromos) que solo reconoce un AP no determinista.

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

Computación de `aabb`:

```
(q0, aabb, $)  ──a──▶  (q0, abb, A$)  ──a──▶  (q0, bb, AA$)  ──b──▶  (q1, b, A$)
               ──b──▶  (q1, ε, $)  ──ε──▶  (q2, ε, $)        q2 ∈ F  →  ACEPTADA
```

Con `aab` queda `(q1, ε, A$)`: sobra una `A` en la pila, el tope no es `$` y no se puede pasar a q2: **rechazada**. Con `abab` el autómata llega a `(q1, ab, $)` y desde ahí no existe ninguna transición que lea otra `a`: **rechazada**.

#### El autómata de pila en C#

Cada transición es un registro; la pila se guarda como texto cuyo **primer carácter es el tope**. Como el AP puede ser no determinista, el simulador **explora todas las configuraciones alcanzables** y acepta si alguna llega a un estado final con toda la entrada leída.

```csharp
// Lee == null significa ε. Apila reemplaza al tope: su primer carácter queda arriba ("" = solo desapilar).
record Transicion(string Desde, char? Lee, char Tope, string Hasta, string Apila);

class AutomataDePila
{
    private readonly List<Transicion> _transiciones;
    public string Inicial { get; }
    public char Fondo { get; }
    public HashSet<string> Finales { get; }

    public AutomataDePila(IEnumerable<Transicion> transiciones, string inicial, char fondo, IEnumerable<string> finales)
    {
        _transiciones = transiciones.ToList();
        Inicial = inicial;
        Fondo = fondo;
        Finales = new HashSet<string>(finales);
    }

    public bool Acepta(string cadena, bool traza = false)
    {
        var visitadas  = new HashSet<(string, int, string)>();
        var pendientes = new Stack<(string Estado, int Pos, string Pila)>();
        pendientes.Push((Inicial, 0, Fondo.ToString()));

        while (pendientes.Count > 0)
        {
            var (estado, pos, pila) = pendientes.Pop();

            if (!visitadas.Add((estado, pos, pila))) continue;      // configuración ya explorada
            if (pila.Length > 2 * cadena.Length + 4) continue;      // protección contra bucles de transiciones ε

            if (traza)
            {
                string resto = cadena.Substring(pos);
                Console.WriteLine($"  ({estado}, {(resto == "" ? "ε" : resto)}, {pila})");
            }

            if (pos == cadena.Length && Finales.Contains(estado)) return true;
            if (pila.Length == 0) continue;

            char tope = pila[0];

            foreach (var t in _transiciones)
            {
                if (t.Desde != estado || t.Tope != tope) continue;

                int nuevaPos = pos;
                if (t.Lee.HasValue)
                {
                    if (pos >= cadena.Length || cadena[pos] != t.Lee.Value) continue;
                    nuevaPos++;
                }

                pendientes.Push((t.Hasta, nuevaPos, t.Apila + pila.Substring(1)));
            }
        }

        return false;
    }
}
```

El autómata de `aⁿbⁿ`:

```csharp
var anbnAP = new AutomataDePila(
    new List<Transicion>
    {
        new("q0", null, '$', "q2", "$"),     // 1
        new("q1", null, '$', "q2", "$"),     // 2
        new("q0", 'a', '$', "q0", "A$"),     // 3  apilar A
        new("q0", 'a', 'A', "q0", "AA"),     // 4
        new("q0", 'b', 'A', "q1", ""),       // 5  desapilar
        new("q1", 'b', 'A', "q1", ""),       // 6
    },
    inicial: "q0", fondo: '$', finales: new[] { "q2" });

foreach (string w in new[] { "", "ab", "aabb", "aaabbb", "aab", "abab", "ba" })
    Console.WriteLine($"{(w == "" ? "ε" : w),-7} → {anbnAP.Acepta(w)}");

// → ε       → True
// → ab      → True
// → aabb    → True
// → aaabbb  → True
// → aab     → False
// → abab    → False
// → ba      → False

anbnAP.Acepta("aabb", traza: true);
// →   (q0, aabb, $)
// →   (q0, abb, A$)
// →   (q0, bb, AA$)
// →   (q1, b, A$)
// →   (q1, ε, $)
// →   (q2, ε, $)
```

#### Ejemplo: paréntesis balanceados

La misma idea: apilar con `(`, desapilar con `)`. Si se intenta desapilar con el fondo `$` a la vista (un `)` de más), no hay transición y la cadena se rechaza.

```csharp
var parentesisAP = new AutomataDePila(
    new List<Transicion>
    {
        new("q0", null, '$', "q1", "$"),     // el fondo está a la vista: todo quedó emparejado
        new("q0", '(', '$', "q0", "X$"),
        new("q0", '(', 'X', "q0", "XX"),
        new("q0", ')', 'X', "q0", ""),
    },
    inicial: "q0", fondo: '$', finales: new[] { "q1" });

foreach (string w in new[] { "", "()", "(())", "(()())", "(", ")(", "(()", "())" })
    Console.WriteLine($"{(w == "" ? "ε" : w),-7} → {parentesisAP.Acepta(w)}");

// → ε       → True
// → ()      → True
// → (())    → True
// → (()())  → True
// → (       → False
// → )(      → False
// → (()     → False
// → ())     → False
```

#### Ejemplo: palíndromos (no determinista)

Para reconocer `w wᴿ` (una cadena seguida de su reverso) hay que apilar la primera mitad y **comparar** con la segunda. El problema: el autómata **no sabe dónde está el centro**. La solución es **adivinarlo**: en cada posición tiene la opción de "empezar a comparar aquí". Si alguna apuesta acierta, la cadena se acepta.

```csharp
var t = new List<Transicion>();

foreach (char s in "ab")
    foreach (char tope in "ab$")
    {
        t.Add(new("q0", s, tope, "q0", $"{s}{tope}"));    // apilar s sobre el tope
        t.Add(new("q0", s, tope, "q1", $"{tope}"));       // s es el símbolo central (longitud impar)
    }

foreach (char tope in "ab$")
    t.Add(new("q0", null, tope, "q1", $"{tope}"));         // el centro está entre dos símbolos (longitud par)

foreach (char s in "ab")
    t.Add(new("q1", s, s, "q1", ""));                      // comparar con el tope y desapilar

t.Add(new("q1", null, '$', "q2", "$"));                    // todo coincidió: aceptar

var palindromosAP = new AutomataDePila(t, "q0", '$', new[] { "q2" });

foreach (string w in new[] { "", "a", "abba", "aba", "aabaa", "ab", "abab" })
    Console.WriteLine($"{(w == "" ? "ε" : w),-7} → {palindromosAP.Acepta(w)}");

// → ε       → True
// → a       → True
// → abba    → True
// → aba     → True
// → aabaa   → True
// → ab      → False
// → abab    → False
```

### 4.2.2. Relación entre autómata de pila y lenguaje libre de contexto

> **Teorema.** Un lenguaje es **libre de contexto** si y solo si existe un **autómata de pila** que lo acepta.

Es el equivalente, para el tipo 2, de lo que el teorema de Kleene decía para el tipo 3:

| Tipo | Se *genera* con… | Se *reconoce* con… |
|---|---|---|
| 3 (regular) | Gramática regular / expresión regular | Autómata **finito** |
| 2 (libre de contexto) | Gramática libre de contexto | Autómata **de pila** |

#### De gramática a autómata de pila

La construcción es sorprendentemente simple: el autómata **simula la derivación por la izquierda** usando la pila como forma sentencial.

| Situación (tope de la pila) | Transición |
|---|---|
| Al inicio | Apilar el símbolo inicial: `ε, $ → S$` |
| Tope = **no terminal** `A`, para cada producción `A → α` | Reemplazarlo por `α`, **sin leer entrada**: `ε, A → α` |
| Tope = **terminal** `a` | Si coincide con el símbolo de entrada, **desapilarlo**: `a, a → ε` |
| Fondo `$` a la vista | Aceptar: `ε, $ → $` |

Es decir: la pila **conserva lo que falta por producir**. Cada vez que arriba hay un no terminal, se "adivina" qué producción aplicar; cada vez que arriba hay un terminal, se **verifica** contra la entrada. Esto vuelve a ser no determinista: en `S → aSb | ε` hay que elegir entre dos reglas.

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

#### La construcción en C#

Se agrega este método a la clase `AutomataDePila`; recibe cualquier `Gramatica` y devuelve el autómata equivalente:

```csharp
public static AutomataDePila DesdeGramatica(Gramatica g)
{
    var t = new List<Transicion>
    {
        new("q0", null, '$', "q1", g.Inicial + "$"),              // apilar el símbolo inicial
    };

    foreach (var par in g.Producciones)
        foreach (string alfa in par.Value)
            t.Add(new("q1", null, par.Key, "q1", alfa));          // A → α: reemplazar el tope

    foreach (char a in g.Terminales)
        t.Add(new("q1", a, a, "q1", ""));                          // terminal en el tope: verificar y desapilar

    t.Add(new("q1", null, '$', "q2", "$"));                        // fondo a la vista: aceptar

    return new AutomataDePila(t, "q0", '$', new[] { "q2" });
}
```

Se comprueba que el autómata y la gramática describen el **mismo lenguaje** (hasta longitud 8), usando `Equivalentes` de la Unidad 3:

```csharp
var apDeAnbn = AutomataDePila.DesdeGramatica(anbn);
var lenguajeAnbn = anbn.Genera(8);

Console.WriteLine(apDeAnbn.Acepta("aabb"));    // → True
Console.WriteLine(apDeAnbn.Acepta("aab"));     // → False
Console.WriteLine(Equivalentes(w => apDeAnbn.Acepta(w), w => lenguajeAnbn.Contains(w), "ab", 8));      // → True

var apDePalindromos = AutomataDePila.DesdeGramatica(palindromos);
var lenguajePalindromos = palindromos.Genera(8);

Console.WriteLine(Equivalentes(w => apDePalindromos.Acepta(w), w => lenguajePalindromos.Contains(w), "ab", 8));   // → True
```

#### De autómata de pila a gramática

El sentido contrario existe (todo AP puede convertirse en una GLC), pero la construcción es larga: los no terminales de la gramática representan "ir del estado `p` al estado `q` dejando la pila como estaba". Para el curso basta con saber que **el teorema asegura que siempre es posible**.

#### Lo que un autómata de pila no puede hacer

Con **una sola pila** solo se puede comparar **dos** cantidades encadenadas. Por eso `aⁿbⁿ` sí se puede, pero `aⁿbⁿcⁿ` **no**: al contar las `b` contra las `a` la pila se vacía, y ya no queda con qué comparar las `c`. Para ese lenguaje se necesita una máquina con más poder (tipo 1 de la jerarquía).
