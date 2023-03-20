package tasks;

import gui.VistaCliente;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

import static java.lang.Thread.sleep;
import static javax.xml.ws.Endpoint.publish;

public class WorkerDescarga extends SwingWorker<Void, Integer> {

    private Socket socket;
    private boolean pausado;
    private int contador;
    private long fileSize;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private VistaCliente vistaCliente;
    private String ficheroSeleccionado;
    private File file;


    public WorkerDescarga(ObjectInputStream entrada, ObjectOutputStream salida, VistaCliente vistaCliente, String ficheroSeleccionado, File file) {
        this.entrada = entrada;
        this.salida = salida;
        this.vistaCliente = vistaCliente;
        this.ficheroSeleccionado = ficheroSeleccionado;
         this.file = file;
        contador = 0;
    }

    @Override
    protected void done() {
        vistaCliente.lblEstado.setText("Estado: Descarga finalizada");
        vistaCliente.lblEstado.setBackground(Color.GREEN);
    }

    @Override
    protected void process(List<Integer> valores) {
        if(!valores.isEmpty()){
            vistaCliente.barraProgreso.setValue((Integer) valores.get(valores.size() - 1));
        }
    }

    @Override
    protected Void doInBackground() throws Exception {

        int selec = 2; //Envio la opcion he presionado
        salida.writeInt(selec);
        salida.writeObject(ficheroSeleccionado);
        salida.flush();

        if (contador > 0) {
            while (!vistaCliente.lblEstado.getText().equals("Estado: Descarga finalizada")) {
                try {
                    sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        contador++;
        vistaCliente.lblEstado.setText("Estado: Descarga en proceso");
        vistaCliente.lblEstado.setForeground(Color.black);

        //LEO el fichero que me envia HiloCliente
        fileSize = (long) entrada.readLong();
        System.out.println("Tamañado del fichero " + fileSize);
        FileOutputStream escritorFichero = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        long totalLeido = 0;
        int bytesLeidos;

        while(totalLeido < fileSize && (bytesLeidos = entrada.read(buffer)) > 0 ){
            escritorFichero.write(buffer, 0, bytesLeidos);
            totalLeido += bytesLeidos;
            int progreso =(int) (totalLeido*100/fileSize);
            publish(progreso);
        }

        System.out.println("Fichero escrito " + ficheroSeleccionado + " tamaño: " + totalLeido);
        escritorFichero.close();

        return null;
    }
}
