/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reclamo_parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import parser.IdmefParser;

/**
 *
 * @author Pilar
 */
public class reclamo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Leer como argumento un fichero y transformarlo en el string de entrada
          if (args.length <1){
                System.out.println("You should enter parameters");
        }

        else{
            BufferedReader entradaDatos = null;
            try {
                String xml_file = args[0];
                entradaDatos = new BufferedReader(new FileReader(xml_file));
                IdmefParser parser = new IdmefParser();
                parser.leeIDMEF(entradaDatos);
            } catch (IOException ex) {
                Logger.getLogger(reclamo.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
    }
}
