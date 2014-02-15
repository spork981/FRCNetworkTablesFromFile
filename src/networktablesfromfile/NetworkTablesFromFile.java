/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package networktablesfromfile;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class NetworkTablesFromFile {
    // edit these values! NOTE: the settings file will overwrite these
    static String FILENAME = "test.txt";  // including path, if needed
    //final static String CRIO_IP = "10.32.43.2"; // IPV4 address
    static String CRIO_IP = "10.42.0.1"; // IPV4 address
    static String TABLE_NAME = "default"; // NetworkTable name
    static double POLL_TIME = 0.5;  // in seconds
    
    final static String SETTINGS_FILENAME = "settings.txt"; // settings file    
    
    static NetworkTable table;
    
    private void run() {
        while(true) {
            try {
                readFile(FILENAME);

                Thread.sleep((long) (POLL_TIME * 1000));
            } catch (InterruptedException ex) {
                Logger.getLogger(NetworkTablesFromFile.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        int res = readSettingsFile();
        if(res < 0) return;
        
        System.out.println("cRIO IP: " + CRIO_IP);
        System.out.println("Table: " + TABLE_NAME);
        
        NetworkTable.setClientMode();
        NetworkTable.setIPAddress(CRIO_IP);
        
        try {
            NetworkTable.initialize();
            table = NetworkTable.getTable(TABLE_NAME);
            System.out.println("running...");
            new NetworkTablesFromFile().run();
        } catch (IOException ex) {
            Logger.getLogger(NetworkTablesFromFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static int readSettingsFile() {
        try {
			BufferedReader reader = Files.newBufferedReader(Paths.get(SETTINGS_FILENAME), Charset.defaultCharset());
			String s;
			while((s = reader.readLine()) != null) {
				//parseInputLine(s);
                String[] s_arr = s.split(":");
                if(s_arr[0].equals("crio") || s_arr[0].equals("ip")) {
                    CRIO_IP = s_arr[1].replaceAll(" ", "");
                } else if(s_arr[0].equals("table") || s_arr[0].equals("tablename") || s_arr[0].equals("name")) {
                    TABLE_NAME = s_arr[1].replaceAll(" ", "");
                } else if(s_arr[0].equals("file") || s_arr[0].equals("filename")) {
                    FILENAME = s_arr[1].replaceAll(" ", "");
                } else if(s_arr[0].equals("poll")) {
                    POLL_TIME = Double.parseDouble(s_arr[1].replaceAll(" ", ""));
                } else {
                    System.out.println("invalid settings in " + SETTINGS_FILENAME + "!");
                    return -1; // error
                }
			}
		} catch (IOException e) {
            System.out.println("using hardcoded settings");
			e.printStackTrace();
		}
        
        return 0;
    }
    
    private static int readFile(String filename) {
		try {
			BufferedReader reader = Files.newBufferedReader(Paths.get(filename), Charset.defaultCharset());
			String s = "";
			while((s = reader.readLine()) != null) {
				parseInputLine(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        return 1; // error
    }

    private static void parseInputLine(String s) {
        String arr[] = s.split(",");

        if(isDouble(arr[1])) {
            double num = Double.parseDouble(arr[1]);
            table.putNumber(arr[0], num);
            //System.out.println(arr[0] + ", " + arr[1]);
        } else if(arr[1].equals("false") || arr[1].equals("true")) {
            table.putBoolean(arr[0], Boolean.parseBoolean(arr[1]));
        } else { // must be a string
            table.putString(arr[0], arr[1]);
        }
    }

    // from the Java Double documentation
    private static boolean isDouble(String s) {
        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally 
        // signed decimal integer.
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex =
                ("[\\x00-\\x20]*" + // Optional leading "whitespace"
                "[+-]?(" + // Optional sign character
                "NaN|" + // "NaN" string
                "Infinity|" // "Infinity" string
                + "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|"
                + "(\\.(" + Digits + ")(" + Exp + ")?)|"
                + "(("
                + "(0[xX]" + HexDigits + "(\\.)?)|"
                + "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")"
                + ")[pP][+-]?" + Digits + "))"
                + "[fFdD]?))"
                + "[\\x00-\\x20]*");// Optional trailing "whitespace"

        if (Pattern.matches(fpRegex, s)) {
            return true;
        } else {
            return false;
        }
    }
}
