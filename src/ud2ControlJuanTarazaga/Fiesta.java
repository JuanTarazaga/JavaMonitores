package ud2ControlJuanTarazaga;

import java.util.Random;

class Nene extends Thread {
    private Mesa mesa;
    private String nombre;

    public Nene(Mesa mesa, String nombre) {
        this.mesa = mesa;
        this.nombre = nombre;
    }

    // Aquí defino el método run() para que cada hilo de niño ejecute una secuencia
    // de acciones. Al llegar a la fiesta, el niño saluda, espera un tiempo aleatorio
    // antes de intentar comer la tarta, y finalmente se despide cuando consigue
    // su trozo de tarta.
    @Override
    public void run() {
        try {
            // Aquí el niño llega a la fiesta y debe saludar
            // antes de hacer algo más. El mensaje simula el saludo de llegada.
            System.out.println(nombre + " llega a la fiesta");
            mesa.saludarNene(nombre);

            // Ahora añado una pausa breve (200 ms) para simular que el niño
            // se toma un momento para socializar tras llegar.
            Thread.sleep(200);  

            // Luego el niño espera un tiempo aleatorio (de 0 a 500 ms) antes
            // de intentar comer tarta. Esto hace que la llegada y el comportamiento
            // de los niños no sean predecibles ni simultáneos.
            Thread.sleep(new Random().nextInt(500));  
            mesa.comer(nombre);

            // Aquí he hecho esto para que el niño se despida después de conseguir su tarta.
            // Esto simula que se marcha después de comer.
            System.out.println(nombre + " se marcha del cumpleaños");
            
            // Finalmente, añado una pequeña pausa antes de que el hilo termine
            // para que los mensajes de despedida se gestionen de forma ordenada.
            Thread.sleep(100);  
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Madre extends Thread {
    private Mesa mesa;

    public Madre(Mesa mesa) {
        this.mesa = mesa;
    }

    // Aquí defino el método run() para el hilo de la madre. Al llegar,
    // la madre saluda, luego entra en un ciclo donde repone tarta cuando
    // sea necesario, hasta que ya no haya más niños en la fiesta. Finalmente,
    // se despide cuando termina su labor.
    @Override
    public void run() {
        try {
            // Aquí he hecho esto para simular que la madre llega a la fiesta.
            // Es una pausa de 1 segundo para que todo fluya de manera natural.
            System.out.println("-->Mami llega al cumpleaños");
            Thread.sleep(1000);  // Espera 1 segundo al llegar

            // Luego la madre repone la tarta de manera sincronizada. Este ciclo
            // se repite mientras haya niños en la fiesta.
            while (mesa.hayNenesEnFiesta()) { 
                mesa.reponerTarta();
                // Ahorabhe añadido una pausa de 1 segundo antes de que la madre
                // intente reponer tarta de nuevo. Esto hace que el flujo sea más pausado
                // y se evite que la madre reponga demasiadas veces en un corto período.
                Thread.sleep(1000);  
            }

            // Luego, al final del ciclo, la madre se despide cuando ya no quedan niños.
            System.out.println("-->Mami se va del cumpleaños");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Mesa {
    private int porciones = 0;
    private int nenesEnFiesta;
    private final int MAX_PORCIONES = 5; 

    // Aquí he creado el constructor que inicializa la cantidad de niños en la fiesta
    // y establece el número inicial de porciones de tarta.
    public Mesa(int nenesEnFiesta) {
        this.nenesEnFiesta = nenesEnFiesta;
    }

    // Aquí he implementado el método 'saludarNene' que permite que un niño
    // salude cuando llega a la fiesta. Después de imprimir el mensaje, el hilo
    // hace una pausa de 200 ms para simular un tiempo de interacción social al llegar.
    public synchronized void saludarNene(String nombre) throws InterruptedException {
        System.out.println(nombre + " saluda al llegar.");
        Thread.sleep(200); 
    }

    // Aquí he hecho este método para que un niño pueda comer tarta cuando
    // hay porciones disponibles. Si no hay tarta, el niño espera hasta que
    // la madre reponga. Cada vez que un niño come, se reduce el número de
    // porciones y se notifica a los otros hilos para que actúen según corresponda.
    public synchronized void comer(String nombre) throws InterruptedException {
        while (porciones <= 0) { 
            wait(); 
        }

        // Aquí he hecho esto para que el niño coma una porción de tarta.
        porciones--; 
        System.out.println(nombre + " come tarta. Porciones restantes: " + porciones);
        Thread.sleep(300);

        // Luego, verifico si ya no queda tarta y si es el caso, aviso a la madre
        // para que reponga más tarta.
        if (porciones == 0) {
            System.out.println("--> Mamá, ya no hay tarta");
        }

        // Ahora, disminuimos el número de niños en la fiesta, ya que el niño
        // se ha marchado después de comer.
        nenesEnFiesta--; 
        notifyAll();
    }

    // Aquí he creado este método para que la madre reponga la tarta
    // cuando sea necesario, pero solo cuando no hay porciones disponibles.
    // La madre repone la tarta y luego notifica a los niños que la tarta está disponible.
    public synchronized void reponerTarta() throws InterruptedException {
        // Si todavía hay tarta, la madre espera a que se acabe antes de reponer.
        while (porciones > 0) { 
            wait(); 
        }

        // Ahora, la madre repone la tarta solo si quedan niños en la fiesta
        if (nenesEnFiesta > 0) {
            porciones = MAX_PORCIONES;
            System.out.println("--> Mamá pone tarta. Porciones ahora: " + porciones);
            Thread.sleep(500); 
            notifyAll(); 
        }
    }

    // Aquí he creado este método para verificar si todavía quedan niños en la fiesta.
    // Si no quedan, la madre sabe que debe terminar y que ya no es necesario reponer más tarta.
    public synchronized boolean hayNenesEnFiesta() {
        return nenesEnFiesta > 0;
    }
}

public class Fiesta {
    public static void main(String[] args) {
        System.out.println("Empieza el cumpleaños");

        // Aquí he inicializado el número de niños en la fiesta y creado el objeto Mesa
        // que compartirá la madre y los niños.
        int numeroNenes = 10;
        Mesa mesa = new Mesa(numeroNenes);

        // Ahora he creado y lanzado el hilo de la madre que repone la tarta.
        Madre madre = new Madre(mesa);
        madre.start();

        // Luego, he creado y lanzado hilos para cada uno de los 10 niños. Cada niño
        // llega a la fiesta, saluda, espera, come y se va.
        String[] nombres = {"Jaimito", "Luisito", "Juanito", "Jorgito", "Anita", "Luci", "Dani", "Mary", "Vicky", "Chloe"};
        for (String nombre : nombres) {
            new Nene(mesa, nombre).start();
        }

        // Finalmente, he hecho que el hilo principal espere a que la madre termine su trabajo
        // antes de finalizar el programa, asegurando que la fiesta termine correctamente.
        try {
            madre.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("¡¡ Fin del cumpleaños !!");
    }
}
