package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class MultiWordTrie {
	
	/**
	 * Cherche un fichier contenant les multi-mots dans /txt_data.
	 * On se base sur le nom de fichier qui doit se terminer par "ENTRIES-MWE.txt"
	 * @return Le fichier correspondant
	 * @throws FileNotFoundException si aucun ou plusieurs fichiers se terminent par "ENTRIES-MWE.txt"
	 */
	private static File getFileNameMWE() throws FileNotFoundException {
		File root = new File("txt_data/");
		
	    final Pattern p = Pattern.compile(".*ENTRIES-MWE\\.txt"); // careful: could also throw an exception!
	    File[] files = root.listFiles(new FileFilter(){
	        @Override
	        public boolean accept(File file) {
	            return p.matcher(file.getName()).matches();
	        }
	    });
	    
	    if(files.length == 1) {
	    	System.out.println("\nFile containing MWE found : " + files[0].getName());
	    	return files[0];
	    }
	    
	    if(files.length == 0) {
	    	throw new FileNotFoundException("Aucun fichier de multi-mots n'a été trouvé dans le répertoire /txt_data.");
	    }
	    
	    if(files.length > 1) {
	    	throw new FileNotFoundException("Plusieurs fichiers ont été trouvés dans le répertoire /txt_data. Impossible de choisir");
	    }
	    
	    return null;
	}
	
	/**
	 * Crée le fichier sérialisé correspondant au Trie des MWE.
	 * @return
	 * @throws IOException si le fichier de base n'est pas trouvé.
	 */
	public static TrieHash serializeFileMWE() throws IOException {
		DecimalFormat df = new DecimalFormat("0.00");
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		
		long timeInitSerial = System.currentTimeMillis();
		
		File file = getFileNameMWE();
		String inputFile = file.getName();
		
		Date date = new Date(timeInitSerial);
		System.out.println("\nParsing of /txt_data/" + inputFile + " started at " + formatter.format(date));
		
		BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("Cp1252")));
		
		String line;
		TrieHash trie = new TrieHash();

		while ((line = reader.readLine()) != null) {
			String[] splitLine = line.split(";");
			if(splitLine.length == 2) {
				String mwe = splitLine[1].substring(1, splitLine[1].length()-1); //suppresion des " autour du multi-mot
				mwe = mwe.replace("'", "' ");
				if(!(mwe.isBlank()
						|| mwe.contains("|")
						//|| mwe.contains(":")
						|| mwe.contains("+")
						|| mwe.contains("/")
						|| mwe.contains("!")
						|| mwe.contains("?")
						|| mwe.contains("(")
						|| mwe.contains(")")
						|| mwe.matches(".*\\d+.*")
						)) {
					String[] words = mwe.split("\\s+");
					trie.attach(words);
				}	
			}
		}

		reader.close();
		long timeEndSerial = System.currentTimeMillis();
		System.out.println("File /txt_data/" + inputFile + " sucessfully parsed in " + df.format((timeEndSerial - timeInitSerial)/1000.0) + " s.");
		
		FileSerialization.serialization(trie, "MWETrie", false);
		
		return trie;
	}
	
	/**
	 * Charge le Trie des MWE à partir du fichier sérialisé.
	 * @return
	 */
	public static TrieHash loadTrieMWE() {
		TrieHash output = null;
		
		try {
			output = (TrieHash) FileSerialization.deserialization("MWETrie.ser");
		} catch (IOException e) {
			System.err.println("Le fichier /ser/MWETrie.ser n'existe pas. Tentative de serialisation du fichier initial.");
			try {
				output = serializeFileMWE();
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return output;
	}
}
