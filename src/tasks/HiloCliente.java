package tasks;

import controlador.ControladorServidor;
import gui.VistaServidor;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

public class HiloCliente extends Thread {


    private Socket socketCliente;
    private List<String> listaDescargas;
    private VistaServidor vista;

    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private ControladorServidor controladorServidor;
    private int seleccion;

    private File file;

    /**
     * Constructor de la clase HiloCliente
     *
     * @param socketCliente
     * @param listaDescargas
     * @param vista
     */
    public HiloCliente(Socket socketCliente, List<String> listaDescargas, VistaServidor vista) {
        this.socketCliente = socketCliente;
        this.listaDescargas = listaDescargas;
        this.vista = vista;
        seleccion = 0;
    }

    /**
     *
     */
    @Override
    public void run() {
        try {
            do {
                this.salida = new ObjectOutputStream(this.socketCliente.getOutputStream());
                this.entrada = new ObjectInputStream(this.socketCliente.getInputStream());

                // Agrega el nombre del cliente a la lista de clientes y actualiza la vista
                String ip = String.valueOf(this.socketCliente.getInetAddress());

                // Maneja las solicitudes del cliente
                opciones();
                refrescarDescargas();
            } while (!socketCliente.isClosed());

        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            refrescarDescargas();
        }
    }

    private void opciones() {
        while (!socketCliente.isClosed()) { // Verifica si la conexión sigue abierta
            try {
                if (entrada.available() >= 4) { // Verifica si hay al menos 4 bytes disponibles para leer
                    seleccion = entrada.readInt();
                    System.out.println("opcion seleccionada " + seleccion);
                    switch (seleccion) {
                        case 0: {
                            System.out.println("Refrescar button " + " seleccion " + seleccion);
                            enviarListaFicheros();
                            break;
                        }
                        case 1: {
                            System.out.println("Descargar button " + " seleccion" + seleccion);
                            descargas();
                            break;
                        }
                        case 2: {
                            ficheroSubido();
                            refrescarDescargas();
                            break;
                        }
                        case 3: {

                            break;
                        }
                    }
                }
            } catch (EOFException e) { // Captura específicamente la excepción EOFException
                System.out.println("Se ha cerrado la conexión del servidor");
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Envio la lista de ficheros
     */
    private void enviarListaFicheros() {
        try {
            salida.writeObject(listaDescargas);
            salida.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ficheroSubido() {

        try {
            long tamanoFichero = this.entrada.readLong();
            String nombreFichero = (String) this.entrada.readObject();
            System.out.println("Nombre del fichero " + nombreFichero + "Tamaño del fichero a recibir: " + tamanoFichero);

            if (tamanoFichero > 0L) {
                int i = 1;

                System.out.println("Subiendo archivo: " + nombreFichero + ", tamaño: " + tamanoFichero + getClass());
                FileOutputStream ficheroSalida = new FileOutputStream(nombreFichero);
                long totalWrited = 0L;
                byte[] buffer = new byte[1024];
                int bytesLeidos;

                while (tamanoFichero > totalWrited && (bytesLeidos = entrada.read(buffer)) > 0) {
                    ficheroSalida.write(buffer, 0, bytesLeidos);
                    totalWrited += (long) bytesLeidos;
                }
                ficheroSalida.close();
                listaDescargas.add(nombreFichero);

                for (String nombre : listaDescargas) {
                    System.out.println("ficheros agregados: " + nombre);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     *
     */
    private void descargas() {
        try {
            do {
                String nombreFichero = (String) entrada.readObject();
                file = new File(nombreFichero);
                long fileSize = file.length();
                System.out.println("Nombre del fichero " + nombreFichero + " tamaño del fichero " + fileSize);
                long totalEscrito = 0L;

                //Lo lee la clase WorkerDescarga
                salida.writeObject(fileSize);
                salida.flush();

                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int bytesLeidos;

                while (totalEscrito < fileSize && (bytesLeidos = fileInputStream.read(buffer)) > 0) {
                    salida.write(buffer, 0, bytesLeidos);
                    salida.flush();
                    totalEscrito += bytesLeidos;
                }
                fileInputStream.close();
                System.out.println("Nombre del fichero " + nombreFichero + " BYTES ESCRITOS " + totalEscrito);
            } while (!socketCliente.isClosed());
            salida.close();
            entrada.close();
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
    }



    /**
     * Muestra los ficheros en la GUI
     */
    private void refrescarDescargas() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Estoy en el EDT: " + SwingUtilities.isEventDispatchThread());

                try {
                    DefaultListModel modelo = new DefaultListModel<>();
                    for (String descarga : listaDescargas) {
                        modelo.addElement(descarga);
                    }
                    vista.listaGUI.setModel(modelo);
                } catch (NullPointerException ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                    throw new RuntimeException(ex);
                } finally {
                    System.out.println("Ficheros subidos: ");
                    listaDescargas.forEach(System.out::println);
                }
            }
        });
    }


}
