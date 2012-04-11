/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 *
 * @author Ng
 */
public class MyObjectInputStream extends ObjectInputStream {

    protected ClassLoader classLoader;

    // Muss man überschreiben! (default constructer)
    // classLoader -> von welcher Klasse ist das Objekt, dass wir empfangen! Sonst würden wir eine NotSerializableException bekommen!
    // Uns somit können wir die Klasse bestimmen die bei uns ankommt!
    public MyObjectInputStream(ClassLoader classLoader, InputStream in) throws IOException, SecurityException {
        super(in);
        this.classLoader = classLoader;
    }

    @Override
    // Lädt die Klasse, die wir empfangen wollen
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String className = desc.getName();
        return Class.forName(className, true, classLoader);
        //return classLoader.loadClass(desc.getName());
    }
}
