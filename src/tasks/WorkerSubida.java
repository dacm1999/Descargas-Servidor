package tasks;

import gui.VistaCliente;

import javax.swing.*;
import java.awt.*;
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
    private boolean transfiriendo;

    public WorkerSubida(ObjectOutputStream salida, VistaCliente vista, File fichero) {
        this.salida = salida;
        this.vista = vista;
        this.fichero = fichero;
        //this.transfiriendo = transfiriendo;
    }

    @Override
    protected Void doInBackground() throws Exception {
        //envio el boton que he pulsado
//        int seleccion = 3;
//        salida.writeInt(seleccion);


        System.out.println("-------------CLASE WORKER ENVIO-------------");
        //Envio el fichero
        tamanoFichero = fichero.length();
        salida.writeLong(tamanoFichero);
        salida.writeObject(fichero.getName());
        System.out.println("Tamaño del fichero a enviar " + tamanoFichero + " NOMBRE " + fichero.getName());
        salida.flush();

//        System.out.println("NOMBRE DEL FICHERO " + fichero.getName() + " tamaño del fichero " + tamanoFichero);

        //LEO LOS FICHEROS QUE ME ENVIA EL SERVIDOR
        entradaFichero = new FileInputStream(fichero);

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

    @Override
    protected void process(List<Integer> valores) {
        if(!valores.isEmpty()) {
            int valor = valores.get(valores.size() - 1);
            vista.barraProgreso.setValue(valor);
            vista.barraProgreso.setString(valor + "%");
//            vista.barraProgreso.setValue((Integer) valores.get(valores.size() - 1));
        }
    }

    protected void done() {
        this.vista.lblEstado.setText("Se ha transferido el fichero " + fichero.getName());
        this.vista.lblEstado.setBackground(Color.GREEN);
    }
}
