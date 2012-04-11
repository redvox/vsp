/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

/**
 *
 * @author Ng
 */
public class ClassLoaderMobileAgent extends ClassLoader {

    // Klasse in der Virtual Machine erzeugen, in dem die Methode defineClass() aufgerufen wird.
    // defineClass -> Converts an array of bytes into an instance of class Class.
    public Class<?> defineClass(byte[] binaercodeMA, int size) {

        Class<?> c = defineClass("Agent", binaercodeMA, 0, size);

        // Before the Class can be used it must be resolved.
        this.resolveClass(c);

        return c;
    }
}
