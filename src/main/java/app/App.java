package app;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;


public class App {

	private static final String BIN_INPUT = "bin/input/";
	private static final String MATCHER = "^[A-Z]{2}\\.";
	private static final String REGEX = ".*[a-zA-Z].*";
	private static final String INPUT = "input/";
	private static final String DELIMITER = "%s ^ %s";
	private static final String DIR = new File(System.getProperty("user.dir")).getParent();
	private static final String FILENAME = DIR.concat("\\missing_translations.csv");
	private static final Path PATHNAME = Paths.get(FILENAME);
	
	public static void main(String[] args) {
		
		Scanner scanner = new Scanner(System.in);
		App app = new App();
		boolean validUserInput;
		do {
			System.out.println("Enter the .properties file name:");
			String inputFile = scanner.next();
			validUserInput = app.validateAndBuildExport(inputFile);
		}
		while(!validUserInput);
		
		scanner.close();
	}
	
	private boolean validateAndBuildExport(String inputFile) {
		
		URL fileExists = getClass().getClassLoader().getResource(INPUT+inputFile);
		if (fileExists == null) {
			return false;
		} 
		long startTime = System.currentTimeMillis();
		buildExport(inputFile);
		long finishTime = System.currentTimeMillis();
		System.out.println("\nThat took: " + (finishTime - startTime) + " ms");
		return true;
	}
	
	private void buildExport(String inputFile){
		
		try (BufferedWriter bw = Files.newBufferedWriter(PATHNAME)) {
			List<PropertyBean> resultsWithoutCountries = getAllKeysAndSortWithoutCountries(INPUT+inputFile);
			List<PropertyBean> resultsWithCountries = getAllKeysAndSortWithCountries(INPUT+inputFile);			
			List<String> files = getProperFiles(inputFile);
			Properties prop = new Properties();
			System.out.print("\nWriting to file..");
			bw.newLine();
			for (String fileName: files) {
				
				bw.write("***************************************************************************");
				bw.newLine();
				bw.write("********************** File name: "+ fileName+" **********************");
				bw.newLine();
				bw.write("****************************************************************************");
				bw.newLine();

				try (InputStream input = getClass().getClassLoader().getResourceAsStream(INPUT+fileName)){
					prop.load(input);

					for (PropertyBean key : resultsWithoutCountries) {
							if (!hasTheKey(key.getKey(), prop)) {
								bw.write(String.format(DELIMITER, key.getKey(),key.getValue()));
								bw.newLine();
							}
					}
					for (PropertyBean key2 : resultsWithCountries) {
						
						/* Checks if list has a key mentioned in the primary file, then compares country code 
						   references in every key in the list with the appropriate file name.
						*/
							if (!hasTheKey(key2.getKey(), prop) && key2.getKey().substring(0, 2).equals(fileName.substring(10, 12))) {
								bw.write(String.format(DELIMITER, key2.getKey(),key2.getValue()));
								bw.newLine();
							}
					}
					prop.clear();

			   } catch (IOException ex ) {
					ex.printStackTrace();
				}
			}
			System.out.println(" done!");
			

		 } catch (IOException e) {
			e.printStackTrace();
		   }		
	}
	
	private List<String> getProperFiles(String inputFileName) {
		
		List<String> fileNames = new ArrayList<>();
		File folder = new File(BIN_INPUT);
		if (folder.isDirectory()) {
			List<File> listOfFiles = Arrays.asList(folder.listFiles());
			listOfFiles.forEach(i -> { if (i.isFile() && i.getName().toLowerCase().contains(inputFileName.toLowerCase().substring(0,5))) fileNames.add(i.getName()); });
		}	
		return fileNames;
	}
	
	private boolean hasTheKey(String key, Properties prop) {
		
		boolean exists = false;
		exists = prop.keySet().stream().anyMatch(key::equals);			
		return exists;
	}
	
	private List<PropertyBean> getAllKeysAndSortWithoutCountries(String fileName) {
		
		Properties prop = new Properties();
		List<PropertyBean> list = new ArrayList<>();	
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
			prop.load(input);
			prop.forEach((key,value) -> { PropertyBean property = new PropertyBean();
										  if (!key.toString().substring(0, 3).matches(MATCHER) && !value.toString().isEmpty() && value.toString().matches(REGEX)) {
											property.setKey(key.toString());
											property.setValue(value.toString());
											list.add(property);
										 }						 
            					    	});
			list.sort((k1,k2) -> k1.getKey().compareToIgnoreCase(k2.getKey()));
			System.out.println(String.format("\n%d entries without countries' specific keys.",list.size()));
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return list;
	}
	
	private List<PropertyBean> getAllKeysAndSortWithCountries(String fileName) {
			
		Properties prop = new Properties();
		List<PropertyBean> list = new ArrayList<>();	
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
			prop.load(input);
			prop.forEach((key,value) -> { PropertyBean property = new PropertyBean();
										  if (key.toString().substring(0, 3).matches(MATCHER) && !value.toString().isEmpty() && value.toString().matches(REGEX)) {
											property.setKey(key.toString());
											property.setValue(value.toString());
											list.add(property);
										 }						 
            					    	});
			list.sort((k1,k2) -> k1.getKey().compareToIgnoreCase(k2.getKey()));			
			System.out.println(String.format("%d entries with countries' specific keys.",list.size()));

				
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return list;
	}
}
