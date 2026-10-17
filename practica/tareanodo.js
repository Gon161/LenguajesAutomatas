const readline = require("readline");

const nodo1 = 1;
const nodo2 = 2;
const nodo3 = 3;

function recorrer(inicio) {
    let actual = inicio;
    do {
        console.log("Nodo: ", actual);
        if (actual === nodo1) {
            actual = nodo2;
        } else if (actual === nodo2) {
            actual = nodo3;
        } else {
            actual = nodo1;
        }
    } while (actual !== inicio);
}

const read = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
});

read.question("En que nodo quieres empezar?", (respuesta) => {
    const inicio = Number(respuesta);

    if (inicio === nodo1 || inicio === nodo2 || inicio === nodo3) {
        recorrer(inicio);
    } else {
        console.log("Nodo invalido");
    }
    read.close();
});