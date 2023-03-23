package tasks;

import gui.VistaServidor;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class HiloCliente extends Thread {
    private Socket socketCliente;
    private List<String> listaDescargas;
    private VistaServidor vista;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private int seleccion;
    private File file;
    private int contadorClientes;
    private String nombreCliente;


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
     * El método establece flujos de entrada y salida para comunicarse con un cliente a través de un socket.
     * Luego llama a los métodos "opciones" y "refrescarDescargas" para manejar las solicitudes del cliente
     * y actualizar la vista de la lista de descargas en la interfaz gráfica de usuario.
     */
    @Override
    public void run() {
        try {
            do {
                this.salida = new ObjectOutputStream(this.socketCliente.getOutputStream());
                this.entrada = new ObjectInputStream(this.socketCliente.getInputStream());

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

    /**
     * Gestiona cada accion que realiza el cliente
     */
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
            } catch (EOFException e) {
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
    private void enviarNombreCliente() {
        contadorClientes++;
        nombreCliente = "Cliente " + contadorClientes;
        try {
            salida.writeObject(nombreCliente);
            salida.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método que recibe y guarda un archivo enviado a través de un
     * flujo de entrada de datos, especificando su nombre y tamaño.
     */
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
     * Este es un método privado llamado "descargas" que lee un archivo desde un flujo de entrada,
     * envía su tamaño a un flujo de salida, y envía el contenido del fichero.
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
                salida.writeObject(file.length());
                salida.flush();

                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int bytesLeidos;

                while (totalEscrito < fileSize && (bytesLeidos = fileInputStream.read(buffer)) > 0) {
                    salida.write(buffer, 0, bytesLeidos);
                    salida.flush();
                    totalEscrito += bytesLeidos;
                }
                System.out.println("Nombre del fichero " + nombreFichero + " BYTES ESCRITOS " + totalEscrito);
                fileInputStream.close();
            } while (!socketCliente.isClosed());
            salida.close();
            entrada.close();
        } catch (OptionalDataException ex) {
            System.out.println("ERROR" + ex.getMessage());
        } catch (SocketException ex) {
            System.out.println("CLIENTE DESCONECTADO " + socketCliente.getInetAddress().getHostAddress());
        } catch (NullPointerException e) {
            System.out.println("NO DEBE SER NULL");
        } catch (IOException ex) {
            System.out.println("ERROR EN EL BUFFER");
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Clase no encontrada");
        } catch (Exception e) {
        }
    }

    /**
     * Método que actualiza la vista de la lista de descargas en la interfaz gráfica de usuario
     * Utiliza "SwingUtilities.invokeLater" para ejecutar el código en el Event Dispatch Thread (EDT) de Swing para poder
     * actualizar las componentes de forma segura.
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
