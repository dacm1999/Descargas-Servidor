package tasks;

import java.io.File;
import java.io.Serializable;

public class Transferencia implements Serializable {

    private String nombre;
    private File fichero;

    public Transferencia(String nombre, File fichero) {
        this.nombre = nombre;
        this.fichero = fichero;
    }

    public String getNombre() {
        return "copy_"+this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = "copy_"+nombre;
    }

    public File getFichero() {
        return this.fichero;
    }

    public void setFichero(File fichero) {
        this.fichero = fichero;
    }

    public String toString() {
        return this.nombre;
    }
}