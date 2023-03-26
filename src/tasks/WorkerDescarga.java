package tasks;

import com.oracle.webservices.internal.api.message.PropertySet;
import gui.VistaCliente;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

import static java.lang.Thread.sleep;

public class WorkerDescarga extends SwingWorker<Void, Integer> {
    private long fileSize;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private VistaCliente vistaCliente;
    private String nombreFichero;
    private File file;
    private boolean estadoDescarga;
    private String nombreArchivo;

    private Socket cliente;

    public WorkerDescarga(ObjectInputStream entrada, ObjectOutputStream salida, VistaCliente vistaCliente, String ficheroSeleccionado, File file, Socket cliente) {
        this.entrada = entrada;
        this.salida = salida;
        this.vistaCliente = vistaCliente;
        this.nombreFichero = ficheroSeleccionado;
        this.file = file;
        this.cliente = cliente;
        estadoDescarga = false;

        if (isCancelled()) {
            firePropertyChange("Eliminar", false, false);
        }
    }

    private void descargaProgreso() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                vistaCliente.lblEstado.setText("Estado: Procesando...");
                vistaCliente.btnPausar.setText("Pausar");
            }
        });
    }

    private void descargaPausada(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                vistaCliente.lblEstado.setText("Estado: Transferencia pausada.");
                vistaCliente.btnPausar.setText("Reanudar");
            }
        });
    }

    @Override
    protected void process(List<Integer> valores) {
        if (!valores.isEmpty()) {
            int valor = valores.get(valores.size() - 1);
            vistaCliente.barraProgreso.setValue(valor);
            vistaCliente.barraProgreso.setString(valor + "%");
        }
    }

    @Override
    protected void done() {
        vistaCliente.barraProgreso.setValue(100);
        vistaCliente.btnCancelar.setEnabled(false);
        vistaCliente.btnPausar.setEnabled(false);
        vistaCliente.btnPausar.setText("Pausar");
        vistaCliente.lblEstado.setText("Estado: Descarga finalizada");

    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            System.out.println("-------------CLASE WORKER DESCARGA-------------");
            salida.writeObject(nombreFichero);
            salida.flush();
            fileSize = (long) entrada.readObject();

            System.out.println("Nombre del fichero " + nombreFichero + " tamañado del fichero " + fileSize);
            descargaProgreso();

            //LEO el fichero que me envia HiloCliente
            nombreArchivo = "copy_" + nombreFichero;
            int numArchivo = 1;
            //compruebo si ya existe el fichero
            File archivo = new File(nombreArchivo);
            while (archivo.exists()) {
                nombreArchivo = "copy_" + numArchivo + "_" + nombreFichero;
                archivo = new File(nombreArchivo);
                numArchivo++;
            }
            FileOutputStream escritorFichero = new FileOutputStream(nombreArchivo);
            byte[] buffer = new byte[1024];
            long totalLeido = 0;
            int bytesLeidos;

            while (totalLeido < fileSize && (bytesLeidos = entrada.read(buffer)) > 0 && !isCancelled()) {
                while (estadoDescarga) {
                    try {
                        pausar();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                escritorFichero.write(buffer, 0, bytesLeidos);
                totalLeido += bytesLeidos;
                int progreso = (int) (totalLeido * 100 / fileSize);
                publish(progreso);
            }
            System.out.println("Fichero escrito " + nombreFichero + " tamaño: " + totalLeido);
            System.out.println("---------------------------" + "\n");
            escritorFichero.close();
        } catch (EOFException e) {
            System.out.println("Cliente desconectado");
        } catch (StreamCorruptedException e) {
            System.err.println("Datos corruptos recibidos");
        } catch (FileNotFoundException e) {
            System.out.println("Archivo no encontrado" + e.getClass());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (isCancelled()) {
            firePropertyChange("Eliminar", false, true);
        }
        return null;
    }

    public void eliminarArchivo() {
        File archivo = new File(nombreArchivo);
        if (archivo.exists()) {
            archivo.delete();
            JOptionPane.showMessageDialog(null, "Archivo eliminado " + nombreArchivo, "Aviso", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("Archivo " + nombreArchivo + " eliminado");
        }
    }

    public void pausarDescarga(boolean pausa) throws InterruptedException {
        estadoDescarga = pausa;
        descargaPausada();
        if (!pausa) {
            reanudar();
            descargaProgreso();
        }
    }

    /**
     * Metodo para reanudar las descargas
     */
    private synchronized void reanudar() {
        notify();
    }

    /**
     * Metodo para pausar descargas
     *
     * @throws InterruptedException
     */
    private synchronized void pausar() throws InterruptedException {
        wait();
    }

    public void cerrarSocket(){
        try {
            if (cliente != null && !cliente.isClosed()) {
                cliente.close();
            }
        } catch (IOException e) {

        }
    }
}
