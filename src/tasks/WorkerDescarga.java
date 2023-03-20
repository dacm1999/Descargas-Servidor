package tasks;

import gui.VistaCliente;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

import static java.lang.Thread.sleep;
import static javax.xml.ws.Endpoint.publish;

public class WorkerDescarga extends SwingWorker<Void,Integer> {

    Socket socket;
    boolean pausado;
    int contador;
    long fileSize;
    ObjectInputStream entrada;
    ObjectOutputStream salida;
    VistaCliente vistaCliente;
    private String ficheroSeleccionado;
    private File file;


    public WorkerDescarga(ObjectInputStream entrada, ObjectOutputStream salida, VistaCliente vistaCliente, String ficheroSeleccionado, File file) {
        this.entrada = entrada;
        this.salida = salida;
        this.vistaCliente = vistaCliente;
        this.ficheroSeleccionado = ficheroSeleccionado;
        this.file = file;
    }

    @Override
    protected void process(List<Integer> valores) {
        vistaCliente.barraProgreso.setValue((Integer) valores.get(valores.size() - 1));

    }

    @Override
    protected Void doInBackground() throws Exception {

        int selec = 2;
        salida.writeInt(selec);
        salida.writeUTF(ficheroSeleccionado);
        salida.flush();

        try {
            sleep(1000);
            vistaCliente.lblEstado.setText("Estado: Descarga en proceso");
            vistaCliente.lblEstado.setForeground(Color.black);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        fileSize = (long) entrada.readLong();
        System.out.println("Tamaño del fichero" + fileSize);

        FileOutputStream escritorFichero = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        long totalLeido = 0L;
        int bytesLeidos;


        while(totalLeido < fileSize && (bytesLeidos = entrada.read(buffer)) > 0 ){
            escritorFichero.write(buffer, 0, bytesLeidos);
            totalLeido += (long)bytesLeidos;
            int progreso =(int) (totalLeido*100/fileSize);
            publish(progreso);
        }

        System.out.println("Fichero escrito " + file.getName() + " tamaño: " + totalLeido);
        escritorFichero.close();

        return null;
    }
}
