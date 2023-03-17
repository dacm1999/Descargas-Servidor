package tasks;

import gui.VistaCliente;

import java.io.*;
import javax.swing.*;

public class NoSIRVE extends SwingWorker<Void, Integer> {
    private File archivo;
    private JProgressBar barraProgreso;

    private BufferedOutputStream bos;
    private BufferedInputStream bis;

    VistaCliente vistaCliente;

    public NoSIRVE(File archivo, JProgressBar barraProgreso) {
        this.archivo = archivo;
        this.barraProgreso = barraProgreso;
    }

    @Override
    protected Void doInBackground() throws Exception {
        long fileSize = archivo.length();
        long bytesEnviados = 0;
        byte[] buffer = new byte[8192];
        BufferedInputStream archivoBis = new BufferedInputStream(new FileInputStream(archivo));

        bos.write(("UPLOAD " + archivo.getName() + "\n").getBytes());
        bos.flush();

        int n;
        while ((n = archivoBis.read(buffer)) != -1) {
            bos.write(buffer, 0, n);
            bos.flush();
            bytesEnviados += n;
            int progreso = (int) ((bytesEnviados * 100) / fileSize);
            publish(progreso);
        }

        archivoBis.close();
        return null;
    }

    @Override
    protected void process(java.util.List<Integer> chunks) {
        int progreso = chunks.get(chunks.size() - 1);
        // actualizar la barra de progreso de la interfaz
        vistaCliente.barraProgreso.setValue(progreso);
    }
}
