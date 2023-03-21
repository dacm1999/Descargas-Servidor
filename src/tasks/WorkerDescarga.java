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
    private long fileSize;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private VistaCliente vistaCliente;
    private String nombreFichero;
    private File file;
    private boolean estado;


    public WorkerDescarga(ObjectInputStream entrada, ObjectOutputStream salida, VistaCliente vistaCliente, String ficheroSeleccionado, File file) {
        this.entrada = entrada;
        this.salida = salida;
        this.vistaCliente = vistaCliente;
        this.nombreFichero = ficheroSeleccionado;
        this.file = file;
        estado = true;
    }

    @Override
    protected void done() {
        vistaCliente.barraProgreso.setValue(100);
        vistaCliente.lblEstado.setText("Estado: Descarga finalizada");
    }

    @Override
    protected void process(List<Integer> valores) {
        if (!valores.isEmpty()) {
            int valor = valores.get(valores.size() - 1);
            vistaCliente.barraProgreso.setValue(valor);
            vistaCliente.barraProgreso.setString(valor + "%");
//            vistaCliente.barraProgreso.setValue((Integer) valores.get(valores.size() - 1));
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            do {
                System.out.println("-------------CLASE WORKER DESCARGA-------------");
                salida.writeObject(nombreFichero);
                salida.flush();
                fileSize = (long) entrada.readObject();

                System.out.println("Nombre del fichero " + nombreFichero + " tamañado del fichero " + fileSize);

                vistaCliente.lblEstado.setText("Estado: Descarga en proceso");
                vistaCliente.lblEstado.setForeground(Color.black);

                //LEO el fichero que me envia HiloCliente
                FileOutputStream escritorFichero = new FileOutputStream("copy_" + file);
                byte[] buffer = new byte[1024];
                long totalLeido = 0;
                int bytesLeidos;

                while (totalLeido < fileSize && (bytesLeidos = entrada.read(buffer)) > 0) {
                    escritorFichero.write(buffer, 0, bytesLeidos);
                    totalLeido += bytesLeidos;
                    int progreso = (int) (totalLeido * 100 / fileSize);
                    publish(progreso);
                }

                System.out.println("Fichero escrito " + nombreFichero + " tamaño: " + totalLeido);
                escritorFichero.close();
                nombreFichero = (String) entrada.readObject(); // Lee un nuevo nombre de fichero si está disponible
            } while (nombreFichero != null);
        } catch (EOFException e) {
            // Expected exception when the socket is closed by the client
            System.out.println("Cliente desconectado");
        } catch (StreamCorruptedException e) {
            // Handle corrupted data
            System.err.println("Datos corruptos recibidos");
        } catch (FileNotFoundException e) {
            System.out.println("Archivo no encontrado" + e.getClass());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
