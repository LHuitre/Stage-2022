package utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileSerialization {
	
	/**
	 * Sérialise un objet java dans le répertoire /ser
	 * @param data : L'objet à sérialiser
	 * @param nameOutput : Le nom du fichier sérialisé (sans spécifier le répertoire et l'extension)
	 * @param compressed : true si le fichier sérialisé doit être compressé au format .zipser
	 * @throws IOException
	 */
	public static void serialization(Object data, String nameOutput, boolean compressed) throws IOException {
		DecimalFormat df = new DecimalFormat("0.00");
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		
		String filePath;
		if(compressed) filePath = "ser/" + nameOutput + ".zipser";
		else filePath = "ser/" + nameOutput + ".ser";
		
		long timeInitSerial = System.currentTimeMillis();
		Date date = new Date(timeInitSerial);
		System.out.println("\nSerialization of /" + filePath + " started at " + formatter.format(date));
		
		ObjectOutputStream out;
		FileOutputStream fileOut = new FileOutputStream(filePath);;
		if(compressed){
			GZIPOutputStream gzOut = new GZIPOutputStream(fileOut); //compression pour optimiser la mémoire utilisée
			out = new ObjectOutputStream(new BufferedOutputStream(gzOut)); //buffered optimise grandement le temps d'exécution
			
		}
		else {
			out = new ObjectOutputStream(new BufferedOutputStream(fileOut)); //buffered optimise grandement le temps d'exécution
		}
		
		out.writeObject(data);
		out.close();
		fileOut.close();
		
		long size = Files.size(Paths.get(filePath));
		
		long timeEndSerial = System.currentTimeMillis();
		System.out.println("Serialized file /" + filePath + " (" + df.format(size/(1024.0*1024.0)) + " Mo) sucessfully written in ser/ in " + df.format((timeEndSerial - timeInitSerial)/1000.0) + " s.");
	}

	
	/** Désérialisation d'un fichier .ser ou .zipser présent dans le répertoire /ser.
	 * !!! Il faut penser à caster le résultat pour obtenir le type souhaité !!!
	 * @param inputFile : Le fichier présent dans le répertoire /ser
	 * @return L'objet obtenu lors de la d�s�rialisation
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws Exception
	 */
	public static Object deserialization(String inputFile) throws IOException, ClassNotFoundException {
		DecimalFormat df = new DecimalFormat("0.00");
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		
		long size = Files.size(Paths.get("ser/" + inputFile));
		
		long timeInitDeserial = System.currentTimeMillis();
		Date date = new Date(timeInitDeserial);
		System.out.println("\nDeserialization of /ser/" + inputFile + " (" + df.format(size/(1024.0*1024.0)) + " Mo) started at " + formatter.format(date));
		
		
		Object data = new Object();
		
		FileInputStream fileIn = new FileInputStream("ser/" + inputFile);
		
		ObjectInputStream in;
		if(inputFile.endsWith(".zipser")) {
			GZIPInputStream gzIn = new GZIPInputStream(fileIn); //décompression prend un petit plus de temps que sans compression (~20 % en plus)
			in = new ObjectInputStream(new BufferedInputStream(gzIn)); //buffered optimise grandement le temps d'ex�cution
		}
		else{
			in = new ObjectInputStream(new BufferedInputStream(fileIn)); //buffered optimise grandement le temps d'ex�cution
		}
		data = in.readObject();
		in.close();
		fileIn.close();
		
		long timeEndDeserial = System.currentTimeMillis();
		
		
		
		System.out.println("Deserialization of /ser/" + inputFile + " sucessfully done in " + df.format((timeEndDeserial - timeInitDeserial)/1000.0) + " s.");
		
		return data;
	}

}
