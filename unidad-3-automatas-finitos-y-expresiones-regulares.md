# Unidad 3. Autómatas finitos y expresiones regulares

Un **autómata finito** es la máquina más simple que reconoce un lenguaje: lee una cadena **símbolo por símbolo**, cambia de estado con cada uno y, al terminar, **acepta** o **rechaza**. Solo recuerda "en qué estado está"; no tiene más memoria.

Visto con lo aprendido en la Unidad 1, un autómata finito es un **grafo dirigido etiquetado** con tres añadidos: un nodo marcado como **inicial**, uno o más nodos marcados como **de aceptación** y una regla de lectura de la cadena. Esto es exactamente lo que hacen los analizadores léxicos de un compilador al reconocer palabras reservadas, números e identificadores.

La unidad cierra con las **expresiones regulares**, otra forma —textual— de describir los mismos lenguajes.

> Los ejemplos de esta unidad usan las clases `AFD` y `AFN` que se construyen paso a paso en 3.1 y 3.2. Las clases se declaran **después** de las instrucciones de nivel superior del archivo.

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
| **Σ\*** | Todas las cadenas posibles sobre Σ (incluida ε) | {0,1}\* = {ε, 0, 1, 00, 01, 10, 11, 000, …} |
| **Lenguaje** | Cualquier subconjunto de Σ\* | L = {cadenas con número par de 1s} |

Un autómata es la forma de **decidir si una cadena pertenece a un lenguaje**.

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

El **lenguaje del autómata**, L(M), es el conjunto de todas las cadenas que M acepta:

> **L(M) = { w ∈ Σ\* : al leer w desde q₀, M termina en un estado de F }**

> ⚠️ La cadena vacía ε se acepta **si y solo si el estado inicial es de aceptación** (no se lee ningún símbolo, así que el autómata se queda en `q₀`).

#### Lenguajes regulares

Un lenguaje es **regular** si existe un AFD que lo acepta. Más adelante (3.3) se verá que son exactamente los lenguajes que describen las expresiones regulares.

#### El AFD en C#

La tabla de transición se guarda como un diccionario: **llave** = (estado, símbolo), **valor** = estado siguiente.

```csharp
class AFD
{
    public List<string> Estados { get; }
    public HashSet<char> Alfabeto { get; }
    public string Inicial { get; }
    public HashSet<string> Finales { get; }
    private readonly Dictionary<(string, char), string> _delta;

    public AFD(IEnumerable<string> estados, IEnumerable<char> alfabeto,
               Dictionary<(string, char), string> delta,
               string inicial, IEnumerable<string> finales)
    {
        Estados  = estados.ToList();
        Alfabeto = new HashSet<char>(alfabeto);
        _delta   = delta;
        Inicial  = inicial;
        Finales  = new HashSet<string>(finales);
    }

    public bool Acepta(string cadena, bool traza = false)
    {
        string actual = Inicial;

        foreach (char simbolo in cadena)
        {
            if (!_delta.TryGetValue((actual, simbolo), out string? siguiente))
                return false;   // transición no definida: se rechaza

            if (traza) Console.WriteLine($"  δ({actual}, {simbolo}) = {siguiente}");
            actual = siguiente;
        }

        return Finales.Contains(actual);
    }

    public void Imprimir()
    {
        foreach (string q in Estados)
        {
            string marca = (q == Inicial ? "→" : " ") + (Finales.Contains(q) ? "*" : " ");
            string fila = string.Join("   ", Alfabeto.Select(c => $"{c}:{_delta.GetValueOrDefault((q, c), "—")}"));
            Console.WriteLine($"{marca} {q,-10} {fila}");
        }
    }
}
```

Y el autómata de los 1s pares:

```csharp
var pares = new AFD(
    estados:  new[] { "q0", "q1" },
    alfabeto: "01",
    delta: new()
    {
        [("q0", '0')] = "q0",   [("q0", '1')] = "q1",
        [("q1", '0')] = "q1",   [("q1", '1')] = "q0",
    },
    inicial: "q0",
    finales: new[] { "q0" });

Console.WriteLine(pares.Acepta("1001"));   // → True
Console.WriteLine(pares.Acepta("1101"));   // → False
Console.WriteLine(pares.Acepta(""));       // → True   (ε: q0 es de aceptación)
Console.WriteLine(pares.Acepta("0000"));   // → True   (cero 1s)

pares.Acepta("1101", traza: true);
// →   δ(q0, 1) = q1
// →   δ(q1, 1) = q0
// →   δ(q0, 0) = q0
// →   δ(q0, 1) = q1
```

### 3.1.2. Construcción de AFD a partir de casos prácticos

Diseñar un AFD es **decidir qué hay que recordar** de lo que ya se leyó. Cada cosa distinta que se necesite recordar es un estado.

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

```csharp
var terminaEnAb = new AFD(
    estados:  new[] { "q0", "q1", "q2" },
    alfabeto: "ab",
    delta: new()
    {
        [("q0", 'a')] = "q1",   [("q0", 'b')] = "q0",
        [("q1", 'a')] = "q1",   [("q1", 'b')] = "q2",
        [("q2", 'a')] = "q1",   [("q2", 'b')] = "q0",
    },
    inicial: "q0",
    finales: new[] { "q2" });

foreach (string w in new[] { "ab", "aab", "abb", "bab", "b", "" })
    Console.WriteLine($"{(w == "" ? "ε" : w),-4} → {terminaEnAb.Acepta(w)}");

// → ab   → True
// → aab  → True
// → abb  → False
// → bab  → True
// → b    → False
// → ε    → False
```

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

```csharp
var empiezaCon01 = new AFD(
    estados:  new[] { "q0", "q1", "q2", "trampa" },
    alfabeto: "01",
    delta: new()
    {
        [("q0", '0')] = "q1",      [("q0", '1')] = "trampa",
        [("q1", '0')] = "trampa",  [("q1", '1')] = "q2",
        [("q2", '0')] = "q2",      [("q2", '1')] = "q2",
        [("trampa", '0')] = "trampa", [("trampa", '1')] = "trampa",
    },
    inicial: "q0",
    finales: new[] { "q2" });

Console.WriteLine(empiezaCon01.Acepta("01"));      // → True
Console.WriteLine(empiezaCon01.Acepta("0111"));    // → True
Console.WriteLine(empiezaCon01.Acepta("10"));      // → False
Console.WriteLine(empiezaCon01.Acepta("00"));      // → False
```

> ⚠️ **Un AFD debe tener una transición por cada símbolo en cada estado.** Si falta alguna, la función δ está incompleta. La clase `AFD` de arriba rechaza la cadena cuando no encuentra la transición, que equivale a mandarla a un estado trampa implícito; en un examen o diagrama formal, la trampa se dibuja.

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

```csharp
char Clasificar(char c) => char.IsLetter(c) || c == '_' ? 'L' : char.IsDigit(c) ? 'D' : 'O';
string Simbolos(string texto) => new string(texto.Select(Clasificar).ToArray());

var identificador = new AFD(
    estados:  new[] { "inicio", "ident", "error" },
    alfabeto: "LDO",
    delta: new()
    {
        [("inicio", 'L')] = "ident",  [("inicio", 'D')] = "error",  [("inicio", 'O')] = "error",
        [("ident",  'L')] = "ident",  [("ident",  'D')] = "ident",  [("ident",  'O')] = "error",
        [("error",  'L')] = "error",  [("error",  'D')] = "error",  [("error",  'O')] = "error",
    },
    inicial: "inicio",
    finales: new[] { "ident" });

foreach (string texto in new[] { "total", "x1", "_dato", "1x", "a-b" })
{
    bool valido = identificador.Acepta(Simbolos(texto));
    Console.WriteLine($"{texto,-6} ({Simbolos(texto),-5}) → {(valido ? "identificador válido" : "no válido")}");
}

// → total  (LLLLL) → identificador válido
// → x1     (LD   ) → identificador válido
// → _dato  (LLLLL) → identificador válido
// → 1x     (DL   ) → no válido
// → a-b    (LOL  ) → no válido
```

---

## 3.2. Autómata Finito No Determinista (AFN)

### 3.2.1. Definición y diferencias frente al AFD

Un **AFN** tiene la misma estructura que un AFD, pero la función de transición **puede ofrecer varias opciones** (o ninguna) para un mismo estado y símbolo:

> **N = (Q, Σ, δ, q₀, F)**, con **δ: Q × Σ → P(Q)**

`P(Q)` es el conjunto de **subconjuntos** de Q: `δ(q, a)` devuelve un **conjunto de estados** (que puede estar vacío).

| Característica | AFD | AFN |
|---|---|---|
| Transiciones por (estado, símbolo) | **Exactamente una** | **Cero, una o varias** |
| Camino para una cadena | Único | Puede haber varios (o ninguno) |
| Cuándo acepta | El camino único termina en un estado de `F` | **Al menos un** camino termina en un estado de `F` |
| Estado trampa | Necesario para completar δ | No hace falta: si no hay transición, ese camino simplemente muere |
| Diagrama | Más grande, lleno de flechas | Normalmente más pequeño y fácil de leer |

> Todo AFD es, en particular, un AFN (con un solo destino por transición). Lo sorprendente —y lo que se prueba en 3.2.2— es lo contrario: **todo AFN se puede convertir en un AFD equivalente**. El no determinismo hace más cómodo el diseño, pero **no reconoce más lenguajes**.

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

#### El AFN en C#

La simulación consiste en mantener el **conjunto** de estados actuales y reemplazarlo, símbolo a símbolo, por la unión de los destinos:

```csharp
class AFN
{
    public List<string> Estados { get; }
    public HashSet<char> Alfabeto { get; }
    public string Inicial { get; }
    public HashSet<string> Finales { get; }
    private readonly Dictionary<(string, char), HashSet<string>> _delta;

    public AFN(IEnumerable<string> estados, IEnumerable<char> alfabeto,
               Dictionary<(string, char), HashSet<string>> delta,
               string inicial, IEnumerable<string> finales)
    {
        Estados  = estados.ToList();
        Alfabeto = new HashSet<char>(alfabeto);
        _delta   = delta;
        Inicial  = inicial;
        Finales  = new HashSet<string>(finales);
    }

    public static string Nombre(IEnumerable<string> estados) =>
        "{" + string.Join(", ", estados.OrderBy(e => e, StringComparer.Ordinal)) + "}";

    public bool Acepta(string cadena, bool traza = false)
    {
        var actuales = new HashSet<string> { Inicial };

        foreach (char simbolo in cadena)
        {
            var siguientes = new HashSet<string>();

            foreach (string q in actuales)
                if (_delta.TryGetValue((q, simbolo), out var destinos))
                    siguientes.UnionWith(destinos);

            actuales = siguientes;
            if (traza) Console.WriteLine($"  {simbolo} → {Nombre(actuales)}");
        }

        return actuales.Overlaps(Finales);    // ¿hay al menos un estado de aceptación?
    }
}
```

```csharp
var afn = new AFN(
    estados:  new[] { "q0", "q1", "q2" },
    alfabeto: "ab",
    delta: new()
    {
        [("q0", 'a')] = new() { "q0", "q1" },
        [("q0", 'b')] = new() { "q0" },
        [("q1", 'b')] = new() { "q2" },
    },
    inicial: "q0",
    finales: new[] { "q2" });

Console.WriteLine(afn.Acepta("aab"));    // → True
Console.WriteLine(afn.Acepta("aba"));    // → False

afn.Acepta("aab", traza: true);
// →   a → {q0, q1}
// →   a → {q0, q1}
// →   b → {q0, q2}
```

Los pares (estado, símbolo) que no aparecen en el diccionario equivalen a `∅`: ese camino muere.

### 3.2.2. Conversión de AFN a AFD

Si se sigue el conjunto de estados posibles (como en la tabla anterior), **cada conjunto puede verse como un único estado de un AFD**. Esa es la idea de la **construcción de subconjuntos**: el AFD resultante tiene como estados los **subconjuntos** de estados del AFN.

#### Algoritmo

| Paso | Qué hacer |
|---|---|
| 1 | El estado inicial del AFD es el conjunto `{q₀}` |
| 2 | Para cada conjunto `S` pendiente y cada símbolo `a`: `δ_D(S, a) = ⋃ δ_N(q, a)` para todo `q ∈ S` |
| 3 | Si el conjunto obtenido es nuevo, se agrega a los pendientes |
| 4 | Un conjunto es **de aceptación** si **contiene al menos un estado de aceptación** del AFN |
| 5 | Se termina cuando no quedan conjuntos pendientes |

Con `n` estados en el AFN, el AFD tiene **a lo más 2ⁿ** estados (los subconjuntos posibles), aunque en la práctica casi siempre son bastantes menos, porque solo se construyen los que son **alcanzables** (idea de la Unidad 1).

#### Ejemplo: el AFN de `terminan en ab`

| Conjunto | a | b |
|---|---|---|
| → {q0} | δ(q0,a) = **{q0, q1}** | δ(q0,b) = **{q0}** |
| {q0, q1} | δ(q0,a) ∪ δ(q1,a) = {q0, q1} ∪ ∅ = **{q0, q1}** | δ(q0,b) ∪ δ(q1,b) = {q0} ∪ {q2} = **{q0, q2}** |
| \* {q0, q2} | δ(q0,a) ∪ δ(q2,a) = {q0, q1} ∪ ∅ = **{q0, q1}** | δ(q0,b) ∪ δ(q2,b) = {q0} ∪ ∅ = **{q0}** |

`{q0, q2}` es de aceptación porque contiene a q2. Aparecen solo **tres** conjuntos de los 8 posibles, y si se renombran `{q0} = A`, `{q0,q1} = B` y `{q0,q2} = C`, la tabla es **idéntica a la del AFD diseñado a mano en el caso 1**.

#### La conversión en C#

Se agrega este método a la clase `AFN`:

```csharp
public AFD ConvertirAAFD()
{
    var estados    = new List<string>();
    var finales    = new List<string>();
    var delta      = new Dictionary<(string, char), string>();
    var pendientes = new Queue<HashSet<string>>();

    var inicial = new HashSet<string> { Inicial };
    estados.Add(Nombre(inicial));
    pendientes.Enqueue(inicial);

    while (pendientes.Count > 0)
    {
        var conjunto = pendientes.Dequeue();
        string nombre = Nombre(conjunto);

        if (conjunto.Overlaps(Finales))                // basta un estado de aceptación
            finales.Add(nombre);

        foreach (char simbolo in Alfabeto)
        {
            var destino = new HashSet<string>();
            foreach (string q in conjunto)
                if (_delta.TryGetValue((q, simbolo), out var d))
                    destino.UnionWith(d);

            string nombreDestino = Nombre(destino);
            delta[(nombre, simbolo)] = nombreDestino;

            if (!estados.Contains(nombreDestino))      // conjunto nuevo
            {
                estados.Add(nombreDestino);
                pendientes.Enqueue(destino);
            }
        }
    }

    return new AFD(estados, Alfabeto, delta, Nombre(inicial), finales);
}
```

```csharp
AFD convertido = afn.ConvertirAAFD();
convertido.Imprimir();

// → →  {q0}       a:{q0, q1}   b:{q0}
// →    {q0, q1}   a:{q0, q1}   b:{q0, q2}
// →  * {q0, q2}   a:{q0, q1}   b:{q0}

Console.WriteLine(convertido.Acepta("aab"));   // → True
Console.WriteLine(convertido.Acepta("aba"));   // → False
```

Si en algún paso el conjunto destino queda vacío (`{}`), ese conjunto es justamente el **estado trampa** del AFD: es el que completa la función δ de lo que en el AFN eran caminos que morían.

> ⚠️ Un error frecuente al hacer la conversión a mano: olvidar que el estado de aceptación del AFD es **todo conjunto que contenga** un estado de aceptación del AFN, no solo el conjunto formado únicamente por él.

---

## 3.3. Expresiones regulares

### 3.3.1. Sintaxis y semántica de expresiones regulares

Una **expresión regular** (ER) es una **fórmula** que describe un lenguaje usando tres operaciones. Es una forma textual y compacta de lo mismo que dibuja un autómata.

#### Las tres operaciones básicas

| Operación | Se escribe | Significado | Ejemplo | Lenguaje |
|---|---|---|---|---|
| **Unión** | `R\|S` | Una cadena de R **o** una de S | `a\|b` | {a, b} |
| **Concatenación** | `RS` | Una cadena de R **seguida** de una de S | `ab` | {ab} |
| **Cerradura de Kleene** | `R*` | **Cero o más** repeticiones de R | `a*` | {ε, a, aa, aaa, …} |

> En los textos de teoría la unión se escribe `R + S` o `R ∪ S`. En este material (y en C#) se escribe `R|S`, para no confundirla con el `+` de "una o más".

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

#### Precedencia

De mayor a menor: **`*`** → **concatenación** → **`|`**. Los paréntesis cambian el orden.

| Expresión | Se lee como | Lenguaje |
|---|---|---|
| `ab*` | `a(b*)` | `a` seguida de cero o más `b`: a, ab, abb, … |
| `(ab)*` | | ε, ab, abab, ababab, … |
| `a\|bc` | `a\|(bc)` | {a, bc} |
| `(a\|b)c` | | {ac, bc} |

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

#### Expresiones regulares en C#

El espacio de nombres `System.Text.RegularExpressions` ofrece la clase `Regex`.

```csharp
using System.Text.RegularExpressions;

var terminaEnAbRegex = new Regex("^(a|b)*ab$");

foreach (string w in new[] { "ab", "aab", "abb", "bab", "" })
    Console.WriteLine($"{(w == "" ? "ε" : w),-4} → {terminaEnAbRegex.IsMatch(w)}");

// → ab   → True
// → aab  → True
// → abb  → False
// → bab  → True
// → ε    → False
```

> ⚠️ **`^` y `$` son indispensables.** `Regex.IsMatch` busca la expresión en **cualquier parte** de la cadena; el lenguaje de una ER formal es el de las cadenas que coinciden **completas**. Sin `^…$`, `new Regex("ab").IsMatch("xxabxx")` da `True`, y eso no es lo que pide la teoría.

> ⚠️ **No todo lo que acepta `Regex` en .NET es regular.** Funciones como las referencias hacia atrás (`\1`) permiten reconocer lenguajes que **ningún** autómata finito acepta. En esta materia se usa solo el subconjunto formal: unión, concatenación y cerradura (y sus atajos).

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

#### Comprobación en C#

Una ER y un autómata son equivalentes si aceptan **las mismas cadenas**. No se puede comprobar con todas las cadenas (son infinitas), pero sí con todas las de longitud hasta un límite. Esto **no es una demostración**, pero detecta de inmediato los errores.

```csharp
IEnumerable<string> Cadenas(string alfabeto, int largo)
{
    if (largo == 0) { yield return ""; yield break; }

    foreach (string prefijo in Cadenas(alfabeto, largo - 1))
        foreach (char c in alfabeto)
            yield return prefijo + c;
}

bool Equivalentes(Func<string, bool> f, Func<string, bool> g, string alfabeto, int maxLargo)
{
    for (int largo = 0; largo <= maxLargo; largo++)
        foreach (string w in Cadenas(alfabeto, largo))
            if (f(w) != g(w))
            {
                Console.WriteLine($"Difieren en: \"{w}\"");
                return false;
            }
    return true;
}
```

```csharp
// Usa `pares` (3.1.1) y `afn` (3.2.1), declarados antes en el mismo archivo

// El AFD de 3.1.1 contra la ER obtenida por eliminación de estados
var er = new Regex("^(0|10*1)*$");
Console.WriteLine(Equivalentes(w => pares.Acepta(w), w => er.IsMatch(w), "01", 10));   // → True

// El AFN de 3.2.1 contra la ER (a|b)*ab
var er2 = new Regex("^(a|b)*ab$");
Console.WriteLine(Equivalentes(w => afn.Acepta(w), w => er2.IsMatch(w), "ab", 10));    // → True

// Una ER incorrecta: acepta TODAS las cadenas binarias
var mala = new Regex("^(0|1)*$");
Console.WriteLine(Equivalentes(w => pares.Acepta(w), w => mala.IsMatch(w), "01", 10)); // → Difieren en: "1"
                                                                                       // → False
```
