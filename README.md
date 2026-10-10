# Lenguaje y Autómatas — Material del curso

Material de estudio del curso, organizado por unidades. Todos los ejemplos están escritos en **C#** (.NET) y pueden ejecutarse como aplicaciones de consola.

## Contenido

| Unidad | Tema | Archivo |
|---|---|---|
| 1 | Fundamentos de grafos | [unidad-1-fundamentos-de-grafos.md](unidad-1-fundamentos-de-grafos.md) |
| 2 | Fundamentos de árboles | [unidad-2-fundamentos-de-arboles.md](unidad-2-fundamentos-de-arboles.md) |
| 3 | Autómatas finitos y expresiones regulares | [unidad-3-automatas-finitos-y-expresiones-regulares.md](unidad-3-automatas-finitos-y-expresiones-regulares.md) |
| 4 | Gramáticas formales y autómatas de pila | [unidad-4-gramaticas-formales-y-automatas-de-pila.md](unidad-4-gramaticas-formales-y-automatas-de-pila.md) |

El temario completo con todos los subtemas está en [lenguaje-automatas-completo.md](lenguaje-automatas-completo.md).

## Estructura del repositorio

```
LenguajeAutomatas/
├── README.md                                                   ← este archivo
├── lenguaje-automatas-completo.md                              ← temario completo e índice
├── unidad-1-fundamentos-de-grafos.md
├── unidad-2-fundamentos-de-arboles.md
├── unidad-3-automatas-finitos-y-expresiones-regulares.md
├── unidad-4-gramaticas-formales-y-automatas-de-pila.md
└── practica/                                                   ← aquí van tus prácticas
```

## Cómo usar este material

1. Lee la unidad en orden: cada tema se apoya en el anterior. Un autómata es un grafo, un árbol de derivación es un árbol y una gramática es la regla que lo construye.
2. Dibuja a mano cada ejemplo antes de programarlo; en esta materia el diagrama va primero y el código después.
3. Copia los ejemplos en un proyecto de consola dentro de `practica/` y ejecútalos; modifícalos para ver qué cambia.

### Crear un proyecto de consola para practicar

```bash
cd practica
dotnet new console -n Unidad1
cd Unidad1
dotnet run
```

> Requiere tener instalado el [SDK de .NET](https://dotnet.microsoft.com/download).

## Convenciones

- El código usa *top-level statements* (C# 9+), así que no siempre verás `class Program` ni `static void Main`.
- Cuando un ejemplo define clases o métodos propios, esas definiciones van **después** de las instrucciones de nivel superior del mismo archivo.
- Los comentarios `// →` indican lo que imprime la línea en consola.
- Los bloques marcados con ⚠️ señalan errores comunes.
- En los diagramas de texto, `(q0)` es un estado, `((q1))` es un estado de aceptación y `──a──▶` es una transición con el símbolo `a`.
