import java.util.ArrayList;

public class Nodo {

    private String id = "";
    private ArrayList<Nodo> vecinos = new ArrayList<Nodo>();   // nodos a los que apunta este nodo (aristas dirigidas)

    public Nodo(String id) {
        this.id = id;
    }

    public void apuntarA(Nodo destino) {
        vecinos.add(destino);
    }

    public static void main(String[] args) {
        Nodo n1 = new Nodo("1");
        Nodo n2 = new Nodo("2");
        Nodo n3 = new Nodo("3");

        n1.apuntarA(n2);   // 1 → 2
        n2.apuntarA(n3);   // 2 → 3
        n3.apuntarA(n1);   // 3 → 1  (cierra el ciclo)

        Nodo[] nodos = { n1, n2, n3 };

        for (Nodo nodo : nodos) {
            for (Nodo vecino : nodo.vecinos) {
                System.out.println(nodo.id + " -> " + vecino.id);
            }
        }
        // → 1 -> 2
        // → 2 -> 3
        // → 3 -> 1
    }
}
