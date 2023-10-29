public class Main {    
    public static void main(String[] args) {
        API proyecto1 = new API();
        Trigger trigger = Trigger.getInstance();

        Thread thread1 = new Thread(proyecto1);
        Thread thread2 = new Thread(trigger);

        // Iniciar los hilos
        thread1.start();
        thread2.start();
    }  
}
