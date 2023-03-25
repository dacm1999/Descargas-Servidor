package tasks;

import gui.VistaCliente;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class WorkerSubida extends SwingWorker<Void, Integer> {
    private ObjectOutputStream salida;
    private FileInputStream entradaFichero;
    private VistaCliente vista;
    private long tamanoFichero;
    private File fichero;

    public WorkerSubida(ObjectOutputStream salida, VistaCliente vista, File fichero) {
        this.salida = salida;
        this.vista = vista;
        this.fichero = fichero;
    }

    private void progresoSubida(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                vista.lblEstado.setText("Estado: subiendo fichero");
            }
        });
    }

    @Override
    protected void process(List<Integer> valores) {
        if(!valores.isEmpty()) {
            int valor = valores.get(valores.size() - 1);
            vista.barraProgreso.setValue(valor);
            vista.barraProgreso.setString(valor + "%");
        }
    }

    protected void done() {
        vista.lblEstado.setText("Se ha transferido el fichero " + fichero.getName());
    }

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

        progresoSubida();
        byte[] buffer = new byte[1024];
        int bytesLeidos;
        long totalBytesLeidos = 0L;

        while ((bytesLeidos = entradaFichero.read(buffer)) > 0) {
            salida.write(buffer, 0, bytesLeidos);
            salida.flush();
            totalBytesLeidos += bytesLeidos;
            int porcentaje = (int)(totalBytesLeidos *100 / tamanoFichero);
            publish(porcentaje);
        }
        System.out.println("NOMBRE DEL FICHERO: " + fichero.getName() + " -- tamaño del fichero: " + tamanoFichero);
        entradaFichero.close();
        return null;
    }
}
