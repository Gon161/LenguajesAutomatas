# Lenguaje y Autómatas — Material del curso

Material de estudio del curso, organizado por unidades. Todos los ejemplos están escritos en **Java**, con programación orientada a objetos: los grafos, árboles, autómatas y pilas se construyen como **estructuras enlazadas** (objetos que guardan referencias a otros objetos), y pueden ejecutarse como programas de consola.

## Contenido

| Unidad | Tema | Archivo |
|---|---|---|
| 1 | Fundamentos de grafos | [unidad-1-fundamentos-de-grafos.md](unidad-1-fundamentos-de-grafos.md) |
| 2 | Fundamentos de árboles | [unidad-2-fundamentos-de-arboles.md](unidad-2-fundamentos-de-arboles.md) |
| 3 | Autómatas finitos y expresiones regulares | [unidad-3-automatas-finitos-y-expresiones-regulares.md](unidad-3-automatas-finitos-y-expresiones-regulares.md) |
| 4 | Gramáticas formales y autómatas de pila | [unidad-4-gramaticas-formales-y-automatas-de-pila.md](unidad-4-gramaticas-formales-y-automatas-de-pila.md) |

El temario completo con todos los subtemas está en [lenguaje-automatas-completo.md](lenguaje-automatas-completo.md).

Las clases se van construyendo a lo largo del curso y se reutilizan de una unidad a otra:

| Clase | Unidad | Qué representa |
|---|---|---|
| `Nodo`, `Grafo` | 1 | Grafo como lista de nodos enlazados |
| `Estado`, `Transicion` | 1 | Diagrama de transición: estados enlazados por flechas etiquetadas |
| `NodoArbol` | 2 | Árbol con raíz y árbol de derivación |
| `Reconocedor`, `AFD`, `AFN`, `Equivalencia` | 3 | Autómatas finitos y comparación con expresiones regulares |
| `Gramatica`, `NodoPila`, `TransicionPila`, `Configuracion`, `AutomataDePila` | 4 | Gramáticas libres de contexto y autómata de pila con pila enlazada |

## Estructura del repositorio

```
LenguajeAutomatas/
├── README.md                                                   ← este archivo
├── lenguaje-automatas-completo.md                              ← temario completo e índice
├── unidad-1-fundamentos-de-grafos.md
├── unidad-2-fundamentos-de-arboles.md
├── unidad-3-automatas-finitos-y-expresiones-regulares.md
├── unidad-4-gramaticas-formales-y-automatas-de-pila.md
├── ejemplos/                                                   ← ejemplos ejecutables (Nodo.java)
└── practica/                                                   ← aquí van tus prácticas
```

## Cómo usar este material

1. Lee la unidad en orden: cada tema se apoya en el anterior. Un autómata es un grafo, un árbol de derivación es un árbol y una gramática es la regla que lo construye.
2. Dibuja a mano cada ejemplo antes de programarlo; en esta materia el diagrama va primero y el código después.
3. Copia los ejemplos en archivos `.java` dentro de `practica/` y ejecútalos; modifícalos para ver qué cambia.

### Compilar y ejecutar para practicar

Cada clase pública va en su propio archivo con el mismo nombre (`Grafo.java`, `Estado.java`, …). Los fragmentos de uso se escriben dentro del `main` de una clase de prueba:

```java
public class Unidad1 {
    public static void main(String[] args) {
        Grafo g = new Grafo(true);
        g.agregarArista("A", "B");
        System.out.println(g.alcanzable("A", "B"));   // → true
    }
}
```

```bash
cd practica
javac -encoding UTF-8 *.java
java Unidad1
```

> Requiere tener instalado un **JDK 8 o superior** (IntelliJ IDEA y NetBeans ya traen el suyo). Si en la consola de Windows los símbolos `ε`, `δ` o `⇒` aparecen como `?`, ejecuta `chcp 65001` antes de correr el programa.

## Convenciones

- Las clases siguen el estilo de `ejemplos/Nodo.java`: atributos `private` inicializados en la declaración (`private ArrayList<Nodo> vecinos = new ArrayList<Nodo>();`) y métodos `public` para usarlos.
- Las colecciones son de `java.util`: `ArrayList` para listas, `LinkedList` cuando se usa como cola o como pila, `HashSet` y `HashMap` para conjuntos y tablas.
- Cuando un fragmento dice "se agrega a la clase X", el método va dentro de esa clase; los fragmentos de uso van dentro de un `main` (o de un método `static` de la clase de prueba).
- Los comentarios `// →` indican lo que imprime la línea en consola.
- Las notas que empiezan con **Ojo con…** señalan errores comunes y matices.
- En los diagramas de texto, `(q0)` es un estado, `((q1))` es un estado de aceptación y `──a──▶` es una transición con el símbolo `a`.
