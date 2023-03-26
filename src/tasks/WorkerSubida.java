package tasks;

import gui.VistaCliente;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static java.lang.Thread.sleep;

public class WorkerSubida extends SwingWorker<Void, Integer> {
    private ObjectOutputStream salida;
    private FileInputStream entradaFichero;
    private VistaCliente vistaServidor;
    private long tamanoFichero;
    private File fichero;
    private boolean estadoSubida;

    public WorkerSubida(ObjectOutputStream salida, VistaCliente vista, File fichero) {
        this.salida = salida;
        this.vistaServidor = vista;
        this.fichero = fichero;

        estadoSubida = false;
        if (isCancelled()) {
            firePropertyChange("Eliminar", false, false);
        }
    }

    /**
     * Este método actualiza la interfaz gráfica de usuario para indicar que la subida del fichero está en progreso
     */
    private void subidaProgreso() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                vistaServidor.lblEstado.setText("Estado: Procesando...");
                vistaServidor.btnPausar.setText("Pausar");
            }
        });
    }


    /**
     * Este método actualiza la interfaz gráfica de usuario para indicar que la subida del fichero está en pausa
     */
    private void subidaPausada() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                vistaServidor.lblEstado.setText("Estado: Transferencia pausada.");
                vistaServidor.btnPausar.setText("Reanudar");
            }
        });
    }

    /**
     * Este método es una implementación de la clase SwingWorker y se utiliza para actualizar la barra de progreso de descarga en la interfaz de usuario.
     * @param valores intermediate results to process
     *
     */
    @Override
    protected void process(List<Integer> valores) {
        if (!valores.isEmpty()) {
            int valor = valores.get(valores.size() - 1);
            vistaServidor.barraProgreso.setValue(valor);
            vistaServidor.barraProgreso.setString(valor + "%");
        }
    }

    /**
     * Este método se llama cuando la tarea de subida ha finalizado. Actualiza la vista del cliente para reflejar
     * que la descarga ha terminado y desactiva los botones relevantes.
     */
    protected void done() {
        vistaServidor.barraProgreso.setValue(100);
        vistaServidor.btnCancelar.setEnabled(false);
        vistaServidor.btnPausar.setEnabled(false);
        vistaServidor.btnPausar.setText("Pausar");
        vistaServidor.lblEstado.setText("Estado: Se ha transferido el fichero " + fichero.getName());

    }

    /**
     * Realiza la subida de un fichero en segundo plano a través de la conexión establecida.
     *
     * @return
     * @throws Exception
     */
    @Override
    protected Void doInBackground() throws Exception {
        System.out.println("-------------CLASE WORKER ENVIO-------------");
        //Envio el fichero
        tamanoFichero = fichero.length();
        salida.writeLong(tamanoFichero);
        salida.writeObject(fichero.getName());
        System.out.println("Tamaño del fichero a enviar " + tamanoFichero + " NOMBRE " + fichero.getName());
        salida.flush();

        //LEO LOS FICHEROS QUE ME ENVIA EL SERVIDOR
        entradaFichero = new FileInputStream(fichero);

        subidaProgreso();
        byte[] buffer = new byte[1024];
        int bytesLeidos;
        long totalBytesLeidos = 0L;

        while ((bytesLeidos = entradaFichero.read(buffer)) > 0) {
            while (estadoSubida) {
                try {
                    pausar();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            salida.write(buffer, 0, bytesLeidos);
            salida.flush();
            totalBytesLeidos += bytesLeidos;
            int porcentaje = (int) (totalBytesLeidos * 100 / tamanoFichero);
            publish(porcentaje);
        }
        System.out.println("NOMBRE DEL FICHERO: " + fichero.getName() + " -- tamaño del fichero: " + tamanoFichero);
        entradaFichero.close();
        if (isCancelled()) {
            firePropertyChange("Eliminar", false, true);
        }
        return null;
    }

    public void eliminarArchivo(String nombreArchivo2) {
        File archivo = new File(fichero.getName());
        if (archivo.exists()) {
            archivo.delete();
            JOptionPane.showMessageDialog(null, "Archivo eliminado " + fichero.getName(), "Aviso", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("Archivo " + fichero.getName() + " eliminado");
        }
    }

    /**
     * Este método pausa o reanuda la subida actual y actualiza la vista correspondiente.
     *
     * @param pausa
     * @throws InterruptedException
     */
    public void pausarSubida(boolean pausa) throws InterruptedException {
        estadoSubida = pausa;
        subidaPausada();
        if (!pausa) {
            reanudar();
            subidaProgreso();
        }
    }

    /**
     * Metodo para reanudar las subidas
     */
    private synchronized void reanudar() {
        notify();
    }

    /**
     * Metodo para pausar subidas
     *
     * @throws InterruptedException
     */
    private synchronized void pausar() throws InterruptedException {
        wait();
    }
}
