import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Caching {

	public static final String BASE_DIR = "./admin/Cache/";
	public static final String BASE_DIR_LOG = "./admin/Log.txt";
		
	/**
	 * Stores a record of websites requested with the current time and date
	 * @param url website requested by the user
	 */
	public static void logHistory(String url) {
		try {
			String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
			String log = url + " accessed at " + timeStamp + "\n";
			Path filePath = Paths.get(BASE_DIR_LOG);
			Files.write(filePath, log.getBytes(), StandardOpenOption.APPEND);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a directory pertaining to the website requested
	 * @param uri host being pinged
	 */
	public static void createDir(String uri) {
		File directory = new File(BASE_DIR + uri);
		if(!directory.exists()) {
			boolean result = false;
			try {
				directory.mkdir();
				result = true;
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if(result) {
					System.out.println("Caching folder for " + uri + " has been created!");
				}
			}
		}
	}
	
	/**
	 * Stores and recursively downloads the appropriate file to be cached
	 * @param contents data being cached 
	 * @param uri host name pinged by the request
	 */
	public static void saveFile(String contents, String uri) {
	    String extension = uri.substring(uri.lastIndexOf("."));
		if(!extension.contains(".com") || !extension.contains(".ie")){
			try {
				String fileDir = BASE_DIR + uri + "/";
				System.out.println(fileDir);
				
				byte data[] = contents.getBytes();
				
				File file = new File(fileDir);
				if(file.exists()) {
					FileOutputStream out = new FileOutputStream(fileDir);
					out.write(data);
					out.close();
				} else {
					createDir(uri);	
					file.createNewFile();
					FileOutputStream out = new FileOutputStream(fileDir);
					out.write(data);
					out.close();
				}
				
				System.out.println("File cached successful in " + fileDir);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
